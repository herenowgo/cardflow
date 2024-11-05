package com.qiu.qoj.model.vo.questionSubmit;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.qiu.qoj.domain.PageRequest;
import com.qiu.qoj.model.dto.questionsubmint.JudgeInfo;
import com.qiu.qoj.model.entity.QuestionSubmit;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Data
public class QuestionSubmitPageVO extends PageRequest {
    /**
     * id
     */
    private Long id;

    /**
     * 编程语言
     */
    private String language;


    /**
     * 判题信息（json 对象）
     */
    private JudgeInfo judgeInfo;

    /**
     * 判题状态（0 - 待判题、1 - 判题中、2 - 成功、3 - 答案错误、4 - 编译错误）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;




    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 包装类转对象
     *
     * @param QuestionSubmitPageVO
     * @return
     */
    public static QuestionSubmit voToObj(QuestionSubmitPageVO QuestionSubmitPageVO) {
        if (QuestionSubmitPageVO == null) {
            return null;
        }
        QuestionSubmit questionSubmit = new QuestionSubmit();
        BeanUtils.copyProperties(QuestionSubmitPageVO, questionSubmit);
        JudgeInfo judgeInfoObj = QuestionSubmitPageVO.getJudgeInfo();
        if (judgeInfoObj != null) {
            questionSubmit.setJudgeInfo(JSONUtil.toJsonStr(judgeInfoObj));
        }
        return questionSubmit;
    }

    /**
     * 对象转包装类
     *
     * @param questionSubmit
     * @return
     */
    public static QuestionSubmitPageVO objToVo(QuestionSubmit questionSubmit) {
        if (questionSubmit == null) {
            return null;
        }
        QuestionSubmitPageVO QuestionSubmitPageVO = new QuestionSubmitPageVO();
        BeanUtils.copyProperties(questionSubmit, QuestionSubmitPageVO);
        String judgeInfoStr = questionSubmit.getJudgeInfo();
        QuestionSubmitPageVO.setJudgeInfo(JSONUtil.toBean(judgeInfoStr, JudgeInfo.class));

        return QuestionSubmitPageVO;
    }
}
