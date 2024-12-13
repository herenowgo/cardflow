package com.qiu.qoj.ai.model.dto.ai;

import com.qiu.qoj.ai.model.enums.AIModelVO;
import lombok.Data;

@Data
public class AIChatRequest {
    private String content;
    private AIModelVO model = AIModelVO.BASIC;
    private String prompt;
}
