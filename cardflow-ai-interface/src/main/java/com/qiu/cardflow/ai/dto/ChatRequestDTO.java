package com.qiu.cardflow.ai.dto;

import com.qiu.codeflow.eventStream.dto.EventType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequestDTO implements Serializable {

    @NotEmpty(message = "输入不能为空")
    private String userPrompt;

    private String systemPrompt;

    @NotEmpty(message = "模型不能为空")
    private String model;

    @NotEmpty(message = "用户id不能为空")
    private String userId;

    private String conversationId;

    @Min(value = 1, message = "对话记忆窗口大小不能小于1")
    private Integer chatHistoryWindowSize = 5;

    /**
     * 事件类型
     */
    private EventType eventType;

    // the max collected size maxTime
    @Min(value = 1, message = "最大收集大小不能小于1")
    private Integer maxSize = 10;
    // the timeout enforcing the release of a partial buffer，单位是毫秒
    @Min(value = 10, message = "最大收集时间不能小于10毫秒")
    private Integer maxMills = 700;

    // private static final long serialVersionUID = 1L;

}
