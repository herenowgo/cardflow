package com.qiu.cardflow.api.vo.ai;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;

@Data
public class ChatRequest implements Serializable {
    @NotEmpty(message = "输入不能为空")
    private String userPrompt;
    private String systemPrompt;

    @NotEmpty(message = "模型不能为空")
    private String model;

    private String conversationId;

}
