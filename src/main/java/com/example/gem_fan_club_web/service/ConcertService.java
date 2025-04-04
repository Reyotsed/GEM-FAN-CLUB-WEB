package com.example.gem_fan_club_web.service;

import com.example.gem_fan_club_web.model.Concert;
import com.example.gem_fan_club_web.repository.ConcertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConcertService {

    @Autowired
    private ConcertRepository concertRepository;

    /**
     * 获取所有演唱会信息
     */
    public List<Concert> getAllConcerts() {
        return concertRepository.findAll();
    }
} 