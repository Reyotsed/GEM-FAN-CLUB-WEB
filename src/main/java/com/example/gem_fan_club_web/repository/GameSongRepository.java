package com.example.gem_fan_club_web.repository;

import com.example.gem_fan_club_web.model.GameSong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface GameSongRepository extends JpaRepository<GameSong, Integer> {
    @Query(value = "SELECT * FROM game_songs WHERE level <= :maxLevel ORDER BY RAND() LIMIT :count", nativeQuery = true)
    List<GameSong> findRandomByLevel(@Param("maxLevel") int maxLevel, @Param("count") int count);
} 