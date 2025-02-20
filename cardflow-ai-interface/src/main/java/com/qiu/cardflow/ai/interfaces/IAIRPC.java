package com.qiu.cardflow.ai.interfaces;

import com.qiu.cardflow.ai.dto.ChatRequestDTO;
import com.qiu.cardflow.ai.dto.StructuredOutputRequestDTO;
import com.qiu.cardflow.common.interfaces.exception.BusinessException;
import com.qiu.cardflow.common.interfaces.exception.RPC;
import jakarta.validation.Valid;

public interface IAIRPC extends RPC {
    String chat(@Valid ChatRequestDTO chatRequestDTO) throws BusinessException;

    String structuredOutput(@Valid StructuredOutputRequestDTO structuredOutputRequestDTO) throws BusinessException;
}
