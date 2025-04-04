package com.example.gem_fan_club_web.controller;

import com.example.gem_fan_club_web.model.Concert;
import com.example.gem_fan_club_web.model.ResponseDTO;
import com.example.gem_fan_club_web.service.ConcertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/concert")
public class ConcertController {

    @Autowired
    private ConcertService concertService;

    @GetMapping("/list")
    public ResponseDTO getConcertList() {
        List<Concert> concerts = concertService.getAllConcerts();
        return new ResponseDTO(200, "success", concerts);
    }
} 