package com.qiu.cardflow.ai.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.qiu.cardflow.ai.client.ChatClientFactory;
import com.qiu.cardflow.ai.constant.AIConstant;
import com.qiu.cardflow.ai.judge.codesandbox.model.JudgeInfo;
import com.qiu.cardflow.ai.model.dto.ai.AIChatRequest;
import com.qiu.cardflow.ai.model.dto.question.JudgeCase;
import com.qiu.cardflow.ai.model.entity.Cards;
import com.qiu.cardflow.ai.model.entity.Question;
import com.qiu.cardflow.ai.model.entity.QuestionSubmit;
import com.qiu.cardflow.ai.model.entity.Tags;
import com.qiu.cardflow.ai.model.enums.AIModel;
import com.qiu.cardflow.ai.service.AIService;
import com.qiu.cardflow.ai.service.QuestionService;
import com.qiu.cardflow.ai.service.QuestionSubmitService;
import com.qiu.cardflow.common.api.UserContext;
import com.qiu.codeflow.eventStream.dto.EventMessage;
import com.qiu.codeflow.eventStream.dto.EventType;
import com.qiu.codeflow.eventStream.util.EventMessageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private final QuestionSubmitService questionSubmitService;

    private final QuestionService questionService;

    private final ChatClientFactory chatClientFactory;

    private final StreamBridge streamBridge;

    // 自定义线程池
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    // 用于存储会话记录
    private final InMemoryChatMemory chatMemory = new InMemoryChatMemory();

    private static final int DEFAULT_WINDOW_SIZE = 10;

    /**
     * 强制使用flash模型
     *
     * @param request
     * @return
     */
    @Override
    public String generateTags(AIChatRequest request) {
        AIModel aiModel = AIModel.getByVO(request.getModel());
        ChatClient client = chatClientFactory.getClient(aiModel);
        String requestId = EventMessageUtil.generateRequestId();
        String userId = UserContext.getUserId().toString();

        String sanitizedContent = sanitizeUserPrompt(request.getContent());

        // 异步执行 AI 生成内容和发送消息的操作
        executorService.submit(() -> {
            try {
                Tags tags = client.prompt()
                        .options(ChatOptions.builder().model(
                                aiModel.getName()).build())
                        .system(AIConstant.GENERATE_TAGS_SYSTEM_PROMPT.render())
                        .user(sanitizedContent)
                        .call()
                        .entity(Tags.class);

                if (tags == null) {
                    tags = new Tags();
                }

                sendToQueue(tags.getTags(), requestId, EventType.TAGS, userId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return requestId;
    }

    @Override
    public String generateCards(AIChatRequest request) {
        AIModel aiModel = AIModel.getByVO(request.getModel());
        ChatClient client = chatClientFactory.getClient(aiModel);
        String requestId = EventMessageUtil.generateRequestId();
        String userId = UserContext.getUserId().toString();
        String sanitizedContent = sanitizeUserPrompt(request.getContent());

        executorService.submit(() -> {
            try {
                Cards cards = client.prompt()
                        .options(ChatOptions.builder().model(aiModel.getName()).build())
                        .system(AIConstant.GENERATE_CARDS_SYSTEM_PROMPT)
                        .user(sanitizedContent)
                        .call()
                        .entity(Cards.class);

                sendToQueue(cards, requestId, EventType.CARDS_GENERATE, userId);

            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        return requestId;
    }

    /**
     * @param questionSubmitId
     * @param index            从0开始
     * @return
     */
    @Override
    public String generateCodeModificationSuggestion(AIChatRequest aiChatRequest, Long questionSubmitId,
            Integer index) {
        if (aiChatRequest == null) {
            aiChatRequest = new AIChatRequest();
        }
        AIModel aiModel = AIModel.getByVO(aiChatRequest.getModel());
        ChatClient client = chatClientFactory.getClient(aiModel);
        String requestId = EventMessageUtil.generateRequestId();
        String userId = UserContext.getUserId().toString();

        executorService.submit(() -> {
            try {
                QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
                Long questionId = questionSubmit.getQuestionId();
                Question question = questionService.getById(questionId);

                String language = questionSubmit.getLanguage();
                String code = questionSubmit.getCode();
                String judgeInfo = questionSubmit.getJudgeInfo();
                JudgeInfo judgeInfoBean = JSONUtil.toBean(judgeInfo, JudgeInfo.class);
                String title = question.getTitle();
                String judgeCase = question.getJudgeCase();
                List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCase, JudgeCase.class);

                Map<String, Object> userPromptMap = new HashMap<>();
                userPromptMap.put("question", title + "\n" + question.getContent());
                userPromptMap.put("input", judgeCaseList.get(index).getInput());
                userPromptMap.put("expectedOutput", judgeCaseList.get(index).getOutput());
                userPromptMap.put("code", code);
                userPromptMap.put("language", language);
                userPromptMap.put("actualRunOutput",
                        judgeInfoBean.getRunOutput().get(index) + judgeInfoBean.getCompileErrorOutput());

                Flux<String> result = client.prompt()
                        .system(AIConstant.ALGORITHM_PROBLEM_CODE_MODIFICATION_SUGGESTION_SYSTEM_PROMPT_TEMPLATE
                                .render())
                        .user(AIConstant.ALGORITHM_PROBLEM_CODE_MODIFICATION_SUGGESTION_USER_PROMPT_TEMPLATE
                                .render(userPromptMap))
                        .stream()
                        .content();

                result
                        .bufferTimeout(15, Duration.ofSeconds(1))
                        .index()
                        .doOnNext(message -> {
                            sendToQueue(message.getT2(), requestId, EventType.CODE_SUGGEST, userId,
                                    Math.toIntExact(message.getT1()) + 1);
                        }) // 对每个接收到的元素调用sendToQueue方法
                        .doOnComplete(() -> sendEndMessageToQueue(requestId, EventType.CODE_SUGGEST, userId))
                        .blockLast(); // 启动流的消费
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return requestId;
    }

    @Override
    public String chat(AIChatRequest request) {
        AIModel aiModel = AIModel.getByVO(request.getModel());
        ChatClient client = chatClientFactory.getClient(aiModel);
        String requestId = EventMessageUtil.generateRequestId();
        String userId = UserContext.getUserId().toString();
        String sessionId = UserContext.getUserId() + request.getSessionId().substring(0, 10);
        String systemPrompt = StrUtil.isBlank(request.getPrompt())
                ? AIConstant.CARD_CHECK_SYSTEM_PROMPT_TEMPLATE.render()
                : request.getPrompt();

        String sanitizedContent = sanitizeUserPrompt(request.getContent());
                
        executorService.submit(() -> {
            try {
                // 创建记忆顾问，设置窗口大小
                MessageChatMemoryAdvisor memoryAdvisor = new MessageChatMemoryAdvisor(
                        chatMemory,
                        sessionId,
                        DEFAULT_WINDOW_SIZE);
                // 使用流式响应
                Flux<String> result = client.prompt()
                        .options(ChatOptions.builder()
                                .model(aiModel.getName())
                                .build())
                        .advisors(memoryAdvisor)
                        .system(systemPrompt)
                        .user(sanitizedContent)
                        .stream()
                        .content();

                result
                        .bufferTimeout(15, Duration.ofSeconds(1))
                        .index()
                        .doOnNext(message -> {
                            sendToQueue(message.getT2(), requestId, EventType.ANSWER, userId,
                                    Math.toIntExact(message.getT1()) + 1);
                        })
                        .doOnComplete(() -> sendEndMessageToQueue(requestId, EventType.ANSWER, userId))
                        .subscribe();

            } catch (Exception e) {
                e.printStackTrace();
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

    private void sendEndMessageToQueue(String requestId, EventType eventType, String userId) {
        EventMessage eventMessage = EventMessage.builder()
                .userId(userId)
                .eventType(eventType)
                .requestId(requestId)
                .sequence(-1)
                .build();
        streamBridge.send("eventMessage-out-0", eventMessage);
    }

    /**
     * 处理用户输入内容，使其可以安全地用于 PromptTemplate
     */
    private String sanitizeUserPrompt(String content) {
        if (content == null) {
            return "";
        }

        return content
                // 1. 处理HTML实体
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                // 2. 转义 PromptTemplate 特殊字符
                .replace("\\", "\\\\") // 必须先处理反斜杠
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace("$", "\\$")
                .replace("#", "\\#")
                // 3. 保持换行符
                .replace("\r\n", "\n")
                .replace("\r", "\n");
    }
}
