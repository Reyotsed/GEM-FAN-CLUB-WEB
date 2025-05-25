package com.example.gem_fan_club_web.controller;

import com.example.gem_fan_club_web.model.GameSong;
import com.example.gem_fan_club_web.model.ResponseDTO;
import com.example.gem_fan_club_web.service.GameSongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/game/song")
public class GameSongController {
    @Autowired
    private GameSongService gameSongService;

    @GetMapping("/random")
    public ResponseDTO getRandomSongs(@RequestParam("maxLevel") int maxLevel) {
        List<GameSong> songs = gameSongService.getRandomSongs(maxLevel, 10);
        return new ResponseDTO(200, "success", songs);
    }
} 