package com.qiu.qoj.discussion.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiu.qoj.common.constant.CommonConstant;
import com.qiu.qoj.common.api.UserContext;
import com.qiu.qoj.discussion.mapper.CommentMapper;
import com.qiu.qoj.discussion.model.dto.comment.CommentQueryRequest;
import com.qiu.qoj.discussion.model.entity.Comment;
import com.qiu.qoj.common.model.entity.User;
import com.qiu.qoj.discussion.model.vo.CommentVO;
import com.qiu.qoj.discussion.service.CommentService;
import com.qiu.qoj.discussion.service.UserService;
import com.qiu.qoj.discussion.utils.SqlUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 10692
 * @description 针对表【comment(评论表)】的数据库操作Service实现
 * @createDate 2023-12-31 11:52:53
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
        implements CommentService {

    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public QueryWrapper<Comment> getQueryWrapper(CommentQueryRequest commentQueryRequest) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        if (commentQueryRequest == null) {
            return queryWrapper;
        }

        Long id = commentQueryRequest.getId();
        String content = commentQueryRequest.getContent();
        Long userId = commentQueryRequest.getUserId();
        Long questionSolvingId = commentQueryRequest.getQuestionSolvingId();
        Long fatherCommentId = commentQueryRequest.getFatherCommentId();
        String sortField = commentQueryRequest.getSortField();
        String sortOrder = commentQueryRequest.getSortOrder();


        // 拼接查询条件
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);

        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionSolvingId), "questionSolvingId", questionSolvingId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(fatherCommentId), "fatherCommentId", fatherCommentId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public Page<CommentVO> getCommentPageVO(Page<Comment> commentPage, HttpServletRequest request) {
        List<Comment> commentList = commentPage.getRecords();
        Page<CommentVO> commentVOPage = new Page<>(commentPage.getCurrent(), commentPage.getSize(), commentPage.getTotal());
        if (CollUtil.isEmpty(commentList)) {
            return commentVOPage;
        }
        Long viewUserId = UserContext.getUserId();
        // 1. 关联查询用户信息
        Set<Long> userIdSet = commentList.stream().map(Comment::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
//        Map<Long, Boolean> commentIdHasThumbMap = new HashMap<>();
//        Map<Long, Boolean> commentIdHasFavourMap = new HashMap<>();
//        User loginUser = userService.getLoginUserPermitNull(request);
//        if (loginUser != null) {
//            Set<Long> commentIdSet = commentList.stream().map(Comment::getId).collect(Collectors.toSet());
//            loginUser = userService.getLoginUser(request);
//            // 获取点赞
//            QueryWrapper<CommentThumb> commentThumbQueryWrapper = new QueryWrapper<>();
//            commentThumbQueryWrapper.in("commentId", commentIdSet);
//            commentThumbQueryWrapper.eq("userId", loginUser.getId());
//            List<CommentThumb> commentCommentThumbList = commentThumbMapper.selectList(commentThumbQueryWrapper);
//            commentCommentThumbList.forEach(commentCommentThumb -> commentIdHasThumbMap.put(commentCommentThumb.getCommentId(), true));
//            // 获取收藏
//            QueryWrapper<CommentFavour> commentFavourQueryWrapper = new QueryWrapper<>();
//            commentFavourQueryWrapper.in("commentId", commentIdSet);
//            commentFavourQueryWrapper.eq("userId", loginUser.getId());
//            List<CommentFavour> commentFavourList = commentFavourMapper.selectList(commentFavourQueryWrapper);
//            commentFavourList.forEach(commentFavour -> commentIdHasFavourMap.put(commentFavour.getCommentId(), true));
//        }
        // 填充信息
        List<CommentVO> commentVOList = commentList.stream().map(comment -> {
            Long commentId = comment.getId();
            String key = CommonConstant.COMMENT_LIKED_KEY + commentId;
            CommentVO commentVO = new CommentVO();
            BeanUtils.copyProperties(comment, commentVO);
            Long userId = comment.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            commentVO.setUserVO(userService.getUserVO(user));
            Boolean liked = stringRedisTemplate.opsForSet().isMember(key, viewUserId.toString());
            commentVO.setLiked(liked);
//            commentVO.setHasThumb(commentIdHasThumbMap.getOrDefault(comment.getId(), false));
//            commentVO.setHasFavour(commentIdHasFavourMap.getOrDefault(comment.getId(), false));
            return commentVO;
        }).collect(Collectors.toList());
        commentVOPage.setRecords(commentVOList);
        return commentVOPage;
    }

    /**
     * 给评论点赞
     *
     * @param commentId          评论ID
     * @param httpServletRequest
     */
    @Override
    public Boolean likeComment(Long commentId, HttpServletRequest httpServletRequest) {
        String key = CommonConstant.COMMENT_LIKED_KEY + commentId;
        Long userId = UserContext.getUserId();
        // 用Redis判断用户是否已点赞
        Boolean existed = stringRedisTemplate.opsForSet().isMember(key, userId.toString());
        if (BooleanUtil.isFalse(existed)) {
            // 未点赞，那就可以点赞
            boolean successed = update().setSql("thumbNum = thumbNum + 1").eq("id", commentId).update();
            if (successed) {
                stringRedisTemplate.opsForSet().add(key, userId.toString());
            }
        } else {
            boolean successed = update().setSql("thumbNum = thumbNum - 1").eq("id", commentId).update();
            if (successed) {
                stringRedisTemplate.opsForSet().remove(key, userId.toString());
            }
        }
        return true;
    }
}




