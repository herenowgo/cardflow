package com.qiu.cardflow.ai.rpc;

import com.qiu.cardflow.ai.dto.ChatRequestDTO;
import com.qiu.cardflow.ai.dto.StructuredOutputRequestDTO;
import com.qiu.cardflow.ai.interfaces.IAIRPC;
import com.qiu.cardflow.ai.service.IAIService;
import com.qiu.cardflow.ai.util.ContentCleaner;
import com.qiu.cardflow.common.interfaces.exception.BusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

@DubboService
@Controller
@RequiredArgsConstructor
@Validated
public class AIRPCImpl implements IAIRPC {

    private final IAIService aiService;

    @Override
    public String chat(@Valid ChatRequestDTO chatRequestDTO) {
        chatRequestDTO.setUserPrompt(ContentCleaner.cleanHtmlContent(chatRequestDTO.getUserPrompt()));
        return aiService.chat(chatRequestDTO);
    }

    @Override
    public String structuredOutput(@Valid StructuredOutputRequestDTO structuredOutputRequestDTO) throws BusinessException {
        structuredOutputRequestDTO.setUserPrompt(ContentCleaner.cleanHtmlContent(structuredOutputRequestDTO.getUserPrompt()));
        return aiService.structuredOutput(structuredOutputRequestDTO);
    }
}
