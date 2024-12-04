package com.qiu.qoj.ai.model.dto.comment;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建请求
 */
@Data
public class CommentAddRequest implements Serializable {

    /**
     * 内容
     */
    private String content;

    /**
     * 题解 id
     */
    private Long questionSolvingId;

    /**
     * 父评论 id
     */
    private Long fatherCommentId;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}