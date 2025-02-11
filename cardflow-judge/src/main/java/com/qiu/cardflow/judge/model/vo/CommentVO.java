package com.qiu.cardflow.judge.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 题解分页封装类
 *
 * @TableName question
 */

@Data
public class CommentVO implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer thumbNum;

    /**
     * 收藏数
     */
    private Integer favourNum;

    /**
     * 创建用户
     */
    private UserVO userVO;

    /**
     * 父评论 id
     */
    private Long fatherCommentId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否点过赞了
     */
    private Boolean liked;
}