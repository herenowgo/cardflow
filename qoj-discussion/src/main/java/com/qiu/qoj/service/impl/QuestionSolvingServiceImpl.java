package com.qiu.qoj.service.impl;

import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiu.qoj.constant.CommonConstant;
import com.qiu.qoj.constant.QuestionSolvingConstant;
import com.qiu.qoj.domain.UserContext;
import com.qiu.qoj.mapper.QuestionSolvingMapper;
import com.qiu.qoj.model.dto.questionsolving.QuestionSolvingQueryRequest;
import com.qiu.qoj.model.entity.QuestionSolving;
import com.qiu.qoj.model.entity.User;
import com.qiu.qoj.model.vo.QuestionSolvingPageVO;
import com.qiu.qoj.model.vo.UserVO;
import com.qiu.qoj.service.QuestionSolvingService;
import com.qiu.qoj.service.UserService;
import com.qiu.qoj.utils.SqlUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 10692
 * @description 针对表【question_solving(题解)】的数据库操作Service实现
 * @createDate 2023-12-21 10:24:02
 */
@Service
public class QuestionSolvingServiceImpl extends ServiceImpl<QuestionSolvingMapper, QuestionSolving>
        implements QuestionSolvingService {


    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    /**
     * 检验题目是否合法
     *
     * @param question
     * @param add
     */


    /**
     * 获取查询包装类
     *
     * @param questionSolvingRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionSolving> getQueryWrapper(QuestionSolvingQueryRequest questionSolvingRequest) {
        QueryWrapper<QuestionSolving> queryWrapper = new QueryWrapper<>();
        if (questionSolvingRequest == null) {
            return queryWrapper;
        }

        Long id = questionSolvingRequest.getId();
        Long questionId = questionSolvingRequest.getQuestionId();
        String title = questionSolvingRequest.getTitle();
        String text = questionSolvingRequest.getText();
        Long userId = questionSolvingRequest.getUserId();
        String sortField = questionSolvingRequest.getSortField();
        String sortOrder = questionSolvingRequest.getSortOrder();


        // 拼接查询条件
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(text), "text", text);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public QuestionSolving getUserQuestionSolving(Long questionId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        LambdaQueryWrapper<QuestionSolving> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QuestionSolving::getQuestionId, questionId);
        queryWrapper.eq(QuestionSolving::getUserId, userId);
        return this.getOne(queryWrapper);
    }

    @Override
    public Page<QuestionSolvingPageVO> getQuestionSolvingPageVO(Page<QuestionSolving> questionSolvingPage, HttpServletRequest request) {
        List<QuestionSolving> questionSolvingList = questionSolvingPage.getRecords();
        Page<QuestionSolvingPageVO> questionVOPage = new Page<>(questionSolvingPage.getCurrent(), questionSolvingPage.getSize(), questionSolvingPage.getTotal());
        ArrayList<QuestionSolvingPageVO> pageVOList = new ArrayList<>();
        for (QuestionSolving questionSolving : questionSolvingList) {
            QuestionSolvingPageVO questionSolvingPageVO = new QuestionSolvingPageVO();
            BeanUtils.copyProperties(questionSolving, questionSolvingPageVO);
            User user = userService.getById(questionSolving.getUserId());
            UserVO userVO = userService.getUserVO(user);
            questionSolvingPageVO.setUserVO(userVO);
            String key = QuestionSolvingConstant.QUESTION_SOLVING_PAGE_VIEW_KEY + questionSolving.getId();
            String pageView = stringRedisTemplate.opsForValue().get(key);
            if (pageView != null) {
                questionSolvingPageVO.setPageView(Integer.parseInt(pageView));
            }

            pageVOList.add(questionSolvingPageVO);
        }
        questionVOPage.setRecords(pageVOList);


        return questionVOPage;
    }

    @Override
    public Boolean likeQuestionSolving(Long questionSolvingId, HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        String key = QuestionSolvingConstant.QUESTION_SOLVING_LIKED_KEY + questionSolvingId;
        Long userId = loginUser.getId();
        // 用Redis判断用户是否已支持
        Boolean existed = stringRedisTemplate.opsForSet().isMember(key, userId.toString());
        if (BooleanUtil.isFalse(existed)) {
            // 未支持，那就可以支持
            boolean successed = update().setSql("supportNumber = supportNumber + 1").eq("id", questionSolvingId).update();
            if (successed) {
                stringRedisTemplate.opsForSet().add(key, userId.toString());
            }
        } else {
            boolean successed = update().setSql("supportNumber = supportNumber - 1").eq("id", questionSolvingId).update();
            if (successed) {
                stringRedisTemplate.opsForSet().remove(key, userId.toString());
            }
        }
        return true;
    }

    @Override
    public Boolean isSupported(Long questionSolvingId, HttpServletRequest httpServletRequest) {

        String key = QuestionSolvingConstant.QUESTION_SOLVING_LIKED_KEY + questionSolvingId;
        Long userId = UserContext.getUserId();
        // 用Redis判断用户是否已支持
        Boolean supported = stringRedisTemplate.opsForSet().isMember(key, userId.toString());
        return supported;
    }
}




