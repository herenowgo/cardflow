package com.qiu.qoj.question.feign;


import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "qoj-question", path = "/api/question")
public interface QuestionService {




}
