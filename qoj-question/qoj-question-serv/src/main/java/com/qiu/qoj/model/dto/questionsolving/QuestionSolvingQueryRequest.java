package com.qiu.qoj.model.dto.questionsolving;

import com.qiu.qoj.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionSolvingQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String text;


    /**
     * 创建用户 id
     */
    private Long userId;


    /**
     * 题目id
     */
    private Long questionId;


    private static final long serialVersionUID = 1L;
}