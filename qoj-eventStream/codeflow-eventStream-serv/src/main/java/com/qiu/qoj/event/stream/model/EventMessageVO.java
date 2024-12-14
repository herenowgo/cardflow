package com.qiu.qoj.event.stream.model;

import lombok.Data;

@Data
public class EventMessageVO {
    private String groupId;
    private Integer sequence;
    private String eventType;
    private Object data;
} 