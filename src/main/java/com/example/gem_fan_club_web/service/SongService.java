package com.example.gem_fan_club_web.service;

import com.example.gem_fan_club_web.model.Song;
import com.example.gem_fan_club_web.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SongService {
    @Autowired
    private SongRepository songRepository;

    public List<Song> getAllSongs() {
        return songRepository.findAll();
    }

}
