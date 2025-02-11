package com.qiu.cardflow.question.model.dto.questionsubmint;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建请求
 */
@Data
public class QuestionSubmitAddRequest implements Serializable {


    /**
     * 编程语言
     */
    @NotEmpty(message = "编程语言不能为空")
    private String language;

    /**
     * 用户代码
     */
    @NotEmpty(message = "用户代码不能为空")
    private String code;


    /**
     * 题目 id
     */
    @NotNull(message = "题目 id 不能为空")
    @Min(1)
    private Long questionId;


    private static final long serialVersionUID = 1L;
}