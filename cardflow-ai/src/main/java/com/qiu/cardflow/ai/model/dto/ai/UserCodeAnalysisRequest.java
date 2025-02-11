package com.qiu.cardflow.ai.model.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserCodeAnalysisRequest {
    AIChatRequest aiChatRequest;
    Long questionSubmitId;
    Integer index;
}
