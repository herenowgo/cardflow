package com.qiu.cardflow.codesandbox.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class JudgeInfo {

    /**
     * 程序执行信息
     */
    private String message;

    /**
     * 消耗内存
     */
    private List<Long> memory = new ArrayList<>();

    /**
     * 消耗时间（KB）
     */
    private List<Long> time = new ArrayList<>();
}
