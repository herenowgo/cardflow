package com.qiu.qoj.discussion.service;

import com.qiu.qoj.discussion.model.vo.QuestionRecommendation;
import jakarta.servlet.http.HttpServletRequest;


public interface AIService {


    String generateAlgorithmProblemModificationSuggestion(Long questionSubmitId, Integer index);

    QuestionRecommendation generateQuestionRecommendation(String message, HttpServletRequest httpServletRequest);
}
