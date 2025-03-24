package com.example.gem_fan_club_web.repository;

import com.example.gem_fan_club_web.model.Song;
import org.springframework.data.jpa.repository.JpaRepository;


public interface SongRepository extends JpaRepository<Song, Long> {

}