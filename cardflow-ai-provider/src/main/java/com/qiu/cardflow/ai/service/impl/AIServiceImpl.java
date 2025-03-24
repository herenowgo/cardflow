package com.qiu.cardflow.ai.service.impl;

import com.qiu.cardflow.ai.dto.ChatRequestDTO;
import com.qiu.cardflow.ai.dto.StructuredOutputRequestDTO;
import com.qiu.cardflow.ai.model.AIModel;
import com.qiu.cardflow.ai.model.AIModelFactory;
import com.qiu.cardflow.ai.model.AIModelInstance;
import com.qiu.cardflow.ai.service.IAIService;
import com.qiu.cardflow.ai.structured.TargetType;
import com.qiu.cardflow.ai.util.ChatClientRequestSpecBuilder;
import com.qiu.cardflow.redis.starter.key.AICacheKeyBuilder;
import com.qiu.cardflow.redis.starter.key.EventStreamKeyBuilder;
import com.qiu.cardflow.rpc.starter.RPCContext;
import com.qiu.codeflow.eventStream.dto.EventMessage;
import com.qiu.codeflow.eventStream.dto.EventType;
import com.qiu.codeflow.eventStream.message.EventStreamMessageConstant;
import com.qiu.codeflow.eventStream.util.EventMessageUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIServiceImpl implements IAIService {

    private static final String CHECK_AND_DECR_SCRIPT = "local credit_key = ARGV[1] -- 直接使用传入的 Key\n" + //
            "local initial_credit = ARGV[2] -- 从外部参数获取初始值\n" + //
            "local credit = tonumber(redis.call('get', credit_key))\n" + //
            "\n" + //
            "if not credit then\n" + //
            "  redis.call('set', credit_key, initial_credit) -- 创建 Key 并设置初始值\n" + //
            "  credit = tonumber(initial_credit) -- 更新 credit 变量\n" + //
            "end\n" + //
            "\n" + //
            "if credit > 0 then\n" + //
            "  redis.call('decr', credit_key)\n" + //
            "  return 1 -- 允许调用\n" + //
            "else\n" + //
            "  return 0 -- 拒绝调用\n" + //
            "end";

//    private final StreamBridge streamBridge;

    private final RabbitTemplate rabbitTemplate;
    private final AIModelFactory aiModelFactory;

    private final AICacheKeyBuilder aiCacheKeyBuilder;

    private final StringRedisTemplate redisTemplate;

    private final EventStreamKeyBuilder eventStreamKeyBuilder;

    // 用于存储会话记录
    private final InMemoryChatMemory chatMemory = new InMemoryChatMemory();

    // 自定义线程池
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    private RetryTemplate retryTemplate;

    @PostConstruct
    private void init() {
        // 初始化 RetryTemplate
        retryTemplate = new RetryTemplate();
        // 设置重试策略
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(2);
        retryTemplate.setRetryPolicy(retryPolicy);

        // 设置退避策略
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(1);
        retryTemplate.setBackOffPolicy(backOffPolicy);
    }

    @Override
    public String chat(ChatRequestDTO chatRequestDTO) {
        String userPrompt = chatRequestDTO.getUserPrompt();
        String systemPrompt = chatRequestDTO.getSystemPrompt();
        String model = chatRequestDTO.getModel();
        String conversationId = chatRequestDTO.getConversationId();
        Integer chatHistoryWindowSize = chatRequestDTO.getChatHistoryWindowSize();
        Integer maxMills = chatRequestDTO.getMaxMills();
        Integer maxSize = chatRequestDTO.getMaxSize();
        EventType eventType = chatRequestDTO.getEventType();
        String userId = RPCContext.getUserId().toString();
        String requestId = EventMessageUtil.generateRequestId();
        executorService.submit(() -> {
            try {
                retryTemplate.execute(new RetryCallback<String, Exception>() {
                    @Override
                    public String doWithRetry(RetryContext context) throws Exception {
                        // 这里是要重试的代码
                        doChat(model, userId, systemPrompt, userPrompt, chatHistoryWindowSize, conversationId, maxSize, maxMills, requestId, eventType);
                        return "ok";
                    }
                }, new RecoveryCallback<String>() {
                    @Override
                    public String recover(RetryContext context) throws Exception {
                        String userToEventStreamKey = eventStreamKeyBuilder.buildUserToEventStreamKey(userId);
                        String routingKey = redisTemplate.opsForValue().get(userToEventStreamKey);
                        sendEndMessageToQueue("该AI模型暂时不可用，请切换模型或稍后再试", requestId, eventType, userId, routingKey);
                        log.error("AI模型重试调用全部失败");
                        return null;
                    }
                });
            } catch (Exception e) {
                log.error("AI模型重试调用异常");
                throw new RuntimeException(e);
            }
        });


        return requestId;
    }

    private void doChat(String model, String userId, String systemPrompt, String userPrompt, Integer chatHistoryWindowSize, String conversationId, Integer maxSize, Integer maxMills, String requestId, EventType eventType) {
        AIModel aiModel = aiModelFactory.getAIModel(model);
        AIModelInstance aiModelInstance = aiModel.getInstance();
        String modelUsageKey = aiCacheKeyBuilder.buildModelUsageKey(Long.parseLong(userId), model);
        boolean decreaseQuotaResult = checkAndDecreaseQuota(modelUsageKey, AIModelFactory.getModelInitialQuota(model));
        // 额度不足，直接返回
        if (!decreaseQuotaResult) {
            sendEndMessageToQueue("该模型的调用额度不足，请完成签到任务获取更多额度", requestId, eventType, userId, null);
            return;
        }
        String routingKey = null;
        try {
            ChatClient chatClient = aiModelInstance.getChatClient();

            String modelName = aiModelInstance.getModelNameInSupplier();
            ChatClient.ChatClientRequestSpec chatClientRequestSpec = ChatClientRequestSpecBuilder
                    .builder()
                    .withOptions(ChatOptions.builder()
                            .model(modelName)
                            .build())
                    .withSystemPrompt(systemPrompt)
                    .withUserPrompt(userPrompt)
                    .withChatMemory(chatMemory)
                    .withChatHistoryWindowSize(chatHistoryWindowSize)
                    .withConversationId(conversationId)
                    .build(chatClient);
            String userToEventStreamKey = eventStreamKeyBuilder.buildUserToEventStreamKey(userId);
            routingKey = redisTemplate.opsForValue().get(userToEventStreamKey);

            Flux<String> result = chatClientRequestSpec.stream().content();
            String finalRoutingKey = routingKey;
            result
                    .bufferTimeout(maxSize, Duration.ofMillis(maxMills))
                    .index()
                    .doOnNext(message -> {
                        sendToQueue(message.getT2(), requestId, eventType, userId,
                                Math.toIntExact(message.getT1()) + 1, finalRoutingKey);
                    })
                    .doOnComplete(() -> sendEndMessageToQueue(null, requestId, eventType, userId, finalRoutingKey))
                    .doOnError((e) -> {
//                        log.error("AI模型调用失败", e);
//                        sendEndMessageToQueue("该AI模型暂时不可用，请切换模型或稍后再试", requestId, eventType, userId, routingKey);
                        throw new ModelCallFailedException(e);
                    })
                    .subscribe();
            aiModel.recordSuccess(aiModelInstance);
        } catch (Exception e) {
//            sendEndMessageToQueue("该AI模型暂时不可用，请切换模型或稍后再试", requestId, eventType, userId, routingKey);
//            log.error("AI模型调用失败", e);
            redisTemplate.opsForValue().increment(modelUsageKey);
            throw new ModelCallFailedException(e);
        }
    }

    @Override
    public String structuredOutput(StructuredOutputRequestDTO structuredOutputRequestDTO) {
        TargetType targetType = structuredOutputRequestDTO.getTargetType();
        String userPromptOriginal = structuredOutputRequestDTO.getUserPrompt();
        String userPrompt = userPromptOriginal.replaceAll("\\{", "\\\\{").replaceAll("\\}", "\\\\}");
        String systemPromptOriginal = structuredOutputRequestDTO.getSystemPrompt();
        String systemPrompt = systemPromptOriginal.replaceAll("\\{", "\\\\{").replaceAll("\\}", "\\\\}");
        String model = structuredOutputRequestDTO.getModel();
        String userId = structuredOutputRequestDTO.getUserId();
        String conversationId = structuredOutputRequestDTO.getConversationId();
        EventType eventType = structuredOutputRequestDTO.getEventType();
        Integer chatHistoryWindowSize = structuredOutputRequestDTO.getChatHistoryWindowSize();
        String requestId = EventMessageUtil.generateRequestId();

        executorService.submit(() -> {
            try {
                retryTemplate.execute(new RetryCallback<String, Exception>() {
                    @Override
                    public String doWithRetry(RetryContext context) throws Exception {
                        // 这里是要重试的代码
                        doStructuredOutput(model, userId, systemPrompt, userPrompt, chatHistoryWindowSize, conversationId, targetType, requestId, eventType);
                        return "ok";
                    }
                }, new RecoveryCallback<String>() {
                    @Override
                    public String recover(RetryContext context) throws Exception {
                        String userToEventStreamKey = eventStreamKeyBuilder.buildUserToEventStreamKey(userId);
                        String routingKey = redisTemplate.opsForValue().get(userToEventStreamKey);
                        sendEndMessageToQueue("该AI模型暂时不可用，请切换模型或稍后再试", requestId, eventType, userId, routingKey);
                        log.error("AI模型重试调用全部失败");
                        return null;
                    }
                });
            } catch (Exception e) {
                log.error("AI模型重试调用异常");
                throw new RuntimeException(e);
            }
        });

        return requestId;
    }

    private boolean checkAndDecreaseQuota(String key, int initialCredit) {
        return (Boolean) redisTemplate.execute(
                RedisScript.of(CHECK_AND_DECR_SCRIPT, Boolean.class),
                Arrays.asList(key, String.valueOf(initialCredit)), // 传递两个参数
                key, String.valueOf(initialCredit) // 传递两个参数
        );
    }

    private void doStructuredOutput(String model, String userId, String systemPrompt, String userPrompt, Integer chatHistoryWindowSize, String conversationId, TargetType targetType, String requestId, EventType eventType) {
        AIModel aiModel = aiModelFactory.getAIModel(model);
        AIModelInstance aiModelInstance = aiModel.getInstance();
        ChatClient chatClient = aiModelInstance.getChatClient();
        String modelName = aiModelInstance.getModelNameInSupplier();
        String userToEventStreamKey = eventStreamKeyBuilder.buildUserToEventStreamKey(userId);
        String routingKey = redisTemplate.opsForValue().get(userToEventStreamKey);
        String modelUsageKey = aiCacheKeyBuilder.buildModelUsageKey(Long.parseLong(userId), model);
        boolean decreaseQuotaResult = checkAndDecreaseQuota(modelUsageKey, AIModelFactory.getModelInitialQuota(model));
        // 额度不足，直接返回
        if (!decreaseQuotaResult) {
            sendEndMessageToQueue("该模型的调用额度不足，请完成签到任务获取更多额度", requestId, eventType, userId, null);
            return;
        }
        try {
            ChatClient.ChatClientRequestSpec chatClientRequestSpec = ChatClientRequestSpecBuilder
                    .builder()
                    .withOptions(ChatOptions.builder()
                            .model(modelName)
                            .build())
                    .withSystemPrompt(systemPrompt)
                    .withUserPrompt(userPrompt)
                    .withChatMemory(chatMemory)
                    .withChatHistoryWindowSize(chatHistoryWindowSize)
                    .withConversationId(conversationId)
                    .build(chatClient);
            Object resultObject = chatClientRequestSpec
                    .call()
                    .entity(targetType.getType());
            sendToQueue(resultObject, requestId, eventType, userId, routingKey);
        } catch (Exception e) {
//                log.error("结构化输出异常", e);
//                sendEndMessageToQueue("该AI模型暂时不可用，请切换模型或稍后再试", requestId, eventType, userId, routingKey);
            // 调用失败不扣费
            redisTemplate.opsForValue().increment(modelUsageKey);
            throw new RuntimeException(e);
        }
    }

    public class ModelCallFailedException extends RuntimeException {
        public ModelCallFailedException(String message) {
            super(message);
        }

        public ModelCallFailedException(Throwable cause) {
            super(cause);
        }
    }

    private void sendToQueue(Object data, String requestId, EventType eventType, String userId, String routingKey) {
        EventMessage eventMessage = EventMessage.builder()
                .userId(userId)
                .eventType(eventType)
                .requestId(requestId)
                .data(data)
                .build();

//        streamBridge.send("eventMessage-out-0", eventMessage);
        rabbitTemplate.convertAndSend(EventStreamMessageConstant.EVENT_STREAM_EXCHANGE, routingKey, eventMessage);

    }

    private void sendToQueue(List<String> data, String requestId, EventType eventType, String userId,
                             Integer sequence, String routingKey) {
        StringBuilder sb = new StringBuilder();
        for (String str : data) {
            sb.append(str);
        }
        String result = sb.toString();
        EventMessage eventMessage = EventMessage.builder()
                .userId(userId)
                .eventType(eventType)
                .requestId(requestId)
                .data(result)
                .sequence(sequence)
                .build();
//        streamBridge.send("eventMessage-out-0", eventMessage);
        rabbitTemplate.convertAndSend(EventStreamMessageConstant.EVENT_STREAM_EXCHANGE, routingKey, eventMessage);
    }

    private void sendEndMessageToQueue(String endMessage, String requestId, EventType eventType, String userId, String routingKey) {
        EventMessage eventMessage = EventMessage.builder()
                .userId(userId)
                .data(endMessage)
                .eventType(eventType)
                .requestId(requestId)
                .sequence(-1)
                .build();
//        streamBridge.send("eventMessage-out-0", eventMessage);
        rabbitTemplate.convertAndSend(EventStreamMessageConstant.EVENT_STREAM_EXCHANGE, routingKey, eventMessage);
    }

    @Override
    public Set<String> getAvailableModels() {
        return AIModelFactory.getAIModelNames().stream().sorted().collect(Collectors.toSet());
    }
}
