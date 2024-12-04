package com.qiu.qoj.discussion.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qiu.qoj.discussion.model.dto.questionsolving.QuestionSolvingQueryRequest;
import com.qiu.qoj.discussion.model.entity.QuestionSolving;
import com.qiu.qoj.discussion.model.vo.QuestionSolvingPageVO;
import jakarta.servlet.http.HttpServletRequest;


/**
 * @author 10692
 * @description 针对表【question_solving(题解)】的数据库操作Service
 * @createDate 2023-12-21 10:24:02
 */
public interface QuestionSolvingService extends IService<QuestionSolving> {

    QueryWrapper<QuestionSolving> getQueryWrapper(QuestionSolvingQueryRequest questionQueryRequest);


    QuestionSolving getUserQuestionSolving(Long questionId, HttpServletRequest request);

//    <E extends IPage<T>> E page(E page, Wrapper<T> queryWrapper) {
//        return getBaseMapper().selectPage(page, queryWrapper);
//    }

    Page<QuestionSolvingPageVO> getQuestionSolvingPageVO(Page<QuestionSolving> questionSolvingPage, HttpServletRequest request);

    Boolean likeQuestionSolving(Long questionSolvingId, HttpServletRequest httpServletRequest);

    Boolean isSupported(Long questionSolvingId, HttpServletRequest httpServletRequest);
}
