package com.qiu.cardflow.question.feign;


import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "qoj-question", path = "/api/question")
public interface QuestionService {




}
