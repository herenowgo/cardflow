package com.qiu.cardflow.ai.constant;

/**
 * 熔断状态枚举
 */
public enum CircuitState {
    /**
     * 关闭状态 - 正常工作
     */
    CLOSED,

    /**
     * 半开状态 - 尝试恢复
     */
    HALF_OPEN,

    /**
     * 开启状态 - 熔断激活
     */
    OPEN
}