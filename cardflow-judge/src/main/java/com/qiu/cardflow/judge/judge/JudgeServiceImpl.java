package com.qiu.cardflow.judge.judge;

import cn.hutool.json.JSONUtil;
import com.qiu.cardflow.common.constant.QuestionConstant;
import com.qiu.cardflow.common.exception.Asserts;
import com.qiu.cardflow.judge.CodeSandBoxService;
import com.qiu.cardflow.judge.judge.codesandbox.model.ExecuteCodeRequest;
import com.qiu.cardflow.judge.judge.codesandbox.model.ExecuteCodeResponse;
import com.qiu.cardflow.judge.judge.codesandbox.model.JudgeInfo;
import com.qiu.cardflow.judge.judge.strategy.JudgeContext;
import com.qiu.cardflow.judge.model.dto.question.JudgeCase;
import com.qiu.cardflow.judge.model.entity.Question;
import com.qiu.cardflow.judge.model.entity.QuestionSubmit;
import com.qiu.cardflow.judge.model.enums.QuestionSubmitStatusEnum;
import com.qiu.cardflow.judge.service.QuestionService;
import com.qiu.cardflow.judge.service.QuestionSubmitService;
import com.qiu.cardflow.redis.starter.key.EventStreamKeyBuilder;
import com.qiu.codeflow.eventStream.dto.EventMessage;
import com.qiu.codeflow.eventStream.dto.EventType;
import com.qiu.codeflow.eventStream.message.EventStreamMessageConstant;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    private RabbitTemplate rabbitTemplate;

    @Resource
    private StreamBridge streamBridge;

    @Resource
    private EventStreamKeyBuilder eventStreamKeyBuilder;

    @Value("${codesandbox.type:example}")
    private String type;


    @Override
    public Boolean doJudge(String questionSubmitIdAndRequestId) throws IOException, InterruptedException {
        String[] split = questionSubmitIdAndRequestId.split(",");
        Long questionSubmitId = Long.parseLong(split[0]);
        String requestId = split[1];
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

        executeCodeResponse = codeSandBoxService.executeCode(executeCodeRequest);


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

        // todo 把错误的测试用例放在前面
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
        EventMessage eventMessage = EventMessage.builder()
                .userId(questionSubmit.getUserId().toString())
                .eventType(EventType.JUDGE_RESULT)
                .requestId(requestId)
                .data(judgeInfo)
                .build();
        // 发送消息到消息队列
        String userToEventStreamKey = eventStreamKeyBuilder.buildUserToEventStreamKey(questionSubmit.getUserId().toString());
        String routingKey = stringRedisTemplate.opsForValue().get(userToEventStreamKey);
        if (routingKey != null) {
            rabbitTemplate.convertAndSend(EventStreamMessageConstant.EVENT_STREAM_EXCHANGE, routingKey, eventMessage);
        }
//        streamBridge.send("eventStream-out-0", eventMessage);
        return true;
    }

}

