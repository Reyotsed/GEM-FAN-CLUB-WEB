package com.example.gem_fan_club_web.controller;

import com.example.gem_fan_club_web.model.GuessSongLeaderboard;
import com.example.gem_fan_club_web.dto.ResponseDTO;
import com.example.gem_fan_club_web.service.GuessSongLeaderboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/game/guess-song")
public class GuessSongLeaderboardController {

    @Autowired
    private GuessSongLeaderboardService leaderboardService;

    @PostMapping("/submit-score")
    public ResponseDTO submitScore(
            @RequestParam("nickname") String nickname,
            @RequestParam("score") Integer score,
            @RequestParam("completionTime") Integer completionTime,
            @RequestParam("diffLevel") Integer diffLevel,
            @RequestParam("userId") String userId
    ) {
        GuessSongLeaderboard savedScore = leaderboardService.saveScore(nickname, score, completionTime, diffLevel, userId);
        return new ResponseDTO(200, "success", savedScore);
    }

    @GetMapping("/getLeaderboard")
    public ResponseDTO getLeaderboard() {
        return new ResponseDTO(200, "success", leaderboardService.getTop10Scores());
    }
}
