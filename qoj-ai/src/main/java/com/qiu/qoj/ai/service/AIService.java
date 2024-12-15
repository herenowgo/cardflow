package com.qiu.qoj.ai.service;

import com.qiu.qoj.ai.model.dto.ai.AIChatRequest;
import com.qiu.qoj.ai.model.vo.QuestionRecommendation;
import jakarta.servlet.http.HttpServletRequest;


public interface AIService {


    String generateTags(AIChatRequest request);

    String generateCards(AIChatRequest request);

    String generateCodeModificationSuggestion(AIChatRequest aiChatRequest, Long questionSubmitId, Integer index);

    QuestionRecommendation generateQuestionRecommendation(String message, HttpServletRequest httpServletRequest);

    String generateStudySuggestion(String message);

    String analyzeUserSubmitRecord();
}
