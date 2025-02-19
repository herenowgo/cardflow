package com.qiu.cardflow.ai.service.impl;

import com.qiu.cardflow.ai.client.ChatClientFactory;
import com.qiu.cardflow.ai.dto.ChatRequestDTO;
import com.qiu.cardflow.ai.dto.StructuredOutputRequestDTO;
import com.qiu.cardflow.ai.service.IAIService;
import com.qiu.cardflow.ai.structured.TargetType;
import com.qiu.cardflow.ai.util.ChatClientRequestSpecBuilder;
import com.qiu.cardflow.common.interfaces.exception.Assert;
import com.qiu.codeflow.eventStream.dto.EventMessage;
import com.qiu.codeflow.eventStream.dto.EventType;
import com.qiu.codeflow.eventStream.util.EventMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIServiceImpl implements IAIService {

    private final ChatClientFactory chatClientFactory;

    private final StreamBridge streamBridge;

    // 用于存储会话记录
    private final InMemoryChatMemory chatMemory = new InMemoryChatMemory();

    // 自定义线程池
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);


    @Override
    public String chat(ChatRequestDTO chatRequestDTO) {
        String userPrompt = chatRequestDTO.getUserPrompt();
        String systemPrompt = chatRequestDTO.getSystemPrompt();
        String model = chatRequestDTO.getModel();
        String userId = chatRequestDTO.getUserId();
        String conversationId = chatRequestDTO.getConversationId();
        Integer chatHistoryWindowSize = chatRequestDTO.getChatHistoryWindowSize();
        Integer maxMills = chatRequestDTO.getMaxMills();
        Integer maxSize = chatRequestDTO.getMaxSize();
        EventType eventType = chatRequestDTO.getEventType();

        String requestId = EventMessageUtil.generateRequestId();

        ChatClient chatClient = chatClientFactory.getChatClient(model);
        Assert.notNull(chatClient, "模型不存在");
        String modelName = chatClientFactory.getModelName(model);
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

        try {
            Flux<String> result = chatClientRequestSpec.stream().content();
            result
                    .bufferTimeout(maxSize, Duration.ofMillis(maxMills))
                    .index()
                    .doOnNext(message -> {
                        sendToQueue(message.getT2(), requestId, eventType, userId,
                                Math.toIntExact(message.getT1()) + 1);
                    })
                    .doOnComplete(() -> sendEndMessageToQueue(null, requestId, eventType, userId))
                    .doOnError((e) -> {
                        log.error("AI模型调用失败", e);
                        sendEndMessageToQueue("该AI模型暂时不可用，请切换模型或稍后再试", requestId, eventType, userId);
                    })
                    .subscribe();
        } catch (Exception e) {
            sendEndMessageToQueue("该AI模型暂时不可用，请切换模型或稍后再试", requestId, eventType, userId);
            throw new RuntimeException(e);
        }

        return requestId;
    }


    @Override
    public String structuredOutput(StructuredOutputRequestDTO structuredOutputRequestDTO) {
        TargetType targetType = structuredOutputRequestDTO.getTargetType();
        String userPrompt = structuredOutputRequestDTO.getUserPrompt();
        String systemPrompt = structuredOutputRequestDTO.getSystemPrompt();
        String model = structuredOutputRequestDTO.getModel();
        String userId = structuredOutputRequestDTO.getUserId();
        String conversationId = structuredOutputRequestDTO.getConversationId();
        EventType eventType = structuredOutputRequestDTO.getEventType();
        Integer chatHistoryWindowSize = structuredOutputRequestDTO.getChatHistoryWindowSize();
        String requestId = EventMessageUtil.generateRequestId();

        ChatClient chatClient = chatClientFactory.getChatClient(model);
        Assert.notNull(chatClient, "模型不存在");
        String modelName = chatClientFactory.getModelName(model);
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
                sendToQueue(resultObject, requestId, eventType, userId);
            } catch (Exception e) {
                log.error("结构化输出异常", e);
                sendEndMessageToQueue("该AI模型暂时不可用，请切换模型或稍后再试", requestId, eventType, userId);
                throw new RuntimeException(e);
            }
        });

        return requestId;
    }


    private void sendToQueue(Object data, String requestId, EventType eventType, String userId) {
        EventMessage eventMessage = EventMessage.builder()
                .userId(userId)
                .eventType(eventType)
                .requestId(requestId)
                .data(data)
                .build();

        streamBridge.send("eventMessage-out-0", eventMessage);
    }

    private void sendToQueue(List<String> data, String requestId, EventType eventType, String userId,
                             Integer sequence) {
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
        streamBridge.send("eventMessage-out-0", eventMessage);
    }

    private void sendEndMessageToQueue(String endMessage, String requestId, EventType eventType, String userId) {
        EventMessage eventMessage = EventMessage.builder()
                .userId(userId)
                .data(endMessage)
                .eventType(eventType)
                .requestId(requestId)
                .sequence(-1)
                .build();
        streamBridge.send("eventMessage-out-0", eventMessage);
    }
}
