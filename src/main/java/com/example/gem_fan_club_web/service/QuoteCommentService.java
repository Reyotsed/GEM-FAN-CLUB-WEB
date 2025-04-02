package com.example.gem_fan_club_web.service;

import com.example.gem_fan_club_web.model.quote.QuoteComment;
import com.example.gem_fan_club_web.repository.quote.QuoteCommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class QuoteCommentService {

    @Autowired
    private QuoteCommentRepository quoteCommentRepository;

    /**
     * 添加评论
     */
    public QuoteComment addComment(Long quoteId, String userId, String content, Long parentId) {
        QuoteComment comment = new QuoteComment();
        comment.setQuoteId(quoteId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setParentId(parentId);
        comment.setCreatedAt(new Date());
        comment.setLikeNum(0);
        comment.setStatus(1);
        return quoteCommentRepository.save(comment);
    }

    /**
     * 获取语录的评论列表
     */
    public List<QuoteComment> getCommentsByQuoteId(Long quoteId) {
        return quoteCommentRepository.findByQuoteIdAndStatusOrderByCreatedAtDesc(quoteId, 1);
    }

    /**
     * 获取评论的回复列表
     */
    public List<QuoteComment> getRepliesByCommentId(Long commentId) {
        return quoteCommentRepository.findByParentIdAndStatusOrderByCreatedAtAsc(commentId, 1);
    }

    /**
     * 获取用户的评论列表
     */
    public List<QuoteComment> getCommentsByUserId(String userId) {
        return quoteCommentRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, 1);
    }

    /**
     * 点赞评论
     */
    public void likeComment(Long commentId) {
        quoteCommentRepository.incrementLikeNum(commentId);
    }

    /**
     * 删除评论（软删除）
     */
    public void deleteComment(Long commentId) {
        quoteCommentRepository.softDelete(commentId);
    }

    /**
     * 获取评论详情
     */
    public QuoteComment getCommentById(Long commentId) {
        return quoteCommentRepository.findById(commentId).orElse(null);
    }
} 