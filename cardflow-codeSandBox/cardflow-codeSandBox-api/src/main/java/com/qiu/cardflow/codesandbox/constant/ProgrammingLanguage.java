package com.qiu.cardflow.codesandbox.constant;

import lombok.Getter;

@Getter
public enum ProgrammingLanguage {
    JAVA("java", "Java");

    private final String code;
    private final String name;

    ProgrammingLanguage(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public static ProgrammingLanguage fromCode(String code) {
        for (ProgrammingLanguage language : ProgrammingLanguage.values()) {
            if (language.getCode().equalsIgnoreCase(code)) {
                return language;
            }
        }
        throw new IllegalArgumentException("Invalid language code: " + code);
    }
}
