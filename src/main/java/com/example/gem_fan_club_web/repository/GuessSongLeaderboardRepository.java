package com.example.gem_fan_club_web.repository;

import com.example.gem_fan_club_web.model.GuessSongLeaderboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuessSongLeaderboardRepository extends JpaRepository<GuessSongLeaderboard, Long> {
    List<GuessSongLeaderboard> findTop10ByOrderByScoreDescCreateTimeAsc();
}
