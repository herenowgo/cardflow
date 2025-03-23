package com.qiu.codeflow.eventStream.dto;

import cn.hutool.core.bean.BeanUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventMessage {
    private String requestId;
    private String userId;
    private EventType eventType;
    private Integer sequence;
    private Object data;

    public static EventMessageVO parseToVO(EventMessage eventMessage) {
        return BeanUtil.copyProperties(eventMessage, EventMessageVO.class);
    }
} 