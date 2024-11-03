package com.qiu.qoj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qiu.qoj.model.dto.comment.CommentQueryRequest;
import com.qiu.qoj.model.entity.Comment;
import com.qiu.qoj.model.vo.CommentVO;
import jakarta.servlet.http.HttpServletRequest;


/**
 * @author 10692
 * @description 针对表【comment(评论表)】的数据库操作Service
 * @createDate 2023-12-31 11:52:53
 */
public interface CommentService extends IService<Comment> {

    QueryWrapper<Comment> getQueryWrapper(CommentQueryRequest commentQueryRequest);

    Page<CommentVO> getCommentPageVO(Page<Comment> commentPage, HttpServletRequest request);

    Boolean likeComment(Long commentId, HttpServletRequest httpServletRequest);


}
