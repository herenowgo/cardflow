package com.qiu.cardflow.ai.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qiu.cardflow.ai.model.dto.ai.AIChatRequest;
import com.qiu.cardflow.ai.model.dto.ai.UserCodeAnalysisRequest;
import com.qiu.cardflow.ai.service.AIService;
import com.qiu.cardflow.ai.utils.ContentCleaner;
import com.qiu.cardflow.common.api.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class ChatController {

    private final AIService aiService;

    @PostMapping("/tags")
    public BaseResponse<String> getTags(@RequestBody AIChatRequest aiChatRequest) {
        aiChatRequest.setContent(ContentCleaner.cleanHtmlContent(aiChatRequest.getContent()));
        return BaseResponse.success(aiService.generateTags(aiChatRequest));
    }

    @PostMapping("/cards")
    public BaseResponse<String> getCards(@RequestBody AIChatRequest aiChatRequest) {
        aiChatRequest.setContent(ContentCleaner.cleanHtmlContent(aiChatRequest.getContent()));
        return BaseResponse.success(aiService.generateCards(aiChatRequest));
    }

    @PostMapping("/codeAnalysis")
    public BaseResponse<String> analysisUserCode(@RequestBody UserCodeAnalysisRequest userCodeAnalysisRequest) {
        String requestId = aiService.generateCodeModificationSuggestion(userCodeAnalysisRequest.getAiChatRequest(),
                userCodeAnalysisRequest.getQuestionSubmitId(), userCodeAnalysisRequest.getIndex());

        return BaseResponse.success(requestId);
    }

    /**
     * 与AI进行对话
     * 
     * @param aiChatRequest AI对话请求参数
     * @return 请求ID，用于获取流式响应
     */
    @Operation(summary = "AI对话", description = "与AI进行对话，返回请求ID用于从流式响应中获取消息")
    @PostMapping("/chat")
    public BaseResponse<String> chat(
            @Parameter(description = "AI对话请求参数", required = true) @RequestBody AIChatRequest aiChatRequest) {
        aiChatRequest.setContent(ContentCleaner.cleanHtmlContent(aiChatRequest.getContent()));
        return BaseResponse.success(aiService.chat(aiChatRequest));
    }

}