package com.example.gem_fan_club_web.repository;

import com.example.gem_fan_club_web.model.LyricsChainLeaderboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LyricsChainLeaderboardRepository extends JpaRepository<LyricsChainLeaderboard, Long> {
    List<LyricsChainLeaderboard> findTop10ByOrderByScoreDescCreateTimeAsc();
} 