package com.qiu.cardflow.question.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.qiu.cardflow.codesandbox.message.CodeSandBoxToEventStreamMessage;
import com.qiu.cardflow.judge.message.JudgeAMQPConfig;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiu.cardflow.codesandbox.dto.ExecuteCodeRequest;
import com.qiu.cardflow.codesandbox.dto.ExecuteCodeResponse;
import com.qiu.cardflow.codesandbox.dto.JudgeInfo;
import com.qiu.cardflow.codesandbox.feign.CodeSandBoxService;
import com.qiu.cardflow.common.api.ResultCode;
import com.qiu.cardflow.common.api.UserContext;
import com.qiu.cardflow.common.constant.CommonConstant;
import com.qiu.cardflow.common.constant.EventConstant;
import com.qiu.cardflow.common.constant.QuestionConstant;
import com.qiu.cardflow.common.exception.Asserts;
import com.qiu.cardflow.question.constant.QuestionSubmitConstant;
import com.qiu.cardflow.question.mapper.QuestionSubmitMapper;
import com.qiu.cardflow.question.model.dto.question.JudgeCase;
import com.qiu.cardflow.question.model.dto.questionsubmint.DebugCodeRequest;
import com.qiu.cardflow.question.model.dto.questionsubmint.QuestionSubmitAddRequest;
import com.qiu.cardflow.question.model.dto.questionsubmint.QuestionSubmitQueryRequest;
import com.qiu.cardflow.question.model.dto.questionsubmint.QuestionSubmitResponse;
import com.qiu.cardflow.question.model.entity.Question;
import com.qiu.cardflow.question.model.entity.QuestionSubmit;
import com.qiu.cardflow.question.model.enums.QuestionSubmitLanguageEnum;
import com.qiu.cardflow.question.model.enums.QuestionSubmitStatusEnum;
import com.qiu.cardflow.question.model.vo.ExecuteCodeResponseVO;
import com.qiu.cardflow.question.model.vo.QuestionSubmitStateVO;
import com.qiu.cardflow.question.model.vo.QuestionSubmitVO;
import com.qiu.cardflow.question.model.vo.QuestionSubmitWithTagVO;
import com.qiu.cardflow.question.model.vo.questionSubmit.QuestionSubmitPageVO;
import com.qiu.cardflow.question.service.QuestionService;
import com.qiu.cardflow.question.service.QuestionSubmitService;
import com.qiu.cardflow.question.utils.SqlUtils;
import com.qiu.codeflow.eventStream.dto.EventMessage;
import com.qiu.codeflow.eventStream.dto.EventType;
import com.qiu.codeflow.eventStream.util.EventMessageUtil;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;

/**
 * @author 10692
 * @description 针对表【question_submit(题目提交)】的数据库操作Service实现
 * @createDate 2023-12-11 19:31:25
 */
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
        implements QuestionSubmitService {

    @Resource
    private QuestionService questionService;

    @Resource
    private StreamBridge streamBridge;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

//    @Resource
//    private CodeSandBoxService codeSandBoxService;

    @Resource
    private RabbitTemplate rabbitTemplate;

    // 自定义线程池
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest
     * @return 提交记录的id
     */
    @Override
    public QuestionSubmitResponse doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, Long userId) {
        String requestId = EventMessageUtil.generateRequestId();
        // 校验编程语言是否合法
        String language = questionSubmitAddRequest.getLanguage();
        QuestionSubmitLanguageEnum languageEnum = QuestionSubmitLanguageEnum.getEnumByValue(language);
        Asserts.failIf(languageEnum == null, "编程语言错误");

        long questionId = questionSubmitAddRequest.getQuestionId();
        // 获取题目相关信息
        Question question = questionService.getById(questionId);
        Asserts.failIf(question == null, ResultCode.FAILED);

        QuestionSubmit questionSubmit = QuestionSubmit.builder()
                .userId(userId)
                .questionId(questionId)
                .code(questionSubmitAddRequest.getCode())
                .language(language)
                .status(QuestionSubmitStatusEnum.WAITING.getValue())
                .judgeInfo("{}")
                .build();

        boolean save = this.save(questionSubmit);
        Asserts.failIf(!save, "数据插入失败");

        // 题目提交数加1
        String key = QuestionConstant.QUESTION_SUBMIT_NUMBER;
        stringRedisTemplate.opsForZSet().incrementScore(key, String.valueOf(questionId), 1);
        Long questionSubmitId = questionSubmit.getId();
        // 异步执行判题服务
//        streamBridge.send(EventConstant.QUESTION_SUBMIT, questionSubmitId + "," + requestId);
        rabbitTemplate.convertAndSend(JudgeAMQPConfig.JUDGE_EXCHANGE, "", questionSubmitId + "," + requestId);
        return new QuestionSubmitResponse(requestId, questionSubmitId);
    }

    /**
     * 获取查询包装类
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        if (questionSubmitQueryRequest == null) {
            return queryWrapper;
        }

        String language = questionSubmitQueryRequest.getLanguage();
        Integer status = questionSubmitQueryRequest.getStatus();
        Long questionId = questionSubmitQueryRequest.getQuestionId();
        Long userId = questionSubmitQueryRequest.getUserId();
        String sortField = questionSubmitQueryRequest.getSortField();
        String sortOrder = questionSubmitQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.eq(QuestionSubmitLanguageEnum.getEnumByValue(language) != null, "language", language);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq(QuestionSubmitStatusEnum.getEnumByValue(status) != null, "status", status);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit) {
        QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
        // 仅本人和管理员能看见自己提交的代码
        if (!UserContext.getUserId().equals(questionSubmit.getUserId()) && !UserContext.isAdmin()) {
            questionSubmitVO.setCode(null);
        }
        return questionSubmitVO;
    }

    @Override
    public Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage) {
        List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
        Page<QuestionSubmitVO> questionSubmitVOPage = new Page<>(questionSubmitPage.getCurrent(),
                questionSubmitPage.getSize(), questionSubmitPage.getTotal());
        if (CollectionUtils.isEmpty(questionSubmitList)) {
            return questionSubmitVOPage;
        }
        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList.stream().map(this::getQuestionSubmitVO)
                .collect(Collectors.toList());
        questionSubmitVOPage.setRecords(questionSubmitVOList);
        return questionSubmitVOPage;
    }

    @Override
    public Integer getQuestionSubmitState(Long questionSubmitId) {
        String submitStateKey = QuestionSubmitConstant.QUESTION_SUBMIT_STATE_KEY + questionSubmitId;
        String res = stringRedisTemplate.opsForValue().get(submitStateKey);
        if (StringUtils.isBlank(res)) {
            res = "0";
        }
        return Integer.parseInt(res);
    }

    @Override
    public QuestionSubmitStateVO getJudgeInformation(Long questionSubmitId) {
        QuestionSubmit questionSubmit = getById(questionSubmitId);
        QuestionSubmitStateVO questionSubmitStateVO = new QuestionSubmitStateVO();
        questionSubmitStateVO.setStatus(questionSubmit.getStatus());
        questionSubmitStateVO.setJudgeInfo(JSONUtil.parse(questionSubmit.getJudgeInfo()).toBean(JudgeInfo.class));
        return questionSubmitStateVO;
    }

    @Override
    public Page<QuestionSubmitPageVO> listQuestionSubmitRecord(Long questionId, Integer current, Integer size) {
        Long userId = UserContext.getUserId();

        LambdaQueryWrapper<QuestionSubmit> wrapper = Wrappers.lambdaQuery(QuestionSubmit.class)
                .eq(QuestionSubmit::getUserId, userId)
                .eq(QuestionSubmit::getQuestionId, questionId)
                .select(QuestionSubmit::getStatus, QuestionSubmit::getId, QuestionSubmit::getLanguage,
                        QuestionSubmit::getJudgeInfo, QuestionSubmit::getCreateTime)
                .orderByDesc(QuestionSubmit::getCreateTime);

        // 从数据库中获取原始的分页数据
        Page<QuestionSubmit> questionSubmitPage = page(new Page<>(current, size), wrapper);

        List<QuestionSubmitPageVO> questionSubmitPageVOList = questionSubmitPage.getRecords().stream()
                .map(QuestionSubmitPageVO::objToVo)
                .collect(Collectors.toList());

        Page<QuestionSubmitPageVO> questionSubmitPageVOPage = new Page<>();
        BeanUtil.copyProperties(questionSubmitPage, questionSubmitPageVOPage);
        questionSubmitPageVOPage.setRecords(questionSubmitPageVOList);
        return questionSubmitPageVOPage;
    }

    @Override
    public String debugCode(DebugCodeRequest debugCodeRequest) throws IOException, InterruptedException {
        String requestId = EventMessageUtil.generateRequestId();
        String userId = UserContext.getUserId().toString();
        executorService.submit(() -> {
            Long questionId = debugCodeRequest.getQuestionId();
            String code = debugCodeRequest.getCode();
            String language = debugCodeRequest.getLanguage();
            String testCase = debugCodeRequest.getTestCase();
            // 如果用户没输入测试用例，获取默认的测试用例
            if (StrUtil.isBlank(testCase)) {
                Question question = questionService.getById(questionId);
                List<JudgeCase> judgeCaseList = JSONUtil.toList(question.getJudgeCase(), JudgeCase.class);
                testCase = judgeCaseList.get(0).getInput();
            }

            ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
            executeCodeRequest.setLanguage(language);
            executeCodeRequest.setCode(code);
            executeCodeRequest.setInputList(List.of(testCase));
            ExecuteCodeResponse executeCodeResponse = null;

            CodeSandBoxToEventStreamMessage codeSandBoxToEventStreamMessage = CodeSandBoxToEventStreamMessage.builder()
                    .userId(userId)
                    .requestId(requestId)
                    .executeCodeRequest(executeCodeRequest)
                    .eventType(EventType.JUDGE_RESULT)
                    .build();

            rabbitTemplate.convertAndSend("codesandbox.exchange", "toEventStream", codeSandBoxToEventStreamMessage);
//            try {
//                executeCodeResponse = codeSandBoxService.executeCode(executeCodeRequest);
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }

//            ExecuteCodeResponseVO executeCodeResponseVO = new ExecuteCodeResponseVO();
//            BeanUtil.copyProperties(executeCodeResponse, executeCodeResponseVO);
//
////            executeCodeResponseVO.setTestCase(testCase);
//
//            EventMessage eventMessage = EventMessage.builder()
//                    .data(executeCodeResponseVO)
//                    .userId(userId)
//                    .eventType(EventType.JUDGE_RESULT)
//                    .requestId(requestId)
//                    .build();
//            streamBridge.send("eventMessage-out-0", eventMessage);
        });

        return requestId;
    }

    @Override
    public List<QuestionSubmitWithTagVO> listQuestionSubmit(Integer number) {

        LambdaQueryWrapper<QuestionSubmit> lambdaQueryWrapper = Wrappers.lambdaQuery(QuestionSubmit.class)
                .select(QuestionSubmit::getLanguage, QuestionSubmit::getCreateTime, QuestionSubmit::getJudgeInfo,
                        QuestionSubmit::getStatus, QuestionSubmit::getQuestionId)
                .eq(QuestionSubmit::getUserId, UserContext.getUserId())
                .orderByDesc(QuestionSubmit::getCreateTime)
                .last("limit " + number);

        List<QuestionSubmit> list = list(lambdaQueryWrapper);

        List<Long> quesitonIDs = list.stream()
                .map(QuestionSubmit::getQuestionId)
                .distinct()
                .toList();

        List<Question> questions = questionService.listByIds(quesitonIDs);
        Map<Long, String> idTagMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, Question::getTags));

        List<QuestionSubmitWithTagVO> voList = list.stream()
                .map(
                        questionSubmit -> {
                            QuestionSubmitWithTagVO questionSubmitWithTagVO = new QuestionSubmitWithTagVO();
                            BeanUtil.copyProperties(questionSubmit, questionSubmitWithTagVO);
                            questionSubmitWithTagVO.setTags(idTagMap.get(questionSubmit.getQuestionId()));
                            return questionSubmitWithTagVO;
                        })
                .toList();

        return voList;
    }

}