package com.example.gem_fan_club_web.repository;

import com.example.gem_fan_club_web.model.Story;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoryRepository extends JpaRepository<Story, Long> {

    boolean existsByIgMediaId(String igMediaId);

    Page<Story> findAllByOrderByTakenAtDesc(Pageable pageable);
}
