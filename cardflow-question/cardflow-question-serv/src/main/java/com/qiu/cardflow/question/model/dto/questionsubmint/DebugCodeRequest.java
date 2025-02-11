package com.qiu.cardflow.question.model.dto.questionsubmint;

import lombok.Data;

import java.io.Serializable;

@Data
public class DebugCodeRequest implements Serializable {

    /**
     * 题目 id
     */
    private Long questionId;

    /**
     * 用户代码
     */
    private String code;

    /**
     * 编程语言
     */
    private String language;

    /**
     * 测试用例（输入）
     */
    private String testCase;
}
