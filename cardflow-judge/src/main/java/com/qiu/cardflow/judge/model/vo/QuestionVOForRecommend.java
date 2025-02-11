package com.qiu.cardflow.judge.model.vo;

import cn.hutool.json.JSONUtil;
import com.qiu.cardflow.judge.model.entity.Question;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.List;

/**
 * 题目封装类
 *
 * @TableName question
 */

@Data
public class QuestionVOForRecommend implements Serializable {
    /**
     * id
     */

    private Long id;

    /**
     * 标题
     */
    private String title;


    /**
     * 标签列表（json 数组）
     */
    private List<String> tags;


    /**
     * 题目提交数
     */
    private Integer submitNum;

    /**
     * 题目通过数
     */
    private Integer acceptedNum;


    /**
     * 点赞数
     */
    private Integer thumbNum;

    /**
     * 收藏数
     */
    private Integer favourNum;


    private static final long serialVersionUID = 1L;


    /**
     * 包装类转对象
     *
     * @param questionVO
     * @return
     */
    public static Question voToObj(QuestionVOForRecommend questionVO) {
        if (questionVO == null) {
            return null;
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionVO, question);
        List<String> tagList = questionVO.getTags();
        if (tagList != null) {
            question.setTags(JSONUtil.toJsonStr(tagList));
        }


        return question;
    }

    /**
     * 对象转包装类
     *
     * @param question
     * @return
     */
    public static QuestionVOForRecommend objToVo(Question question) {
        if (question == null) {
            return null;
        }
        QuestionVOForRecommend questionVOForRecommend = new QuestionVOForRecommend();
        BeanUtils.copyProperties(question, questionVOForRecommend);
        String tagList = question.getTags();
        questionVOForRecommend.setTags(JSONUtil.toList(tagList, String.class));
        return questionVOForRecommend;
    }
}