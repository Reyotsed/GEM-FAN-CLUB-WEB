package com.example.gem_fan_club_web.repository;

import com.example.gem_fan_club_web.model.SiteStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SiteStatsRepository extends JpaRepository<SiteStats, Long> {

    Optional<SiteStats> findByStatDateAndStatKey(LocalDate statDate, String statKey);

    List<SiteStats> findByStatDate(LocalDate statDate);

    List<SiteStats> findByStatDateBetween(LocalDate startDate, LocalDate endDate);

    List<SiteStats> findByStatKey(String statKey);

    List<SiteStats> findByStatKeyAndStatDateBetween(String statKey, LocalDate startDate, LocalDate endDate);
}
