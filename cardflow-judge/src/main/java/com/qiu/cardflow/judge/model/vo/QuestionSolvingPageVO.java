package com.qiu.cardflow.judge.model.vo;

import com.qiu.cardflow.judge.service.UserService;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 题解分页封装类
 *
 * @TableName question
 */

@Data
public class QuestionSolvingPageVO implements Serializable {
    private UserService userService;
    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 题目 id
     */
    private Long questionId;

    /**
     * 支持数
     */
    private Integer supportNumber;

    /**
     * 浏览量
     */
    private Integer pageView;


    /**
     * 创建用户
     */
    private UserVO userVO;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;


}