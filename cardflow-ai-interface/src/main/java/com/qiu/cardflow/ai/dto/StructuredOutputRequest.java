package com.qiu.cardflow.ai.dto;

import com.qiu.codeflow.eventStream.dto.EventType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StructuredOutputRequest {
    @NotNull
    private Class targetType;

    @NotEmpty(message = "输入不能为空")
    private String userPrompt;
    private String systemPrompt = "";

    @NotEmpty(message = "模型不能为空")
    private String model;

    @NotEmpty(message = "用户id不能为空")
    private String userId;

    /**
     * 事件类型
     */
    private EventType eventType;

    private String conversationId;

    @Min(value = 1, message = "对话记忆窗口大小不能小于1")
    private Integer chatHistoryWindowSize = 5;


}
