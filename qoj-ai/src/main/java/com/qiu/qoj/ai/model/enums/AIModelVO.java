package com.qiu.qoj.ai.model.enums;

import lombok.Getter;

@Getter
public enum AIModelVO {
    BASIC("basic"),
    A1("a1"),
    A2("a2"),
    PLUS("plus"),
    ;

    private final String name;

    AIModelVO(String name) {
        this.name = name;
    }
}
