package com.qiu.codeflow.dto;

import cn.hutool.core.bean.BeanUtil;
import lombok.Data;

@Data
public class EventMessage {
    private String groupId;
    private String userId;
    private EventType eventType;
    private Integer sequence;
    private Object data;

    public static EventMessageVO parseToVO(EventMessage eventMessage) {
        return BeanUtil.copyProperties(eventMessage, EventMessageVO.class);
    }
} 