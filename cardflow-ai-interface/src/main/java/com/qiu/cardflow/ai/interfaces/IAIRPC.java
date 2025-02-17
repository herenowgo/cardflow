package com.qiu.cardflow.ai.interfaces;

import com.qiu.cardflow.ai.dto.ChatRequest;
import com.qiu.cardflow.ai.dto.StructuredOutputRequest;
import com.qiu.cardflow.common.interfaces.exception.BusinessException;

public interface IAIRPC {
    String chat(ChatRequest chatRequest) throws BusinessException;

    String structuredOutput(StructuredOutputRequest structuredOutputRequest) throws BusinessException;
}
