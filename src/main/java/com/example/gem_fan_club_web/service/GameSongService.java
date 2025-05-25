package com.example.gem_fan_club_web.service;

import com.example.gem_fan_club_web.model.GameSong;
import com.example.gem_fan_club_web.repository.GameSongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class GameSongService {
    @Autowired
    private GameSongRepository gameSongRepository;

    public List<GameSong> getRandomSongs(int maxLevel, int count) {
        return gameSongRepository.findRandomByLevel(maxLevel, count);
    }
} 