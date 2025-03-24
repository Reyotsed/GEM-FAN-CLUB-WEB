package com.example.gem_fan_club_web.repository.quote;

import com.example.gem_fan_club_web.model.Song;
import com.example.gem_fan_club_web.model.quote.QuoteLike;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface QuoteLikeRepository extends JpaRepository<QuoteLike, Long> {
    @Modifying
    @Transactional
    void deleteByQuoteIdAndUserId(Long quoteId, String userId);

    QuoteLike findByQuoteIdAndUserId(Long quoteId, String userId);
}
