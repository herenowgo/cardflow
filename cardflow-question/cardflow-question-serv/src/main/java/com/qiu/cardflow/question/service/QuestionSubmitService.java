package com.qiu.cardflow.question.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qiu.cardflow.question.model.dto.questionsubmint.DebugCodeRequest;
import com.qiu.cardflow.question.model.dto.questionsubmint.QuestionSubmitAddRequest;
import com.qiu.cardflow.question.model.dto.questionsubmint.QuestionSubmitQueryRequest;
import com.qiu.cardflow.question.model.dto.questionsubmint.QuestionSubmitResponse;
import com.qiu.cardflow.question.model.entity.QuestionSubmit;
import com.qiu.cardflow.question.model.vo.QuestionSubmitStateVO;
import com.qiu.cardflow.question.model.vo.QuestionSubmitVO;
import com.qiu.cardflow.question.model.vo.QuestionSubmitWithTagVO;
import com.qiu.cardflow.question.model.vo.questionSubmit.QuestionSubmitPageVO;

import java.io.IOException;
import java.util.List;

/**
 * @author 10692
 * @description 针对表【question_submit(题目提交)】的数据库操作Service
 * @createDate 2023-12-11 19:31:25
 */
public interface QuestionSubmitService extends IService<QuestionSubmit> {
    /**
     * 题目提交
     *
     * @param questionSubmitAddRequest
     * @return
     */
    QuestionSubmitResponse doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, Long userId);

    /**
     * 题目提交（内部服务）
     *
     * @param userId
     * @param questionId
     * @return
     */
//    int doQuestionSubmitInner(long userId, long questionId);

    /**
     * 获取查询条件
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest);



    /**
     * 分页获取题目封装
     *
     * @param questionSubmitPage
     * @return
     */
    Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage);

    QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit);

    Integer getQuestionSubmitState(Long questionSubmitId);

    QuestionSubmitStateVO getJudgeInformation(Long questionSubmitId);

    Page<QuestionSubmitPageVO> listQuestionSubmitRecord(Long questionId, Integer current, Integer size);

    String debugCode(DebugCodeRequest debugCodeRequest) throws IOException, InterruptedException;

    List<QuestionSubmitWithTagVO> listQuestionSubmit(Integer number);
}
