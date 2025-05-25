package com.example.gem_fan_club_web.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "game_songs")
@Data
public class GameSong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "audio_url", nullable = false, length = 255)
    private String audioUrl;

    @Column(name = "wyy_id", length = 100)
    private String wyyId;

    @Column(columnDefinition = "text")
    private String discribe;

    private Integer level;
} 