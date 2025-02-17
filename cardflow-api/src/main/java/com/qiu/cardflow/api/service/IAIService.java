package com.qiu.cardflow.api.service;

import com.qiu.cardflow.ai.dto.StructuredOutputRequestDTO;
import com.qiu.cardflow.api.vo.ai.ChatRequest;

public interface IAIService {

    String chat(ChatRequest chatRequest);

    String structuredOutput(StructuredOutputRequestDTO structuredOutputRequestDTO);

    String generateCards(ChatRequest chatRequest);
}
