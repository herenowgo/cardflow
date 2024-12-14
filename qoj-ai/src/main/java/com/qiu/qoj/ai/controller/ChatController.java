package com.qiu.qoj.ai.controller;

import com.qiu.qoj.ai.model.dto.ai.AIChatRequest;
import com.qiu.qoj.ai.service.AIService;
import com.qiu.qoj.common.api.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/code")
    public BaseResponse<String> analysisAlgorithmicProblem(AIChatRequest aiChatRequest) {
        return BaseResponse.success(aiService.generateAlgorithmProblemModificationSuggestion());
    }


}