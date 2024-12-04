package com.qiu.qoj.codesandbox.model;

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
//    private List<String> outputList;
//
//    /**
//     * 接口信息
//     */
//    private String message;
//
//    /**
//     * 执行状态
//     */
//    private Integer status;
//
//    /**
//     * 判题信息
//     */
//    private JudgeInfo judgeInfo;

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


