package com.qiu.cardflow.ai.service;

import com.qiu.cardflow.ai.dto.ChatRequestDTO;
import com.qiu.cardflow.ai.dto.StructuredOutputRequestDTO;

public interface IAIService {

    String chat(ChatRequestDTO chatRequestDTO);

    String structuredOutput(StructuredOutputRequestDTO structuredOutputRequestDTO);
}
