package com.example.gem_fan_club_web.dto;

import java.util.List;

public class AiChatRequest {
    private String question;
    private List<ChatMessage> history; // 使用强类型的列表

    // Getters and Setters
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public List<ChatMessage> getHistory() { return history; }
    public void setHistory(List<ChatMessage> history) { this.history = history; }
}