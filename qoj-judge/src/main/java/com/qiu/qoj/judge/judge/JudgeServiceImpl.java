package com.qiu.qoj.judge.judge;

import cn.hutool.json.JSONUtil;
import com.qiu.qoj.judge.CodeSandBoxService;
import com.qiu.qoj.common.constant.QuestionConstant;
import com.qiu.qoj.common.constant.QuestionSubmitConstant;
import com.qiu.qoj.common.exception.Asserts;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
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

    @Value("${codesandbox.type:example}")
    private String type;


    @Override
    public Boolean doJudge(long questionSubmitId) {
        // 1）传入题目的提交 id，获取到对应的题目、提交信息（包含代码、编程语言等）
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
        Asserts.failIf(questionSubmit == null, "提交信息不存在");

        Long questionId = questionSubmit.getQuestionId();
        Question question = questionService.getById(questionId);
        Asserts.failIf(question == null, "题目不存在");

        // 2）如果题目提交状态不为等待中，就不用重复执行了
        Asserts.failIf(!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.WAITING.getValue()),
                "题目正在判题中");

        // 3）更改判题（题目提交）的状态为 "判题中"，防止重复执行
        String submitStateKey = QuestionSubmitConstant.QUESTION_SUBMIT_STATE_KEY + questionSubmit.getId();
        stringRedisTemplate.opsForValue().set(submitStateKey, QuestionSubmitStatusEnum.RUNNING.getValue().toString(), 5, TimeUnit.MINUTES);
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean update = questionSubmitService.updateById(questionSubmitUpdate);
        Asserts.failIf(!update, "题目状态更新错误");

        // 4）调用沙箱，获取到执行结果

        String language = questionSubmit.getLanguage();
        String code = questionSubmit.getCode();
        // 获取输入用例
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        List<String> outputList = executeCodeResponse.getRunOutput();

        // 5）根据沙箱的执行结果，设置题目的判题状态和信息
        JudgeContext judgeContext = new JudgeContext();
        JudgeInfo judgeInfo1 = new JudgeInfo();
        if (!executeCodeResponse.getTime().isEmpty()) {
            judgeInfo1.setTime(executeCodeResponse.getTime().get(0));
        }
        judgeContext.setJudgeInfo(judgeInfo1);
        judgeContext.setInputList(inputList);
        judgeContext.setOutputList(outputList);
        judgeContext.setJudgeCaseList(judgeCaseList);
        judgeContext.setQuestion(question);
        judgeContext.setQuestionSubmit(questionSubmit);
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
        // 6）修改数据库中的判题结果
        questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        if ("Wrong Answer".equals(judgeInfo.getMessage())) {
            questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.FAILED.getValue());
            stringRedisTemplate.opsForValue().set(submitStateKey, QuestionSubmitStatusEnum.FAILED.getValue().toString(), 5, TimeUnit.MINUTES);
        } else if ("编译错误".equals(judgeInfo.getMessage())) {
            questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.COMPILE_ERROR.getValue());
            stringRedisTemplate.opsForValue().set(submitStateKey, QuestionSubmitStatusEnum.COMPILE_ERROR.getValue().toString(), 5, TimeUnit.MINUTES);

        } else {
            questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCESS.getValue());
            stringRedisTemplate.opsForValue().set(submitStateKey, QuestionSubmitStatusEnum.SUCCESS.getValue().toString(), 5, TimeUnit.MINUTES);
            String key = QuestionConstant.QUESTION_ACCEPTED_NUMBER + questionId;
            stringRedisTemplate.opsForValue().increment(key);
        }
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        update = questionSubmitService.updateById(questionSubmitUpdate);
        Asserts.failIf(!update, "题目状态更新错误");
        return true;
    }
}
