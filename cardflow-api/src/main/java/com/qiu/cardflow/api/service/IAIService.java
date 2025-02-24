package com.qiu.cardflow.api.service;

import java.util.Set;

import com.qiu.cardflow.api.vo.ai.ChatRequest;

public interface IAIService {

    String chat(ChatRequest chatRequest);

    String generateCards(ChatRequest chatRequest);
    
    Set<String> getAvailableModels();
}
