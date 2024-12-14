package com.qiu.qoj.question.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiu.qoj.common.api.BaseResponse;
import com.qiu.qoj.common.api.UserContext;
import com.qiu.qoj.question.model.dto.questionsubmint.DebugCodeRequest;
import com.qiu.qoj.question.model.dto.questionsubmint.QuestionSubmitAddRequest;
import com.qiu.qoj.question.model.dto.questionsubmint.QuestionSubmitQueryRequest;
import com.qiu.qoj.question.model.entity.QuestionSubmit;
import com.qiu.qoj.question.model.vo.QuestionSubmitStateVO;
import com.qiu.qoj.question.model.vo.QuestionSubmitVO;
import com.qiu.qoj.question.model.vo.QuestionSubmitWithTagVO;
import com.qiu.qoj.question.model.vo.questionSubmit.QuestionSubmitPageVO;
import com.qiu.qoj.question.service.QuestionSubmitService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;


/**
 * 题目提交接口
 */
@RestController
@RequestMapping("/question_submit")
@Slf4j
public class QuestionSubmitController {

    @Resource
    private QuestionSubmitService questionSubmitService;


    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest
     * @return resultNum
     */
    @PostMapping("/")
    public BaseResponse<String> doQuestionSubmit(@RequestBody @Valid QuestionSubmitAddRequest questionSubmitAddRequest) {
        String requestId = questionSubmitService.doQuestionSubmit(questionSubmitAddRequest, UserContext.getUserId());
        return BaseResponse.success(requestId);
    }


    @PostMapping("/debug")
    public BaseResponse<String> debugCode(@RequestBody DebugCodeRequest debugCodeRequest) throws IOException, InterruptedException {
        String requestId = questionSubmitService.debugCode(debugCodeRequest);
        return BaseResponse.success(requestId);
    }

    /**
     * 分页获取题目提交列表（仅本人和管理员能看见自己提交的代码）
     *
     * @param questionSubmitQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<QuestionSubmitVO>> listQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest,
                                                                         HttpServletRequest request) {
        long current = questionSubmitQueryRequest.getCurrent();
        long size = questionSubmitQueryRequest.getPageSize();

        // 从数据库中获取原始的分页数据
        Page<QuestionSubmit> questionSubmitPage = questionSubmitService.page(new Page<>(current, size),
                questionSubmitService.getQueryWrapper(questionSubmitQueryRequest));

        // 脱敏
        return BaseResponse.success(questionSubmitService.getQuestionSubmitVOPage(questionSubmitPage));
    }

    /**
     * 获取题目的提交状态
     *
     * @param questionSubmitId
     * @return
     */
    @GetMapping("/state")
    public Integer getQuestionSubmitState(@RequestParam Long questionSubmitId) {
        return questionSubmitService.getQuestionSubmitState(questionSubmitId);
    }

    /**
     * 获取题目的提交信息
     *
     * @param questionSubmitId
     * @return
     */
    @GetMapping("/judgeInformation")
    public BaseResponse<QuestionSubmitStateVO> getJudgeInformation(@RequestParam Long questionSubmitId) {
        QuestionSubmitStateVO judgeInformation = questionSubmitService.getJudgeInformation(questionSubmitId);
        return BaseResponse.success(judgeInformation);
    }


    /**
     * 获取题目提交记录
     *
     * @param questionId
     * @return
     */
    @GetMapping("/records")
    public BaseResponse<Page<QuestionSubmitPageVO>> listQuestionSubmitRecord(@RequestParam Long questionId, @RequestParam Integer current, @RequestParam Integer size) {
        return BaseResponse.success(questionSubmitService.listQuestionSubmitRecord(questionId, current, size));
    }


    @GetMapping
    public BaseResponse<QuestionSubmit> getQuestionSubmitInfo(@RequestParam Long questionSubmitId) {
        return BaseResponse.success(questionSubmitService.getById(questionSubmitId));
    }


    /**
     * 获取提交记录
     */
    @GetMapping("/list")
    public BaseResponse<List<QuestionSubmitWithTagVO>> listQuestionSubmit(Integer number) {
        return BaseResponse.success(questionSubmitService.listQuestionSubmit(number));
    }

}
