package com.qiu.cardflow.api.service;

import com.qiu.cardflow.api.vo.ai.ChatRequest;

public interface IAIService {

    String chat(ChatRequest chatRequest);

    String generateCards(ChatRequest chatRequest);
}
