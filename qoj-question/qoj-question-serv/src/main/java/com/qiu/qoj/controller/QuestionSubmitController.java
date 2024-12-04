package com.qiu.qoj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiu.qoj.common.api.BaseResponse;
import com.qiu.qoj.common.api.ErrorCode;
import com.qiu.qoj.common.api.UserContext;
import com.qiu.qoj.exception.BusinessException;
import com.qiu.qoj.model.dto.questionsubmint.DebugCodeRequest;
import com.qiu.qoj.model.dto.questionsubmint.QuestionSubmitAddRequest;
import com.qiu.qoj.model.dto.questionsubmint.QuestionSubmitQueryRequest;
import com.qiu.qoj.model.entity.QuestionSubmit;
import com.qiu.qoj.model.vo.ExecuteCodeResponseVO;
import com.qiu.qoj.model.vo.QuestionSubmitStateVO;
import com.qiu.qoj.model.vo.QuestionSubmitVO;
import com.qiu.qoj.model.vo.QuestionSubmitWithTagVO;
import com.qiu.qoj.model.vo.questionSubmit.QuestionSubmitPageVO;
import com.qiu.qoj.service.QuestionSubmitService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
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
     * @param request
     * @return resultNum
     */
    @PostMapping("/")
    public BaseResponse<Long> doQuestionSubmit(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest,
                                               HttpServletRequest request) {
        if (questionSubmitAddRequest == null || questionSubmitAddRequest.getQuestionId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long questionSubmitId = questionSubmitService.doQuestionSubmit(questionSubmitAddRequest, UserContext.getUserId());
//        QuestionSubmitStateVO questionSubmitStateVO = new QuestionSubmitStateVO();
//        questionSubmitStateVO.setStatus(questionSubmit.getStatus());
//        questionSubmitStateVO.setJudgeInfo(JSONUtil.parse(questionSubmit.getJudgeInfo()).toBean(JudgeInfo.class));
        return BaseResponse.success(questionSubmitId);
    }



    @PostMapping("/debug")
    public BaseResponse<ExecuteCodeResponseVO> debugCode(@RequestBody DebugCodeRequest debugCodeRequest) throws IOException, InterruptedException {
        ExecuteCodeResponseVO executeCodeResponseVO = questionSubmitService.debugCode(debugCodeRequest);
        return BaseResponse.success(executeCodeResponseVO);
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
