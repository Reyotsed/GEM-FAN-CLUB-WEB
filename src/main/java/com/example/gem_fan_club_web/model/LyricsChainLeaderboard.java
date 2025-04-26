package com.example.gem_fan_club_web.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "lyrics_chain_leaderboard")
public class LyricsChainLeaderboard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nickname;
    
    private Integer score;
    
    private Integer completionTime;
    
    private LocalDateTime createTime;
    
    private Integer diffLevel;
    
    private String userId;
} 