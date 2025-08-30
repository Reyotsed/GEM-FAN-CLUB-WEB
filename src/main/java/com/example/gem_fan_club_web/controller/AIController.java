package com.example.gem_fan_club_web.controller;

import com.example.gem_fan_club_web.dto.ChatMessage;
import com.example.gem_fan_club_web.dto.ChatRequest;
import com.example.gem_fan_club_web.dto.ResponseDTO;
import com.example.gem_fan_club_web.service.AIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/ai")
public class AIController {

    @Autowired
    AIService aiService;

    @PostMapping("/answer")
    public ResponseDTO answer(
            @RequestBody ChatRequest request)
    {
        log.info(request.toString());
        String question = request.getQuestion();
        List<ChatMessage> history = request.getHistory();
        String answer = aiService.getAnswer(question, history);
        return new ResponseDTO(200,"success",answer);
    }
}
