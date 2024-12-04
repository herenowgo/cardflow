package com.qiu.qoj.ai.service.impl;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.qiu.qoj.common.api.BaseResponse;
import com.qiu.qoj.common.api.UserContext;
import com.qiu.qoj.ai.feign.QuestionSubmitServiceFeign;
import com.qiu.qoj.ai.feign.QuestionSubmitWithTagVO;
import com.qiu.qoj.ai.judge.codesandbox.model.JudgeInfo;
import com.qiu.qoj.ai.manager.AIManage;
import com.qiu.qoj.ai.model.dto.question.JudgeCase;
import com.qiu.qoj.ai.model.entity.Question;
import com.qiu.qoj.ai.model.entity.QuestionSubmit;
import com.qiu.qoj.ai.model.vo.QuestionRecommendation;
import com.qiu.qoj.ai.model.vo.QuestionVOForRecommend;
import com.qiu.qoj.ai.service.AIService;
import com.qiu.qoj.ai.service.QuestionService;
import com.qiu.qoj.ai.service.QuestionSubmitService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private final QuestionSubmitService questionSubmitService;

    private final AIManage aiManage;

    private final QuestionService questionService;


    private final QuestionSubmitServiceFeign questionSubmitServiceFeign;

    /**
     * @param questionSubmitId
     * @param index            从0开始
     * @return
     */
    @Override
    public String generateAlgorithmProblemModificationSuggestion(Long questionSubmitId, Integer index) {
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


        String questionTemplate = String.format("算法题目描述：\n" +
                        "%s\n" +
                        "\n" +
                        "测试用例: \n" +
                        "- 输入:\n%s\n" +
                        "- 预期输出: \n%s" +
                        "\n\n\n" +
                        "用户代码： \n" +
                        "```%s\n" +
                        "%s\n" +
                        "```\n" +
                        "\n" +
                        "运行结果或报错信息：  \n" +
                        "%s\n" +
                        "\n" +
                        "目标：  \n" +
                        "帮助我：  \n" +
                        "- 识别代码中的问题或错误  \n" +
                        "- 提供针对性的修改建议,使得修改后的代码可以通过测试  \n",

                title,
                judgeCaseList.get(index).getInput(),
                judgeCaseList.get(index).getOutput(),
                language,
                code,
                judgeInfoBean.getAnswers().get(index) + judgeInfoBean.getCompileErrorOutput()
        );

//        User loginUser = userService.getLoginUser(httpServletRequest);
        String result = aiManage.chatForSpeech(questionTemplate, "4535243534543525" + questionSubmitId.shortValue() + index);
        return result;
    }

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
