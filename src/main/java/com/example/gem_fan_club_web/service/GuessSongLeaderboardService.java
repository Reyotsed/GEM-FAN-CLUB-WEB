package com.example.gem_fan_club_web.service;

import com.example.gem_fan_club_web.model.GuessSongLeaderboard;
import com.example.gem_fan_club_web.repository.GuessSongLeaderboardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GuessSongLeaderboardService {

    private final GuessSongLeaderboardRepository guessSongLeaderboardRepository;

    @Autowired
    public GuessSongLeaderboardService(GuessSongLeaderboardRepository guessSongLeaderboardRepository) {
        this.guessSongLeaderboardRepository = guessSongLeaderboardRepository;
    }

    public GuessSongLeaderboard saveScore(String nickname, Integer score, Integer completionTime, Integer diffLevel) {
        GuessSongLeaderboard leaderboard = new GuessSongLeaderboard();
        leaderboard.setNickname(nickname);
        leaderboard.setScore(score);
        leaderboard.setCreateTime(LocalDateTime.now());
        leaderboard.setCompletionTime(completionTime);
        leaderboard.setDiffLevel(diffLevel);
        return guessSongLeaderboardRepository.save(leaderboard);
    }

    public GuessSongLeaderboard saveScore(String nickname, Integer score, Integer completionTime, Integer diffLevel, String userId) {
        GuessSongLeaderboard leaderboard = new GuessSongLeaderboard();
        leaderboard.setNickname(nickname);
        leaderboard.setScore(score);
        leaderboard.setCreateTime(LocalDateTime.now());
        leaderboard.setCompletionTime(completionTime);
        leaderboard.setDiffLevel(diffLevel);
        leaderboard.setUserId(userId);
        return guessSongLeaderboardRepository.save(leaderboard);
    }

    public List<GuessSongLeaderboard> getTop10Scores() {
        return guessSongLeaderboardRepository.findTop10ByOrderByScoreDescCreateTimeAsc();
    }
}
