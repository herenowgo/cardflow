package com.qiu.cardflow.ai.service;

import com.qiu.cardflow.ai.dto.ChatRequest;
import com.qiu.cardflow.ai.dto.StructuredOutputRequest;

public interface IAIService {

    String chat(ChatRequest chatRequest);

    String structuredOutput(StructuredOutputRequest structuredOutputRequest);
}
