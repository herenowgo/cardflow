package com.qiu.cardflow.ai.service.impl;

import cn.hutool.core.util.StrUtil;
import com.qiu.cardflow.ai.client.ChatClientFactory;
import com.qiu.cardflow.ai.dto.ChatRequest;
import com.qiu.cardflow.ai.dto.StructuredOutputRequest;
import com.qiu.cardflow.ai.service.IAIService;
import com.qiu.cardflow.common.interfaces.exception.Assert;
import com.qiu.codeflow.eventStream.dto.EventMessage;
import com.qiu.codeflow.eventStream.dto.EventType;
import com.qiu.codeflow.eventStream.util.EventMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
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
    public String chat(ChatRequest chatRequest) {
        String userPrompt = chatRequest.getUserPrompt();
        String systemPrompt = chatRequest.getSystemPrompt();
        String model = chatRequest.getModel();
        String userId = chatRequest.getUserId();
        String conversationId = chatRequest.getConversationId();
        Integer chatHistoryWindowSize = chatRequest.getChatHistoryWindowSize();
        Integer maxMills = chatRequest.getMaxMills();
        Integer maxSize = chatRequest.getMaxSize();
        EventType eventType = chatRequest.getEventType();

        String requestId = EventMessageUtil.generateRequestId();

        ChatClient chatClient = chatClientFactory.getChatClient(model);
        Assert.notNull(chatClient, "模型不存在");

        Flux<String> result = null;
        if (StrUtil.isNotEmpty(conversationId)) {
            result = getResultWithChatMemory(conversationId, chatHistoryWindowSize, chatClient, model, systemPrompt, userPrompt);
        } else {
            result = chatClient.prompt()
                    .options(ChatOptions.builder()
                            .model(model)
                            .build())
                    .system(systemPrompt)
                    .user(userPrompt)
                    .stream()
                    .content();
        }

        result
                .bufferTimeout(maxSize, Duration.ofMillis(maxMills))
                .index()
                .doOnNext(message -> {
                    sendToQueue(message.getT2(), requestId, eventType, userId,
                            Math.toIntExact(message.getT1()) + 1);
                })
                .doOnComplete(() -> sendEndMessageToQueue(requestId, eventType, userId))
                .subscribe();

        return requestId;
    }

    @NotNull
    private Flux<String> getResultWithChatMemory(String conversationId, Integer chatHistoryWindowSize, ChatClient chatClient, String model, String systemPrompt, String userPrompt) {
        Flux<String> result;
        MessageChatMemoryAdvisor memoryAdvisor = new MessageChatMemoryAdvisor(
                chatMemory,
                conversationId,
                chatHistoryWindowSize);

        result = chatClient.prompt()
                .options(ChatOptions.builder()
                        .model(model)
                        .build())
                .advisors(memoryAdvisor)
                .system(systemPrompt)
                .user(userPrompt)
                .stream()
                .content();
        return result;
    }


    @Override
    public String structuredOutput(StructuredOutputRequest structuredOutputRequest) {
        Class type = structuredOutputRequest.getTargetType();
        String userPrompt = structuredOutputRequest.getUserPrompt();
        String systemPrompt = structuredOutputRequest.getSystemPrompt();
        String model = structuredOutputRequest.getModel();
        String userId = structuredOutputRequest.getUserId();
        String conversationId = structuredOutputRequest.getConversationId();
        EventType eventType = structuredOutputRequest.getEventType();
        Integer chatHistoryWindowSize = structuredOutputRequest.getChatHistoryWindowSize();

        String requestId = EventMessageUtil.generateRequestId();

        ChatClient chatClient = chatClientFactory.getChatClient(model);
        Assert.notNull(chatClient, "模型不存在");

        executorService.submit(() -> {
            getStructuredResultAndSendToQueue(conversationId, chatHistoryWindowSize, chatClient, model, systemPrompt, userPrompt, type, requestId, eventType, userId);
        });

        return requestId;
    }

    private void getStructuredResultAndSendToQueue(String conversationId, Integer chatHistoryWindowSize, ChatClient chatClient, String model, String systemPrompt, String userPrompt, Class type, String requestId, EventType eventType, String userId) {
        try {
            Object resultObject = null;
            if (StrUtil.isNotEmpty(conversationId)) {
                MessageChatMemoryAdvisor memoryAdvisor = new MessageChatMemoryAdvisor(
                        chatMemory,
                        conversationId,
                        chatHistoryWindowSize);

                resultObject = chatClient.prompt()
                        .options(ChatOptions.builder()
                                .model(model)
                                .build())
                        .advisors(memoryAdvisor)
                        .system(systemPrompt)
                        .user(userPrompt)
                        .call()
                        .entity(type);
            } else {
                resultObject = chatClient.prompt()
                        .options(ChatOptions.builder()
                                .model(model)
                                .build())
                        .system(systemPrompt)
                        .user(userPrompt)
                        .call()
                        .entity(type);
            }

            sendToQueue(resultObject, requestId, eventType, userId);
        } catch (Exception e) {
            log.error("结构化输出处理失败", e);
            throw new RuntimeException(e);
        }
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

    private void sendEndMessageToQueue(String requestId, EventType eventType, String userId) {
        EventMessage eventMessage = EventMessage.builder()
                .userId(userId)
                .eventType(eventType)
                .requestId(requestId)
                .sequence(-1)
                .build();
        streamBridge.send("eventMessage-out-0", eventMessage);
    }
}
