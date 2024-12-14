package com.qiu.qoj.ai.model.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventMessage {
    private String groupId;
    private String userId;
    private EventType eventType;
    private Integer sequence;
    private Object data;
} 