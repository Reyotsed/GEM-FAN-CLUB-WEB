package com.example.gem_fan_club_web.repository;

import com.example.gem_fan_club_web.model.Concert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertRepository extends JpaRepository<Concert, Integer> {
} 