package com.qiu.qoj.ai.service.impl;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.qiu.qoj.ai.client.AIClientFactory;
import com.qiu.qoj.ai.constant.AIConstant;
import com.qiu.qoj.ai.feign.QuestionSubmitServiceFeign;
import com.qiu.qoj.ai.feign.QuestionSubmitWithTagVO;
import com.qiu.qoj.ai.judge.codesandbox.model.JudgeInfo;
import com.qiu.qoj.ai.manager.AIManage;
import com.qiu.qoj.ai.model.dto.ai.AIChatRequest;
import com.qiu.qoj.ai.model.dto.question.JudgeCase;
import com.qiu.qoj.ai.model.entity.Cards;
import com.qiu.qoj.ai.model.entity.Question;
import com.qiu.qoj.ai.model.entity.QuestionSubmit;
import com.qiu.qoj.ai.model.entity.Tags;
import com.qiu.qoj.ai.model.enums.AIModel;
import com.qiu.qoj.ai.model.vo.QuestionRecommendation;
import com.qiu.qoj.ai.model.vo.QuestionVOForRecommend;
import com.qiu.qoj.ai.service.AIService;
import com.qiu.qoj.ai.service.QuestionService;
import com.qiu.qoj.ai.service.QuestionSubmitService;
import com.qiu.qoj.common.api.BaseResponse;
import com.qiu.qoj.common.api.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import work.codeflow.eventStream.dto.EventMessage;
import work.codeflow.eventStream.dto.EventType;
import work.codeflow.eventStream.util.EventMessageUtil;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private final QuestionSubmitService questionSubmitService;

    private final AIManage aiManage;

    private final QuestionService questionService;


    private final QuestionSubmitServiceFeign questionSubmitServiceFeign;

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


    private void sendToQueue(Object data, String requestId, EventType eventType, String userId) {
        EventMessage eventMessage = EventMessage.builder()
                .userId(userId)
                .eventType(eventType)
                .requestId(requestId)
                .data(data)
                .build();

        streamBridge.send("aiResult-out-0", eventMessage);
    }

    private void sendToQueue(List<String> data, String requestId, EventType eventType, String userId, Integer sequence) {
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
     * @param questionSubmitId
     * @param index            从0开始
     * @return
     */
    @Override
    public String generateCodeModificationSuggestion(AIChatRequest aiChatRequest, Long questionSubmitId, Integer index) {
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
                userPromptMap.put("actualRunOutput", judgeInfoBean.getRunOutput().get(index) + judgeInfoBean.getCompileErrorOutput());

//                String render = AIConstant.ALGORITHM_PROBLEM_CODE_MODIFICATION_SUGGESTION_SYSTEM_PROMPT_TEMPLATE.render();
//                String render1 = AIConstant.ALGORITHM_PROBLEM_CODE_MODIFICATION_SUGGESTION_USER_PROMPT_TEMPLATE.render(userPromptMap);

                Flux<String> result = client.prompt()
                        .system(AIConstant.ALGORITHM_PROBLEM_CODE_MODIFICATION_SUGGESTION_SYSTEM_PROMPT_TEMPLATE.render())
                        .user(AIConstant.ALGORITHM_PROBLEM_CODE_MODIFICATION_SUGGESTION_USER_PROMPT_TEMPLATE.render(userPromptMap))
                        .stream()
                        .content();

                result
                        .bufferTimeout(15, Duration.ofSeconds(1))
                        .index()
                        .doOnNext(message -> {
                            sendToQueue(message.getT2(), requestId, EventType.CODE_SUGGEST, userId, Math.toIntExact(message.getT1()) + 1);
                        }) // 对每个接收到的元素调用sendToQueue方法
                        .doOnComplete(() -> sendEndMessageToQueue(requestId, EventType.CODE_SUGGEST, userId))
                        .blockLast(); // 启动流的消费
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


        return requestId;
    }


//    /**
//     * @param questionSubmitId
//     * @param index            从0开始
//     * @return
//     */
//    @Override
//    public String generateAlgorithmProblemModificationSuggestion(Long questionSubmitId, Integer index) {
//        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
//        Long questionId = questionSubmit.getQuestionId();
//        Question question = questionService.getById(questionId);
//
//        String language = questionSubmit.getLanguage();
//        String code = questionSubmit.getCode();
//        String judgeInfo = questionSubmit.getJudgeInfo();
//        JudgeInfo judgeInfoBean = JSONUtil.toBean(judgeInfo, JudgeInfo.class);
//
//        String title = question.getTitle();
//        String judgeCase = question.getJudgeCase();
//
//
//        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCase, JudgeCase.class);
//
//
//        String questionTemplate = String.format("算法题目描述：\n" +
//                        "%s\n" +
//                        "\n" +
//                        "测试用例: \n" +
//                        "- 输入:\n%s\n" +
//                        "- 预期输出: \n%s" +
//                        "\n\n\n" +
//                        "用户代码： \n" +
//                        "```%s\n" +
//                        "%s\n" +
//                        "```\n" +
//                        "\n" +
//                        "运行结果或报错信息：  \n" +
//                        "%s\n" +
//                        "\n" +
//                        "目标：  \n" +
//                        "帮助我：  \n" +
//                        "- 识别代码中的问题或错误  \n" +
//                        "- 提供针对性的修改建议,使得修改后的代码可以通过测试  \n",
//
//                title,
//                judgeCaseList.get(index).getInput(),
//                judgeCaseList.get(index).getOutput(),
//                language,
//                code,
//                judgeInfoBean.getAnswers().get(index) + judgeInfoBean.getCompileErrorOutput()
//        );
//

    /// /        User loginUser = userService.getLoginUser(httpServletRequest);
//        String result = aiManage.chatForSpeech(questionTemplate, "4535243534543525" + questionSubmitId.shortValue() + index);
//        return result;
//    }
    @Override
    public QuestionRecommendation generateQuestionRecommendation(String message, HttpServletRequest httpServletRequest) {
        String recommendation = aiManage.chatWithKnowledgeBase(message, "000000" + UserContext.getUserId().toString(), "1845342976004509696");
        int firstIndex = recommendation.indexOf("ID:::");
        int lastIndex = recommendation.lastIndexOf("ID:::");
        String ids = recommendation.substring(firstIndex + 5, lastIndex);
        String[] idArray = ids.split("，");
        List<QuestionVOForRecommend> questions = questionService.listByIds(Arrays.asList(idArray)).stream()
                .map(QuestionVOForRecommend::objToVo)
                .collect(Collectors.toList());
        return new QuestionRecommendation("", questions);
    }

    @Override
    public String generateStudySuggestion(String message) {
        return aiManage.chatForSpeech(message, "666666000000" + UserContext.getUserId().toString());
    }

    @Override
    public String analyzeUserSubmitRecord() {
        BaseResponse<List<QuestionSubmitWithTagVO>> baseResponse = questionSubmitServiceFeign.listQuestionSubmit(100);
        List<QuestionSubmitWithTagVO> data = baseResponse.getData();
        // 使用jackson-dataformat-csv将List<QuestionSubmitWithTagVO>转换为csv格式字符串，头部为QuestionSubmitWithTagVO里的字段
        CsvMapper csvMapper = new CsvMapper();
        CsvSchema schema = csvMapper.schemaFor(QuestionSubmitWithTagVO.class).withHeader();
        String csv = null;
        try {
            csv = csvMapper.writer(schema).writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        String result = aiManage.chatForDataAnalysis(csv, "666666000000" + UserContext.getUserId().toString());

        return result;
    }
}
