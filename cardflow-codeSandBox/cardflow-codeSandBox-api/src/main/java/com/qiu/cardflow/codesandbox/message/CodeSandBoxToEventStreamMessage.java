package com.qiu.cardflow.codesandbox.message;

import com.qiu.cardflow.codesandbox.dto.ExecuteCodeRequest;
import com.qiu.codeflow.eventStream.dto.EventType;
import lombok.Data;

import java.io.Serializable;

@Data
public class CodeSandBoxToEventStreamMessage implements Serializable {
    ExecuteCodeRequest executeCodeRequest;
    String userId;
    EventType eventType;
    String requestId;
}
