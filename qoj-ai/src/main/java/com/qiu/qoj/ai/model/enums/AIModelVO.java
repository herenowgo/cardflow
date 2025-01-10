package com.qiu.qoj.ai.model.enums;

import lombok.Getter;

@Getter
public enum AIModelVO {
    BASIC("basic"),
    A1("a1"),
    A2("a2"),
    PLUS("plus"),
    GEMINI_1_5_PRO_EXP("gemini-1.5-pro-exp"),
    GEMINI_2_0_FLASH_EXP("gemini-2.0-flash-exp"),
    DEEP_SEEK("DeepSeek-V3"),
    ;

    private final String name;

    AIModelVO(String name) {
        this.name = name;
    }
}
