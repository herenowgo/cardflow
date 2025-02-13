package com.qiu.cardflow.ai.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "qoj-question")
public interface QuestionSubmitServiceFeign {

//    /**
//     * 获取提交记录
//     */
//    @GetMapping("/api/question_submit/list")
//    public BaseResponse<List<QuestionSubmitWithTagVO>> listQuestionSubmit(@RequestParam Integer number) ;
}
