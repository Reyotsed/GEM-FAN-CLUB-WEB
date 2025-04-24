package com.example.gem_fan_club_web.repository.quote;

import com.example.gem_fan_club_web.model.quote.Quote;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuoteRepository extends JpaRepository<Quote, Long> {
    List<Quote> findByUserIdOrderByCreatedAtDesc(String userId);

    @Query("SELECT q FROM Quote q ORDER BY FUNCTION('RAND')")
    List<Quote> findRandomQuotes(Pageable pageable);

    @Query("SELECT q FROM Quote q WHERE q.quoteId NOT IN :ids ORDER BY FUNCTION('RAND')")
    List<Quote> findRandomQuotesExcluding(@Param("ids") List<Integer> ids, Pageable pageable);
}