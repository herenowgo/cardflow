package com.qiu.cardflow.question.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiu.cardflow.common.api.ResultCode;
import com.qiu.cardflow.common.constant.CommonConstant;
import com.qiu.cardflow.common.exception.Asserts;
import com.qiu.cardflow.question.constant.QuestionConstant;
import com.qiu.cardflow.question.mapper.QuestionMapper;
import com.qiu.cardflow.question.model.dto.question.QuestionQueryRequest;
import com.qiu.cardflow.question.model.entity.Question;
import com.qiu.cardflow.question.model.vo.QuestionVO;
import com.qiu.cardflow.question.service.QuestionService;
import com.qiu.cardflow.question.utils.CacheClient;
import com.qiu.cardflow.question.utils.SqlUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author 10692
 * @description 针对表【question(题目)】的数据库操作Service实现
 * @createDate 2023-12-11 19:30:57
 */
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
        implements QuestionService {


    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CacheClient cacheClient;


    /**
     * 检验题目是否合法
     *
     * @param question
     * @param add
     */
    @Override
    public void validQuestion(Question question, boolean add) {
        Asserts.failIf(question == null, ResultCode.VALIDATE_FAILED);

        String title = question.getTitle();
        String content = question.getContent();
        String tags = question.getTags();
        String answer = question.getAnswer();

        String judgeCase = question.getJudgeCase();
        String judgeConfig = question.getJudgeConfig();


        // 创建时，参数不能为空
        if (add) {
            Asserts.failIf(StrUtil.hasBlank(title, content, tags), "请求参数错误");
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(title) && title.length() > 80) {
            Asserts.fail("标题过长");
        }
        if (StringUtils.isNotBlank(content) && content.length() > 8192) {
            Asserts.fail("内容过长");
        }
        if (StringUtils.isNotBlank(answer) && answer.length() > 8192) {
            Asserts.fail("答案过长");
        }
        if (StringUtils.isNotBlank(judgeCase) && judgeCase.length() > 8192) {
            Asserts.fail("判题用例过长");
        }
        if (StringUtils.isNotBlank(judgeConfig) && judgeConfig.length() > 8192) {
            Asserts.fail("判题配置过长");
        }
    }

    /**
     * 获取查询包装类
     *
     * @param questionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        if (questionQueryRequest == null) {
            return queryWrapper;
        }

        Long id = questionQueryRequest.getId();
        String title = questionQueryRequest.getTitle();
        String content = questionQueryRequest.getContent();
        List<String> tags = questionQueryRequest.getTags();
        String answer = questionQueryRequest.getAnswer();
        Long userId = questionQueryRequest.getUserId();
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();


        // 拼接查询条件
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        queryWrapper.like(StringUtils.isNotBlank(answer), "answer", answer);
        if (CollectionUtils.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


    @Override
    public QuestionVO getQuestionVO(Question question, HttpServletRequest request) {
        QuestionVO questionVO = QuestionVO.objToVo(question);
        return questionVO;
    }

    @Override
    public Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request) {
        List<Question> questionList = questionPage.getRecords();
        Page<QuestionVO> questionVOPage = new Page<>(questionPage.getCurrent(), questionPage.getSize(), questionPage.getTotal());
        if (CollectionUtils.isEmpty(questionList)) {
            return questionVOPage;
        }
        // 1. 关联查询用户信息
//        Set<Long> userIdSet = questionList.stream().map(Question::getUserId).collect(Collectors.toSet());
//        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
//                .collect(Collectors.groupingBy(User::getId));
        // 填充信息
        List<QuestionVO> questionVOList = questionList.stream().map(question -> {
            QuestionVO questionVO = QuestionVO.objToVo(question);
//            Long userId = question.getUserId();
//            User user = null;
//            if (userIdUserListMap.containsKey(userId)) {
//                user = userIdUserListMap.get(userId).get(0);
//            }
//            questionVO.setUserVO(userService.getUserVO(user));
            Double submitNumber = stringRedisTemplate.opsForZSet().score(QuestionConstant.QUESTION_SUBMIT_NUMBER, question.getId().toString());
            if (submitNumber != null) {
                questionVO.setSubmitNum(submitNumber.intValue());
            }
            String acceptedNumber = stringRedisTemplate.opsForValue().get(QuestionConstant.QUESTION_ACCEPTED_NUMBER + question.getId());
            if (acceptedNumber != null) {
                questionVO.setAcceptedNum(Integer.parseInt(acceptedNumber));
            }
//            questionVO.setHasThumb(questionIdHasThumbMap.getOrDefault(question.getId(), false));
//            questionVO.setHasFavour(questionIdHasFavourMap.getOrDefault(question.getId(), false));
            return questionVO;
        }).collect(Collectors.toList());
        questionVOPage.setRecords(questionVOList);
        return questionVOPage;
    }

    @Override
    public List<QuestionVO> getTopFifty(HttpServletRequest httpServletRequest) {
        String key = QuestionConstant.QUESTION_SUBMIT_NUMBER;
        Set<String> questionIdSet = stringRedisTemplate.opsForZSet().reverseRangeByScore(key, 0, Integer.MAX_VALUE, 0, 49);
        List<Question> questions = listByIds(questionIdSet);

        Page<Question> questionPage = new Page<>();
        questionPage.setRecords(questions);
        Page<QuestionVO> questionVOPage = getQuestionVOPage(questionPage, httpServletRequest);
        List<QuestionVO> records = questionVOPage.getRecords();
        records.sort((a, b) -> b.getSubmitNum() - a.getSubmitNum());
        return records;

    }

    @Override
    public Question getByIdUseCache(long id) {
        return cacheClient.queryWithPassThrough(QuestionConstant.CACHE_QUESTION_KEY, id, Question.class, this::getById, QuestionConstant.CACHE_QUESTION_TTL, TimeUnit.DAYS);
    }

    @Override
    public Page<Question> simplePageUseCache(long current, long size) {
        QuestionQueryRequest questionQueryRequest = new QuestionQueryRequest();
        questionQueryRequest.setSortField("title");
        questionQueryRequest.setSortOrder("ascend");

        return page(new Page<>(current, size), getQueryWrapper(questionQueryRequest));
    }
}




