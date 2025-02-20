package com.qiu.cardflow.api.controller;


import com.qiu.cardflow.api.common.BaseResponse;
import com.qiu.cardflow.api.service.IAIService;
import com.qiu.cardflow.api.vo.ai.ChatRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/ai")
public class AIController {

    private final IAIService aiService;


//    @PostMapping("/tags")
//    public BaseResponse<String> getTags(@RequestBody AIChatRequest chatRequest) {
//        chatRequest.setContent(ContentCleaner.cleanHtmlContent(chatRequest.getContent()));
//        return BaseResponse.success(aiService.generateTags(chatRequest));
//    }
//
    @PostMapping("/cards")
    public BaseResponse<String> getCards(@RequestBody @Valid ChatRequest chatRequest) {
        return BaseResponse.success(aiService.generateCards(chatRequest));
    }
//
//    @PostMapping("/codeAnalysis")
//    public BaseResponse<String> analysisUserCode(@RequestBody UserCodeAnalysisRequest userCodeAnalysisRequest) {
//        String requestId = aiService.generateCodeModificationSuggestion(userCodeAnalysisRequest.getAiChatRequest(),
//                userCodeAnalysisRequest.getQuestionSubmitId(), userCodeAnalysisRequest.getIndex());
//
//        return BaseResponse.success(requestId);
//    }

    /**
     * 与AI进行对话
     *
     * @param chatRequest AI对话请求参数
     * @return 请求ID，用于获取流式响应
     */
    @Operation(summary = "AI对话", description = "与AI进行对话，返回请求ID用于从流式响应中获取消息")
    @PostMapping("/chat")
    public BaseResponse<String> chat(
            @Parameter(description = "AI对话请求参数", required = true) @RequestBody @Valid ChatRequest chatRequest) {
        return BaseResponse.success(aiService.chat(chatRequest));
    }

}