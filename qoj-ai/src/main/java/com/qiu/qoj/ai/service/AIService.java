package com.qiu.qoj.ai.service;

import com.qiu.qoj.ai.model.dto.ai.AIChatRequest;

public interface AIService {

    String generateTags(AIChatRequest request);

    String generateCards(AIChatRequest request);

    String generateCodeModificationSuggestion(AIChatRequest aiChatRequest, Long questionSubmitId, Integer index);
}
