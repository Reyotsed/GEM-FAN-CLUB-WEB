package com.example.gem_fan_club_web.repository;

import com.example.gem_fan_club_web.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, String> {
}


