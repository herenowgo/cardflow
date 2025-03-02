package com.qiu.cardflow.ai.service;

import java.util.Set;

import com.qiu.cardflow.ai.dto.ChatRequestDTO;
import com.qiu.cardflow.ai.dto.StructuredOutputRequestDTO;

public interface IAIService {

    String chat(ChatRequestDTO chatRequestDTO);

    String structuredOutput(StructuredOutputRequestDTO structuredOutputRequestDTO);

    Set<String> getAvailableModels();
}
