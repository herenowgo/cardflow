package com.qiu.cardflow.ai.structured;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public enum TargetType implements Serializable {

    CARDS("Cards", Cards.class);

    private String name;
    private Class type;

}
