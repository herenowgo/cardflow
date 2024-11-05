package com.qiu.qoj.model.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ExecuteCodeResponseVO {
    private String compileOutput;

    private String compileErrorOutput;

    private List<String> runOutput = new ArrayList<>();

    private List<String> runErrorOutput = new ArrayList<>();

    /**
     * 消耗内存
     */
    private List<Long> memory = new ArrayList<>();

    /**
     * 消耗时间（KB）
     */
    private List<Long> time = new ArrayList<>();

    private String testCase;
}
