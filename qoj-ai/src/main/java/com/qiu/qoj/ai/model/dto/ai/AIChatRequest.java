package com.qiu.qoj.ai.model.dto.ai;

import com.qiu.qoj.ai.model.enums.AIModelVO;

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
    // 会话id，用于实现多轮对话
    private String sessionId;
}
