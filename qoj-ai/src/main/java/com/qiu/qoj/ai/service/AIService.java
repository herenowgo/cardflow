package com.qiu.qoj.ai.service;

import com.qiu.qoj.ai.model.dto.ai.AIChatRequest;
import com.qiu.qoj.ai.model.entity.Cards;
import com.qiu.qoj.ai.model.entity.Tags;
import com.qiu.qoj.ai.model.vo.QuestionRecommendation;
import jakarta.servlet.http.HttpServletRequest;


public interface AIService {


    Tags generateTags(AIChatRequest request);

    Cards generateCards(AIChatRequest request);

    String generateAlgorithmProblemModificationSuggestion(Long questionSubmitId, Integer index);

    QuestionRecommendation generateQuestionRecommendation(String message, HttpServletRequest httpServletRequest);

    String generateStudySuggestion(String message);

    String analyzeUserSubmitRecord();
}
