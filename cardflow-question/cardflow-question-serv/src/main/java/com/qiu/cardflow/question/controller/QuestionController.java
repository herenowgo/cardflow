package com.qiu.cardflow.question.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.qiu.cardflow.common.api.BaseResponse;
import com.qiu.cardflow.common.api.DeleteRequest;
import com.qiu.cardflow.common.api.UserContext;
import com.qiu.cardflow.common.constant.QuestionConstant;
import com.qiu.cardflow.common.constant.UserConstant;
import com.qiu.cardflow.common.exception.Asserts;
import com.qiu.cardflow.question.model.dto.question.*;
import com.qiu.cardflow.question.model.entity.Question;
import com.qiu.cardflow.question.model.vo.QuestionVO;
import com.qiu.cardflow.question.service.QuestionService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 题目接口
 */
@RestController
@RequestMapping("/question")
@Slf4j
public class QuestionController {

    @Resource
    private QuestionService questionService;


    private final static Gson GSON = new Gson();

    // region 增删改查

    /**
     * 创建
     *
     * @param questionAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addQuestion(@RequestBody QuestionAddRequest questionAddRequest, HttpServletRequest request) {
        Asserts.failIf(questionAddRequest == null, "请求参数错误");
        Question question = new Question();
        BeanUtils.copyProperties(questionAddRequest, question);
        List<String> tags = questionAddRequest.getTags();
        if (tags != null) {
            question.setTags(GSON.toJson(tags));
        }
        List<JudgeCase> judgeCase = questionAddRequest.getJudgeCase();
        if (judgeCase != null) {
            question.setJudgeCase(GSON.toJson(judgeCase));
        }
        JudgeConfig judgeConfig = questionAddRequest.getJudgeConfig();
        if (judgeConfig != null) {
            question.setJudgeConfig(GSON.toJson(judgeConfig));
        }
        questionService.validQuestion(question, true);

        question.setUserId(UserContext.getUserId());
        question.setFavourNum(0);
        question.setThumbNum(0);
        boolean result = questionService.save(question);
        Asserts.failIf(!result, "操作失败");
        long newQuestionId = question.getId();
        return BaseResponse.success(newQuestionId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @CacheEvict(value = QuestionConstant.CACHE_QUESTION_SIMPLE_PAGE, allEntries = true)
    public BaseResponse<Boolean> deleteQuestion(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        Asserts.failIf(deleteRequest == null || deleteRequest.getId() <= 0, "请求参数错误");
        long id = deleteRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        Asserts.failIf(oldQuestion == null, "数据不存在");
        // 仅本人或管理员可删除
        Asserts.failIf(!oldQuestion.getUserId().equals(UserContext.getUserId()) && !UserContext.isAdmin(), "无权限");
        boolean b = questionService.removeById(id);
        return BaseResponse.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param questionUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @CacheEvict(value = QuestionConstant.CACHE_QUESTION_SIMPLE_PAGE, allEntries = true)
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest questionUpdateRequest) {

        Asserts.failIf(questionUpdateRequest == null || questionUpdateRequest.getId() <= 0, "请求参数错误");
        Question question = new Question();
        BeanUtils.copyProperties(questionUpdateRequest, question);
        List<String> tags = questionUpdateRequest.getTags();
        if (tags != null) {
            question.setTags(GSON.toJson(tags));
        }
        List<JudgeCase> judgeCase = questionUpdateRequest.getJudgeCase();
        if (judgeCase != null) {
            question.setJudgeCase(GSON.toJson(judgeCase));
        }
        JudgeConfig judgeConfig = questionUpdateRequest.getJudgeConfig();
        if (judgeConfig != null) {
            question.setJudgeConfig(GSON.toJson(judgeConfig));
        }
        // 参数校验
        questionService.validQuestion(question, false);
        long id = questionUpdateRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        Asserts.failIf(oldQuestion == null, "数据不存在");
        boolean result = questionService.updateById(question);
        return BaseResponse.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */

    @GetMapping("/get")
    public BaseResponse<Question> getQuestionById(long id) {

        Asserts.failIf(id <= 0, "请求参数错误");

        Question question = questionService.getById(id);

        Asserts.failIf(question == null, "数据不存在");
        // 不是本人或管理员，就不能获取所有信息
        Asserts.failIf(!question.getUserId().equals(UserContext.getUserId()) && !UserContext.isAdmin(), "无权限");

        return BaseResponse.success(question);
    }

    /**
     * 根据 id 获取（脱敏）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<QuestionVO> getQuestionVOById(long id, HttpServletRequest request) {
        Asserts.failIf(id <= 0, "请求参数错误");
        Question question = questionService.getByIdUseCache(id);
        Asserts.failIf(question == null, "数据不存在");
        return BaseResponse.success(questionService.getQuestionVO(question, request));
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                               HttpServletRequest request) {
        Asserts.failIf(questionQueryRequest == null, "请求参数错误");
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        Asserts.failIf(size > 20, "页面大小超出限制");
        Page<Question> questionPage;
//        if (StringUtils.isAllBlank(questionQueryRequest.getTitle(), questionQueryRequest.getAnswer(), questionQueryRequest.getContent()) && CollectionUtils.isEmpty(questionQueryRequest.getTags())) {
//            questionPage = questionService.simplePageUseCache(current, size);
//        } else {
        questionPage = questionService.page(new Page<>(current, size), questionService.getQueryWrapper(questionQueryRequest));
//        }

        return BaseResponse.success(questionService.getQuestionVOPage(questionPage, request));
    }

    /**
     * 分页获取题目列表（仅管理员）
     *
     * @param questionQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Question>> listQuestionByPage(@RequestBody QuestionQueryRequest questionQueryRequest) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        Page<Question> questionPage;
        questionPage = questionService.page(new Page<>(current, size), questionService.getQueryWrapper(questionQueryRequest));

        return BaseResponse.success(questionPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listMyQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                                 HttpServletRequest request) {
        Asserts.failIf(questionQueryRequest == null, "请求参数错误");
        questionQueryRequest.setUserId(UserContext.getUserId());
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        Asserts.failIf(size > 20, "页面大小超出限制");
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        return BaseResponse.success(questionService.getQuestionVOPage(questionPage, request));
    }

    // endregion


    /**
     * 编辑（用户）
     *
     * @param questionEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    @CacheEvict(value = QuestionConstant.CACHE_QUESTION_SIMPLE_PAGE, allEntries = true)
    public BaseResponse<Boolean> editQuestion(@RequestBody QuestionEditRequest questionEditRequest, HttpServletRequest request) {
        Asserts.failIf(questionEditRequest == null || questionEditRequest.getId() <= 0, "请求参数错误");
        Question question = new Question();
        BeanUtils.copyProperties(questionEditRequest, question);
        List<String> tags = questionEditRequest.getTags();
        if (tags != null) {
            question.setTags(GSON.toJson(tags));
        }
        List<JudgeCase> judgeCase = questionEditRequest.getJudgeCase();
        if (judgeCase != null) {
            question.setJudgeCase(GSON.toJson(judgeCase));
        }
        JudgeConfig judgeConfig = questionEditRequest.getJudgeConfig();
        if (judgeConfig != null) {
            question.setJudgeConfig(GSON.toJson(judgeConfig));
        }
        // 参数校验
        questionService.validQuestion(question, false);
        long id = questionEditRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        Asserts.failIf(oldQuestion == null, "数据不存在");
        // 仅本人或管理员可编辑
        Asserts.failIf(!oldQuestion.getUserId().equals(UserContext.getUserId()) && !UserContext.isAdmin(), "无权限");
        boolean result = questionService.updateById(question);
        return BaseResponse.success(result);
    }


    /**
     * 获取最热门的50个题目
     *
     * @param httpServletRequest
     * @return
     */
    @GetMapping("/topFifty")
    public BaseResponse<List<QuestionVO>> getTopFifty(HttpServletRequest httpServletRequest) {
        List<QuestionVO> topFifty = questionService.getTopFifty(httpServletRequest);
        return BaseResponse.success(topFifty);
    }

}
