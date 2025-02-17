package com.qiu.cardflow.ai.rpc;

import com.qiu.cardflow.ai.dto.ChatRequest;
import com.qiu.cardflow.ai.dto.StructuredOutputRequest;
import com.qiu.cardflow.ai.interfaces.IAIRPC;
import com.qiu.cardflow.ai.service.IAIService;
import com.qiu.cardflow.ai.util.ContentCleaner;
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
    public String chat(@Valid ChatRequest chatRequest) {
        chatRequest.setUserPrompt(ContentCleaner.cleanHtmlContent(chatRequest.getUserPrompt()));
        return aiService.chat(chatRequest);
    }

    @Override
    public String structuredOutput(@Valid StructuredOutputRequest structuredOutputRequest) {
        structuredOutputRequest.setUserPrompt(ContentCleaner.cleanHtmlContent(structuredOutputRequest.getUserPrompt()));
        return aiService.structuredOutput(structuredOutputRequest);
    }
}
