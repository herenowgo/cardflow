package com.qiu.qoj.question.model.dto.questionsubmint;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuestionSubmitResponse {
    String requestId;

    Long questionSubmitId;
}
