package com.qiu.qoj.question.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.qiu.qoj.codesandbox.dto.JudgeInfo;
import lombok.Data;

import java.io.Serializable;

/**
 * 题目封装类
 *
 * @TableName question
 */

@Data
public class QuestionSubmitStateVO implements Serializable {


    /**
     * 判题信息（json 对象）
     */
    private JudgeInfo judgeInfo;

    /**
     * 判题状态（0 - 待判题、1 - 判题中、2 - 成功、3 - 失败）
     */
    private Integer status;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}