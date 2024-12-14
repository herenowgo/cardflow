package com.qiu.qoj.question.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiu.qoj.codesandbox.dto.ExecuteCodeRequest;
import com.qiu.qoj.codesandbox.dto.ExecuteCodeResponse;
import com.qiu.qoj.codesandbox.dto.JudgeInfo;
import com.qiu.qoj.codesandbox.feign.CodeSandBoxService;
import com.qiu.qoj.common.api.ResultCode;
import com.qiu.qoj.common.api.UserContext;
import com.qiu.qoj.common.constant.CommonConstant;
import com.qiu.qoj.common.constant.EventConstant;
import com.qiu.qoj.common.constant.QuestionConstant;
import com.qiu.qoj.common.exception.Asserts;
import com.qiu.qoj.question.constant.QuestionSubmitConstant;
import com.qiu.qoj.question.mapper.QuestionSubmitMapper;
import com.qiu.qoj.question.model.dto.question.JudgeCase;
import com.qiu.qoj.question.model.dto.questionsubmint.DebugCodeRequest;
import com.qiu.qoj.question.model.dto.questionsubmint.QuestionSubmitAddRequest;
import com.qiu.qoj.question.model.dto.questionsubmint.QuestionSubmitQueryRequest;
import com.qiu.qoj.question.model.entity.Question;
import com.qiu.qoj.question.model.entity.QuestionSubmit;
import com.qiu.qoj.question.model.enums.QuestionSubmitLanguageEnum;
import com.qiu.qoj.question.model.enums.QuestionSubmitStatusEnum;
import com.qiu.qoj.question.model.vo.ExecuteCodeResponseVO;
import com.qiu.qoj.question.model.vo.QuestionSubmitStateVO;
import com.qiu.qoj.question.model.vo.QuestionSubmitVO;
import com.qiu.qoj.question.model.vo.QuestionSubmitWithTagVO;
import com.qiu.qoj.question.model.vo.questionSubmit.QuestionSubmitPageVO;
import com.qiu.qoj.question.service.QuestionService;
import com.qiu.qoj.question.service.QuestionSubmitService;
import com.qiu.qoj.question.utils.SqlUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import work.codeflow.eventStream.dto.EventMessage;
import work.codeflow.eventStream.dto.EventType;
import work.codeflow.eventStream.util.EventMessageUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Resource
    private CodeSandBoxService codeSandBoxService;

    // 自定义线程池
    private final ExecutorService executorService = Executors.newCachedThreadPool();


    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest
     * @return 提交记录的id
     */
    @Override
    public Long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, Long userId) {
        // 校验编程语言是否合法
        String language = questionSubmitAddRequest.getLanguage();
        QuestionSubmitLanguageEnum languageEnum = QuestionSubmitLanguageEnum.getEnumByValue(language);
        Asserts.failIf(languageEnum == null, "编程语言错误");

        long questionId = questionSubmitAddRequest.getQuestionId();
        // 判断实体是否存在，根据类别获取实体
        Question question = questionService.getById(questionId);
        Asserts.failIf(question == null, ResultCode.FAILED);

        // 是否已提交题目
        // 每个用户串行提交题目
        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setUserId(userId);
        questionSubmit.setQuestionId(questionId);
        questionSubmit.setCode(questionSubmitAddRequest.getCode());
        questionSubmit.setLanguage(language);
        // 设置初始状态
        questionSubmit.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());
        questionSubmit.setJudgeInfo("{}");
        boolean save = this.save(questionSubmit);
        Asserts.failIf(!save, "数据插入失败");

        // Redis中提交数加1
        String key = QuestionConstant.QUESTION_SUBMIT_NUMBER;
        stringRedisTemplate.opsForZSet().incrementScore(key, String.valueOf(questionId), 1);
        // 设置状态
        String submitStateKey = QuestionSubmitConstant.QUESTION_SUBMIT_STATE_KEY + questionSubmit.getId();
        stringRedisTemplate.opsForValue().set(submitStateKey, QuestionSubmitStatusEnum.WAITING.getValue().toString(), 5, TimeUnit.MINUTES);
        Long questionSubmitId = questionSubmit.getId();
        // 异步执行判题服务
        streamBridge.send(EventConstant.QUESTION_SUBMIT, questionSubmitId);
//        CompletableFuture.runAsync(() -> {
//            judgeService.doJudge(questionSubmitId);
//        });
//        HashMap<Object, Object> objectObjectHashMap = new HashMap<>(1);
//        objectObjectHashMap.put("questionSubmitId", questionSubmitId.toString());
////        stringRedisTemplate.opsForStream().add("stream.questionSubmit", objectObjectHashMap);
        // 同步执行判题服务
//        QuestionSubmit questionSubmitResult = judgeService.doJudge(questionSubmitId);

        return questionSubmitId;
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
        Page<QuestionSubmitVO> questionSubmitVOPage = new Page<>(questionSubmitPage.getCurrent(), questionSubmitPage.getSize(), questionSubmitPage.getTotal());
        if (CollectionUtils.isEmpty(questionSubmitList)) {
            return questionSubmitVOPage;
        }
        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList.stream().map(this::getQuestionSubmitVO).collect(Collectors.toList());
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
                .select(QuestionSubmit::getStatus, QuestionSubmit::getId, QuestionSubmit::getLanguage, QuestionSubmit::getJudgeInfo, QuestionSubmit::getCreateTime)
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
            try {
                executeCodeResponse = codeSandBoxService.executeCode(executeCodeRequest);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            ExecuteCodeResponseVO executeCodeResponseVO = new ExecuteCodeResponseVO();
            BeanUtil.copyProperties(executeCodeResponse, executeCodeResponseVO);

            executeCodeResponseVO.setTestCase(testCase);

            EventMessage eventMessage = EventMessage.builder()
                    .data(executeCodeResponseVO)
                    .userId(userId)
                    .eventType(EventType.JUDGE_RESULT)
                    .requestId(requestId)
                    .build();
            streamBridge.send("judgeResult-out-0", eventMessage);
        });


        return requestId;
    }

    @Override
    public List<QuestionSubmitWithTagVO> listQuestionSubmit(Integer number) {

        LambdaQueryWrapper<QuestionSubmit> lambdaQueryWrapper = Wrappers.lambdaQuery(QuestionSubmit.class)
                .select(QuestionSubmit::getLanguage, QuestionSubmit::getCreateTime, QuestionSubmit::getJudgeInfo, QuestionSubmit::getStatus, QuestionSubmit::getQuestionId)
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
                        }
                )
                .toList();

        return voList;
    }

}