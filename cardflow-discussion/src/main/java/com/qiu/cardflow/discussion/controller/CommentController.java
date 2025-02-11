package com.qiu.cardflow.discussion.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiu.cardflow.common.api.BaseResponse;
import com.qiu.cardflow.common.api.DeleteRequest;
import com.qiu.cardflow.common.api.ErrorCode;
import com.qiu.cardflow.common.api.UserContext;
import com.qiu.cardflow.discussion.exception.BusinessException;
import com.qiu.cardflow.discussion.exception.ThrowUtils;
import com.qiu.cardflow.discussion.model.dto.comment.CommentAddRequest;
import com.qiu.cardflow.discussion.model.dto.comment.CommentQueryRequest;
import com.qiu.cardflow.discussion.model.entity.Comment;
import com.qiu.cardflow.discussion.model.vo.CommentVO;
import com.qiu.cardflow.discussion.service.CommentService;
import com.qiu.cardflow.discussion.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;


/**
 * 题目接口
 */
@RestController
@RequestMapping("/comment")
@Slf4j
public class CommentController {

    @Resource
    private CommentService commentService;

    @Resource
    private UserService userService;


    // region 增删改查

    /**
     * 创建
     *
     * @param commentAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addComment(@RequestBody CommentAddRequest commentAddRequest, HttpServletRequest request) {
        if (commentAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Comment comment = new Comment();
        BeanUtils.copyProperties(commentAddRequest, comment);

        // todo 无效参数校验

        comment.setUserId(UserContext.getUserId());


        boolean result = commentService.save(comment);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newCommentId = comment.getId();
        return BaseResponse.success(newCommentId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteComment(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        // 判断是否存在
        Comment oldComment = commentService.getById(id);
        ThrowUtils.throwIf(oldComment == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldComment.getUserId().equals(UserContext.getUserId()) && !UserContext.isAdmin()) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = commentService.removeById(id);
        return BaseResponse.success(b);
    }


    /**
     * 分页获取题解列表
     *
     * @param commentQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<CommentVO>> listCommentPageVOByPage(@RequestBody CommentQueryRequest commentQueryRequest,
                                                                 HttpServletRequest request) {
        long current = commentQueryRequest.getCurrent();
        long size = commentQueryRequest.getPageSize();
        Page<Comment> commentPage = commentService.page(new Page<>(current, 20), commentService.getQueryWrapper(commentQueryRequest)
        );
        Page<CommentVO> commentPageVO = commentService.getCommentPageVO(commentPage, request);
        return BaseResponse.success(commentPageVO);
    }

    /**
     * 给评论点赞
     *
     * @param id
     * @param httpServletRequest
     * @return
     */
    @PutMapping("/like/{id}")
    public BaseResponse likeComment(@PathVariable("id") Long id, HttpServletRequest httpServletRequest) {
        Boolean success = commentService.likeComment(id, httpServletRequest);
        return BaseResponse.success(null);
    }


}
