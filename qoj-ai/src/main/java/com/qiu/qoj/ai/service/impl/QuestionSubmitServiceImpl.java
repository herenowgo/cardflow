package com.qiu.qoj.ai.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiu.qoj.ai.exception.BusinessException;
import com.qiu.qoj.ai.judge.JudgeService;
import com.qiu.qoj.ai.judge.codesandbox.model.JudgeInfo;
import com.qiu.qoj.ai.mapper.QuestionSubmitMapper;
import com.qiu.qoj.ai.model.dto.questionsubmint.QuestionSubmitAddRequest;
import com.qiu.qoj.ai.model.dto.questionsubmint.QuestionSubmitQueryRequest;
import com.qiu.qoj.ai.model.entity.Question;
import com.qiu.qoj.ai.model.entity.QuestionSubmit;
import com.qiu.qoj.ai.model.enums.QuestionSubmitLanguageEnum;
import com.qiu.qoj.ai.model.enums.QuestionSubmitStatusEnum;
import com.qiu.qoj.ai.model.vo.QuestionSubmitStateVO;
import com.qiu.qoj.ai.model.vo.QuestionSubmitVO;
import com.qiu.qoj.ai.service.QuestionService;
import com.qiu.qoj.ai.service.QuestionSubmitService;
import com.qiu.qoj.ai.utils.SqlUtils;
import com.qiu.qoj.common.api.ErrorCode;
import com.qiu.qoj.common.api.UserContext;
import com.qiu.qoj.common.constant.CommonConstant;
import com.qiu.qoj.common.constant.QuestionConstant;
import com.qiu.qoj.common.constant.QuestionSubmitConstant;
import com.qiu.qoj.common.model.entity.User;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
    @Lazy
    private JudgeService judgeService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final ExecutorService QUESTION_SUBMIT_EXECUTOR = Executors.newSingleThreadExecutor();


    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest
     * @param loginUser
     * @return 提交记录的id
     */
    @Override
    public Long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {
        // 校验编程语言是否合法
        String language = questionSubmitAddRequest.getLanguage();
        QuestionSubmitLanguageEnum languageEnum = QuestionSubmitLanguageEnum.getEnumByValue(language);
        if (languageEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编程语言错误");
        }
        long questionId = questionSubmitAddRequest.getQuestionId();
        // 判断实体是否存在，根据类别获取实体
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 是否已提交题目
        long userId = loginUser.getId();
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

        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据插入失败");
        }
        // Redis中提交数加1
        String key = QuestionConstant.QUESTION_SUBMIT_NUMBER;
        stringRedisTemplate.opsForZSet().incrementScore(key, String.valueOf(questionId), 1);
        // 设置状态
        String submitStateKey = QuestionSubmitConstant.QUESTION_SUBMIT_STATE_KEY + questionSubmit.getId();
        stringRedisTemplate.opsForValue().set(submitStateKey, QuestionSubmitStatusEnum.WAITING.getValue().toString(), 5, TimeUnit.MINUTES);
        Long questionSubmitId = questionSubmit.getId();
        // 异步执行判题服务
        CompletableFuture.runAsync(() -> {
            judgeService.doJudge(questionSubmitId);
        });
        HashMap<Object, Object> objectObjectHashMap = new HashMap<>(1);
        objectObjectHashMap.put("questionSubmitId", questionSubmitId.toString());
//        stringRedisTemplate.opsForStream().add("stream.questionSubmit", objectObjectHashMap);
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
    public QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser) {
        QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
        Long userId = loginUser.getId();
        // 仅本人和管理员能看见自己提交的代码
        if (!userId.equals(questionSubmit.getUserId()) && !UserContext.isAdmin()) {
            questionSubmitVO.setCode(null);
        }
        return questionSubmitVO;
    }

    @Override
    public Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser) {
        List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
        Page<QuestionSubmitVO> questionSubmitVOPage = new Page<>(questionSubmitPage.getCurrent(), questionSubmitPage.getSize(), questionSubmitPage.getTotal());
        if (CollUtil.isEmpty(questionSubmitList)) {
            return questionSubmitVOPage;
        }
        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList.stream().map(questionSubmit -> getQuestionSubmitVO(questionSubmit, loginUser)).collect(Collectors.toList());
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

}




