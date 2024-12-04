package com.qiu.qoj.ai.feign;

import com.qiu.qoj.common.api.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "qoj-question")
public interface QuestionSubmitServiceFeign {

    /**
     * 获取提交记录
     */
    @GetMapping("/api/question_submit/list")
    public BaseResponse<List<QuestionSubmitWithTagVO>> listQuestionSubmit(@RequestParam Integer number) ;
}
