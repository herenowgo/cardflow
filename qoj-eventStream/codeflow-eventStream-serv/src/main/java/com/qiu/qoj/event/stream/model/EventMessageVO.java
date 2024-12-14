package com.qiu.qoj.event.stream.model;

import lombok.Data;

@Data
public class EventMessageVO {
    private String requestId;
    private Integer sequence;
    private String eventType;
    private Object data;
} 