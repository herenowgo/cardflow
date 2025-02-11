package com.qiu.cardflow.judge.judge.codesandbox.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeResponse {

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
}







