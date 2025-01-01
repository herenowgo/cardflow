package com.qiu.qoj.ai.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qiu.qoj.ai.model.dto.ai.AIChatRequest;
import com.qiu.qoj.ai.model.dto.ai.UserCodeAnalysisRequest;
import com.qiu.qoj.ai.service.AIService;
import com.qiu.qoj.common.api.BaseResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class ChatController {

    private final AIService aiService;

    @PostMapping("/tags")
    public BaseResponse<String> getTags(@RequestBody AIChatRequest aiChatRequest) {
        return BaseResponse.success(aiService.generateTags(aiChatRequest));
    }

    @PostMapping("/cards")
    public BaseResponse<String> getCards(@RequestBody AIChatRequest aiChatRequest) {
        return BaseResponse.success(aiService.generateCards(aiChatRequest));
    }

    @PostMapping("/codeAnalysis")
    public BaseResponse<String> analysisUserCode(@RequestBody UserCodeAnalysisRequest userCodeAnalysisRequest) {
        String requestId = aiService.generateCodeModificationSuggestion(userCodeAnalysisRequest.getAiChatRequest(),
                userCodeAnalysisRequest.getQuestionSubmitId(), userCodeAnalysisRequest.getIndex());

        return BaseResponse.success(requestId);
    }

    

}