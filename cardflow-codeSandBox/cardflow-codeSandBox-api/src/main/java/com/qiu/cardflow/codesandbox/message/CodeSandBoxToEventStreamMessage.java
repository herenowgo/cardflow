package com.qiu.cardflow.codesandbox.message;

import com.qiu.cardflow.codesandbox.dto.ExecuteCodeRequest;
import com.qiu.codeflow.eventStream.dto.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class CodeSandBoxToEventStreamMessage implements Serializable {
    @NotNull
    ExecuteCodeRequest executeCodeRequest;
    @NotBlank
    String userId;
    @NotNull
    EventType eventType;
    @NotNull
    String requestId;
}
