package com.qiu.cardflow.discussion.service;

import com.qiu.cardflow.discussion.model.vo.QuestionRecommendation;
import jakarta.servlet.http.HttpServletRequest;


public interface AIService {


    String generateAlgorithmProblemModificationSuggestion(Long questionSubmitId, Integer index);

    QuestionRecommendation generateQuestionRecommendation(String message, HttpServletRequest httpServletRequest);
}
