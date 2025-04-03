package com.example.gem_fan_club_web.repository.quote;

import com.example.gem_fan_club_web.model.quote.Quote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteRepository extends JpaRepository<Quote, Long> {
    List<Quote> findByUserIdOrderByCreatedAtDesc(String userId);
}