package com.qiu.qoj.ai.model.enums;

import lombok.Getter;

@Getter
public enum AIModelVO {
    BASIC("basic"),
    A1("a1"),
    A2("a2"),
    PLUS("plus"),
    GEMINI_EXP_1206("gemini-exp-1206"),
    GEMINI_2_0_FLASH_EXP("gemini-2.0-flash-exp"),
    ;

    private final String name;

    AIModelVO(String name) {
        this.name = name;
    }
}
