package com.example.gem_fan_club_web.repository.quote;

import com.example.gem_fan_club_web.model.quote.QuoteComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface QuoteCommentRepository extends JpaRepository<QuoteComment, Long> {
    
    // 根据语录ID查询评论列表
    List<QuoteComment> findByQuoteIdAndStatusOrderByCreatedAtDesc(Integer quoteId, Integer status);
    
    // 根据父评论ID查询回复列表
    List<QuoteComment> findByParentIdAndStatusOrderByCreatedAtAsc(Long parentId, Integer status);
    
    // 根据用户ID查询评论列表
    List<QuoteComment> findByUserIdAndStatusOrderByCreatedAtDesc(String userId, Integer status);
    
    // 更新评论点赞数
    @Modifying
    @Transactional
    @Query("UPDATE QuoteComment q SET q.likeNum = q.likeNum + 1 WHERE q.commentId = ?1")
    void incrementLikeNum(Long commentId);
    
    // 软删除评论
    @Modifying
    @Transactional
    @Query("UPDATE QuoteComment q SET q.status = 0 WHERE q.commentId = ?1")
    void softDelete(Long commentId);
} 