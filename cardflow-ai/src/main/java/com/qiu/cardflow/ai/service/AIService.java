package com.qiu.cardflow.ai.service;

import com.qiu.cardflow.ai.model.dto.ai.AIChatRequest;

public interface AIService {

    String generateTags(AIChatRequest request);

    String generateCards(AIChatRequest request);

    String generateCodeModificationSuggestion(AIChatRequest aiChatRequest, Long questionSubmitId, Integer index);

    String chat(AIChatRequest request);
}
