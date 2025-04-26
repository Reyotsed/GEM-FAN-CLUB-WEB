package com.example.gem_fan_club_web.service;

import com.example.gem_fan_club_web.model.LyricsChainLeaderboard;
import com.example.gem_fan_club_web.repository.LyricsChainLeaderboardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LyricsChainLeaderboardService {

    private final LyricsChainLeaderboardRepository lyricsChainLeaderboardRepository;

    @Autowired
    public LyricsChainLeaderboardService(LyricsChainLeaderboardRepository lyricsChainLeaderboardRepository) {
        this.lyricsChainLeaderboardRepository = lyricsChainLeaderboardRepository;
    }

    public LyricsChainLeaderboard saveScore(String nickname, Integer score, Integer completionTime, Integer diffLevel) {
        LyricsChainLeaderboard leaderboard = new LyricsChainLeaderboard();
        leaderboard.setNickname(nickname);
        leaderboard.setScore(score);
        leaderboard.setCreateTime(LocalDateTime.now());
        leaderboard.setCompletionTime(completionTime);
        leaderboard.setDiffLevel(diffLevel);
        return lyricsChainLeaderboardRepository.save(leaderboard);
    }

    public LyricsChainLeaderboard saveScore(String nickname, Integer score, Integer completionTime, Integer diffLevel, String userId) {
        LyricsChainLeaderboard leaderboard = new LyricsChainLeaderboard();
        leaderboard.setNickname(nickname);
        leaderboard.setScore(score);
        leaderboard.setCreateTime(LocalDateTime.now());
        leaderboard.setCompletionTime(completionTime);
        leaderboard.setDiffLevel(diffLevel);
        leaderboard.setUserId(userId);
        return lyricsChainLeaderboardRepository.save(leaderboard);
    }

    public List<LyricsChainLeaderboard> getTop10Scores() {
        return lyricsChainLeaderboardRepository.findTop10ByOrderByScoreDescCreateTimeAsc();
    }
} 