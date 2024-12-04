package com.qiu.qoj.codesandbox.dto;

import lombok.Data;

@Data
public class ExecuteMessage {
    private Integer exitValue;

    private String message;

    private String errorMessage;

    private Long time;

    private Long memory;
}
