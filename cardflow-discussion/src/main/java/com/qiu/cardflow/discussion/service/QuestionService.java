package com.qiu.cardflow.discussion.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qiu.cardflow.discussion.model.dto.question.QuestionQueryRequest;
import com.qiu.cardflow.discussion.model.entity.Question;
import com.qiu.cardflow.discussion.model.vo.QuestionVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * @author 10692
 * @description 针对表【question(题目)】的数据库操作Service
 * @createDate 2023-12-11 19:30:57
 */
public interface QuestionService extends IService<Question> {

    /**
     * 校验
     *
     * @param question
     * @param add
     */
    void validQuestion(Question question, boolean add);

    /**
     * 获取查询条件
     *
     * @param questionQueryRequest
     * @return
     */
    QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest);


    /**
     * 获取题目封装
     *
     * @param question
     * @param request
     * @return
     */
    QuestionVO getQuestionVO(Question question, HttpServletRequest request);

    /**
     * 分页获取题目封装
     *
     * @param questionPage
     * @param request
     * @return
     */
    Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request);


    List<QuestionVO> getTopFifty(HttpServletRequest httpServletRequest);

    Question getByIdUseCache(long id);

    //    @Cacheable(value = "Test")
//    @Cacheable(value = QuestionConstant.CACHE_QUESTION_SIMPLE_PAGE)
    Page<Question> simplePageUseCache(long current, long size);
}
