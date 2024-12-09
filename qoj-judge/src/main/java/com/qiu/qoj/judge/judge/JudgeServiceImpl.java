package com.qiu.qoj.judge.judge;

import cn.hutool.json.JSONUtil;
import com.qiu.qoj.common.constant.QuestionConstant;
import com.qiu.qoj.common.exception.Asserts;
import com.qiu.qoj.judge.CodeSandBoxService;
import com.qiu.qoj.judge.judge.codesandbox.model.ExecuteCodeRequest;
import com.qiu.qoj.judge.judge.codesandbox.model.ExecuteCodeResponse;
import com.qiu.qoj.judge.judge.codesandbox.model.JudgeInfo;
import com.qiu.qoj.judge.judge.strategy.JudgeContext;
import com.qiu.qoj.judge.model.dto.question.JudgeCase;
import com.qiu.qoj.judge.model.entity.Question;
import com.qiu.qoj.judge.model.entity.QuestionSubmit;
import com.qiu.qoj.judge.model.enums.QuestionSubmitStatusEnum;
import com.qiu.qoj.judge.service.QuestionService;
import com.qiu.qoj.judge.service.QuestionSubmitService;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JudgeServiceImpl implements JudgeService {

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private JudgeManager judgeManager;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CodeSandBoxService codeSandBoxService;

    @Resource
    private StreamBridge streamBridge;

    @Value("${codesandbox.type:example}")
    private String type;


    @Override
    public Boolean doJudge(long questionSubmitId) {
        // 获取提交记录和对应题目的测试用例
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
        Asserts.failIf(questionSubmit == null, "提交信息不存在");

        Long questionId = questionSubmit.getQuestionId();
        Question question = questionService.getById(questionId);
        Asserts.failIf(question == null, "题目不存在");


        // 调用沙箱，获取执行结果
        String language = questionSubmit.getLanguage();
        String code = questionSubmit.getCode();
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        List<String> inputList = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse = null;
        try {
            executeCodeResponse = codeSandBoxService.executeCode(executeCodeRequest);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        // 判题
        List<String> outputList = executeCodeResponse.getRunOutput();
        JudgeInfo judgeInfo1 = new JudgeInfo();
        if (!executeCodeResponse.getTime().isEmpty()) {
            judgeInfo1.setTime(executeCodeResponse.getTime().get(0));
        }

        JudgeContext judgeContext = JudgeContext.builder()
                .judgeInfo(judgeInfo1)
                .inputList(inputList)
                .outputList(outputList)
                .judgeCaseList(judgeCaseList)
                .question(question)
                .questionSubmit(questionSubmit)
                .build();

        JudgeInfo judgeInfo = judgeManager.doJudge(judgeContext, executeCodeResponse);
        judgeInfo.setInputList(inputList.stream().limit(3).collect(Collectors.toList()));

        if (!executeCodeResponse.getRunOutput().isEmpty()) {
            judgeInfo.setRunOutput(executeCodeResponse.getRunOutput().stream().limit(3).collect(Collectors.toList()));
        }
        judgeInfo.setAnswers(judgeCaseList.stream().limit(3).map(JudgeCase::getOutput).collect(Collectors.toList()));
        if (!executeCodeResponse.getRunErrorOutput().isEmpty()) {
            judgeInfo.setCompileErrorOutput(executeCodeResponse.getRunErrorOutput().get(0));
        }
        if (!executeCodeResponse.getCompileErrorOutput().isEmpty()) {
            judgeInfo.setCompileErrorOutput(executeCodeResponse.getCompileErrorOutput());
        }
        // 修改数据库中的判题结果
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        if ("Wrong Answer".equals(judgeInfo.getMessage())) {
            questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.FAILED.getValue());
        } else if ("编译错误".equals(judgeInfo.getMessage())) {
            questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.COMPILE_ERROR.getValue());
        } else {
            questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCESS.getValue());
            String key = QuestionConstant.QUESTION_ACCEPTED_NUMBER + questionId;
            stringRedisTemplate.opsForValue().increment(key);
        }
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        questionSubmitService.updateById(questionSubmitUpdate);

        streamBridge.send("judgeResult-out-0", new EventMessage(questionSubmit.getUserId().toString(), "judgeResult", judgeInfo));
        return true;
    }

    @Data
    @AllArgsConstructor
    class EventMessage {
        private String userId;
        private String eventType;
        private Object data;
    }
}

