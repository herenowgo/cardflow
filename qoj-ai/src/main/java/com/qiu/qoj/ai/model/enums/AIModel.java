package com.qiu.qoj.ai.model.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum AIModel {
    GLM_4_Flash("glm-4-flash", "basic"),
    GLM_4_Air("glm-4-air", "better"),
    GLM_4_AirX("glm-4-airx", "fast"),
    GLM_4_PLUS("glm-4-plus", "plus"),
    ;


    private final String name;
    private final String voName;

    AIModel(String name, String voName) {
        this.name = name;
        this.voName = voName;
    }

    public static AIModel getByVO(AIModelVO aiModelVO) {
        return Arrays.stream(values())
                .filter(aiModel -> aiModel.voName.equals(aiModelVO.getName()))
                .findFirst()
                .orElse(GLM_4_Flash);
    }
}

