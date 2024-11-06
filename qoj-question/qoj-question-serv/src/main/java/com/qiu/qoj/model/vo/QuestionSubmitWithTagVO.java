package com.qiu.qoj.model.vo;

import lombok.Data;

import java.util.Date;

@Data
public class QuestionSubmitWithTagVO {

    /**
     * 编程语言
     */
    private String language;



    /**
     * 判题信息（json 对象）
     */
    private String judgeInfo;

    /**
     * 判题状态（0 - 待判题、1 - 判题中、2 - 成功、3 - 答案错误、4 - 编译错误）
     */
    private Integer status;

    /**
     * 题目 id
     */
    private Long questionId;



    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 标签列表（json 数组）
     */
    private String tags;

}
