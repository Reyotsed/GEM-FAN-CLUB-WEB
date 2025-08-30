package com.example.gem_fan_club_web.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatRequest {
    String question;
    List<ChatMessage> history;
}
