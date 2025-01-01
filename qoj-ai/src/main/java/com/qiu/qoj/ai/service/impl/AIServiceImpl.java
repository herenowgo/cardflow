package com.qiu.qoj.ai.service.impl;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import com.qiu.qoj.ai.client.AIClientFactory;
import com.qiu.qoj.ai.constant.AIConstant;
import com.qiu.qoj.ai.judge.codesandbox.model.JudgeInfo;
import com.qiu.qoj.ai.model.dto.ai.AIChatRequest;
import com.qiu.qoj.ai.model.dto.question.JudgeCase;
import com.qiu.qoj.ai.model.entity.Cards;
import com.qiu.qoj.ai.model.entity.Question;
import com.qiu.qoj.ai.model.entity.QuestionSubmit;
import com.qiu.qoj.ai.model.entity.Tags;
import com.qiu.qoj.ai.model.enums.AIModel;
import com.qiu.qoj.ai.service.AIService;
import com.qiu.qoj.ai.service.QuestionService;
import com.qiu.qoj.ai.service.QuestionSubmitService;
import com.qiu.qoj.common.api.UserContext;

import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import work.codeflow.eventStream.dto.EventMessage;
import work.codeflow.eventStream.dto.EventType;
import work.codeflow.eventStream.util.EventMessageUtil;

@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private final QuestionSubmitService questionSubmitService;

    private final QuestionService questionService;

    private final AIClientFactory aiClientFactory;

    private final StreamBridge streamBridge;

    // 自定义线程池
    private final ExecutorService executorService = Executors.newCachedThreadPool();


    /**
     * 强制使用flash模型
     *
     * @param request
     * @return
     */
    @Override
    public String generateTags(AIChatRequest request) {
        ChatClient client = aiClientFactory.getClient(AIModel.GLM_4_Flash);
        String requestId = EventMessageUtil.generateRequestId();
        String userId = UserContext.getUserId().toString();
        // 异步执行 AI 生成内容和发送消息的操作
        executorService.submit(() -> {
            try {
                Tags tags = client.prompt()
                        .options(ChatOptions.builder().model(AIModel.GLM_4_Flash.getName()).build())
                        .system(AIConstant.GENERATE_TAGS_SYSTEM_PROMPT)
                        .user(request.getContent())
                        .call()
                        .entity(Tags.class);

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
        ChatClient client = aiClientFactory.getClient(aiModel);
        String requestId = EventMessageUtil.generateRequestId();
        String userId = UserContext.getUserId().toString();

        executorService.submit(() -> {
            try {
                Cards cards = client.prompt()
                        .options(ChatOptions.builder().model(aiModel.getName()).build())
                        .system(AIConstant.GENERATE_CARDS_SYSTEM_PROMPT)
                        .user(request.getContent())
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
        ChatClient client = aiClientFactory.getClient(aiModel);
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

    private void sendToQueue(Object data, String requestId, EventType eventType, String userId) {
        EventMessage eventMessage = EventMessage.builder()
                .userId(userId)
                .eventType(eventType)
                .requestId(requestId)
                .data(data)
                .build();

        streamBridge.send("aiResult-out-0", eventMessage);
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
