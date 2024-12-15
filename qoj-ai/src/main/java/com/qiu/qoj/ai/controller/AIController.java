package com.qiu.qoj.ai.controller;


import com.qiu.qoj.ai.model.vo.QuestionRecommendation;
import com.qiu.qoj.ai.service.AIService;
import com.qiu.qoj.common.api.BaseResponse;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


//@RestController
//@RequestMapping("/ai")
//@Slf4j
public class AIController {

    @Resource
    private AIService aiService;


    /**
     * 根据题目的提交ID和测试用例的序号，分析用户代码中的错误，并且生成修改建议
     *
     * @param questionSubmitId
     * @param index            测试用例的序号（从0开始）
     * @return
     */
    @PostMapping("/analyzeError")
    public BaseResponse<String> analyzeError(@RequestParam Long questionSubmitId, @RequestParam Integer index) {
//        String s = aiService.generateAlgorithmProblemModificationSuggestion(questionSubmitId, index);
        return BaseResponse.success("");
    }


    @PostMapping("/recommendQuestion")
    public BaseResponse<QuestionRecommendation> recommendQuestion(String message, HttpServletRequest httpServletRequest) {
        return BaseResponse.success(aiService.generateQuestionRecommendation(message, httpServletRequest));
    }


    @PostMapping("/suggest")
    public BaseResponse<String> getStudySuggestion(String message, HttpServletRequest httpServletRequest) {
        return BaseResponse.success(aiService.generateStudySuggestion(message));
    }

    /**
     * 分析用户提交记录
     */
    @PostMapping("/analyzeUserSubmitRecord")
    public BaseResponse<String> analyzeUserSubmitRecord() {
        return BaseResponse.success(aiService.analyzeUserSubmitRecord());
    }


}
