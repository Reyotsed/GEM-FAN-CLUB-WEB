package com.example.gem_fan_club_web.repository;

import com.example.gem_fan_club_web.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    User findByEmail(String email);
}