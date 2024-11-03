package com.qiu.qoj.model.vo;

import com.qiu.qoj.service.UserService;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 题目封装类
 *
 * @TableName question
 */

@Data
public class QuestionSolvingVO implements Serializable {
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


//    /**
//     * 对象转包装类
//     *
//     * @param questionSolving
//     * @return
//     */
//    public static QuestionSolvingVO objToVo(QuestionSolving questionSolving) {
//        if (questionSolving == null) {
//            return null;
//        }
//        QuestionSolvingVO questionSolvingVO = new QuestionSolvingVO();
//        BeanUtils.copyProperties(questionSolving, questionSolvingVO);
//
//
//        return questionSolvingVO;
//    }

}