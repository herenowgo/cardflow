package com.qiu.cardflow.discussion.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 题解
 *
 * @TableName question_solving
 */
@TableName(value = "question_solving")
@Data
public class QuestionSolving implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
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
     * 题目 id
     */
    private Long questionId;

    /**
     * 支持数
     */
    private Integer supportNumber;

    @TableField(exist = false)
    private Boolean supported;

    /**
     * 浏览量
     */
    private Integer pageView;


    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}