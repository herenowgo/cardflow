package com.qiu.qoj.ai.model.dto.ai;

import com.qiu.qoj.ai.model.enums.AIModelVO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AIChatRequest {
    private String content;
    private AIModelVO model = AIModelVO.BASIC;
    private String prompt;
    // 会话id，用于实现多轮对话（在多轮对话中，要传递相同的sessionId）
    private String sessionId;
    @Schema(description = "AI助手类型", example = "code_assistant")
    private String agent;
}
