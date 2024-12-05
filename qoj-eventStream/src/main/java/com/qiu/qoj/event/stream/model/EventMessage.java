package com.qiu.qoj.event.stream.model;

import lombok.Data;

@Data
public class EventMessage {
    private String userId;
    private String eventType;
    private Object data;
} 