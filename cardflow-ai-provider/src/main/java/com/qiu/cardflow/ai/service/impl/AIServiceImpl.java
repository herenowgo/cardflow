package com.qiu.cardflow.ai.service.impl;

import com.qiu.cardflow.ai.dto.ChatRequestDTO;
import com.qiu.cardflow.ai.dto.StructuredOutputRequestDTO;
import com.qiu.cardflow.ai.model.AIModelFactory;
import com.qiu.cardflow.ai.model.AIModelInstance;
import com.qiu.cardflow.ai.service.IAIService;
import com.qiu.cardflow.ai.structured.TargetType;
import com.qiu.cardflow.ai.util.ChatClientRequestSpecBuilder;
import com.qiu.cardflow.common.interfaces.exception.Assert;
import com.qiu.cardflow.redis.starter.key.AICacheKeyBuilder;
import com.qiu.cardflow.redis.starter.key.EventStreamKeyBuilder;
import com.qiu.cardflow.rpc.starter.RPCContext;
import com.qiu.codeflow.eventStream.dto.EventMessage;
import com.qiu.codeflow.eventStream.dto.EventType;
import com.qiu.codeflow.eventStream.message.EventStreamMessageConstant;
import com.qiu.codeflow.eventStream.util.EventMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
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

        AIModelInstance aiModelInstance = aiModelFactory.getAIModelInstance(model);

        String modelUsageKey = aiCacheKeyBuilder.buildModelUsageKey(Long.parseLong(userId), model);
        boolean decreaseQuotaResult = checkAndDecreaseQuota(modelUsageKey, AIModelFactory.getModelInitialQuota(model));

        Assert.isTrue(decreaseQuotaResult, "该模型的调用额度不足，请完成签到任务获取更多额度");

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
        String routingKey = redisTemplate.opsForValue().get(userToEventStreamKey);
        try {

            Flux<String> result = chatClientRequestSpec.stream().content();
            result
                    .bufferTimeout(maxSize, Duration.ofMillis(maxMills))
                    .index()
                    .doOnNext(message -> {
                        sendToQueue(message.getT2(), requestId, eventType, userId,
                                Math.toIntExact(message.getT1()) + 1, routingKey);
                    })
                    .doOnComplete(() -> sendEndMessageToQueue(null, requestId, eventType, userId, routingKey))
                    .doOnError((e) -> {
                        log.error("AI模型调用失败", e);
                        sendEndMessageToQueue("该AI模型暂时不可用，请切换模型或稍后再试", requestId, eventType, userId, routingKey);
                    })
                    .subscribe();
        } catch (Exception e) {
            sendEndMessageToQueue("该AI模型暂时不可用，请切换模型或稍后再试", requestId, eventType, userId, routingKey);
            throw new RuntimeException(e);
        }

        return requestId;
    }

    private boolean checkAndDecreaseQuota(String key, int initialCredit) {
        return (Boolean) redisTemplate.execute(
                RedisScript.of(CHECK_AND_DECR_SCRIPT, Boolean.class),
                Arrays.asList(key, String.valueOf(initialCredit)), // 传递两个参数
                key, String.valueOf(initialCredit) // 传递两个参数
        );
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

        AIModelInstance aiModelInstance = aiModelFactory.getAIModelInstance(model);
        ChatClient chatClient = aiModelInstance.getChatClient();
        String modelName = aiModelInstance.getModelNameInSupplier();
        String userToEventStreamKey = eventStreamKeyBuilder.buildUserToEventStreamKey(userId);
        String routingKey = redisTemplate.opsForValue().get(userToEventStreamKey);
        executorService.submit(() -> {
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
                log.error("结构化输出异常", e);
                sendEndMessageToQueue("该AI模型暂时不可用，请切换模型或稍后再试", requestId, eventType, userId, routingKey);
                throw new RuntimeException(e);
            }
        });

        return requestId;
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
