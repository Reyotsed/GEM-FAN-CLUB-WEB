package com.example.gem_fan_club_web.controller;

import com.example.gem_fan_club_web.dto.ChatMessage;
import com.example.gem_fan_club_web.dto.ChatRequest;
import com.example.gem_fan_club_web.dto.ResponseDTO;
import com.example.gem_fan_club_web.service.AIService;
import com.example.gem_fan_club_web.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/ai")
public class AIController {

    @Autowired
    AIService aiService;

    @Autowired
    RateLimitService rateLimitService;

    @PostMapping("/answer")
    public ResponseDTO answer(
            @RequestBody ChatRequest request,
            HttpServletRequest httpRequest)
    {
        // Extract client IP for rate limiting
        String clientIp = getClientIp(httpRequest);

        // Rate limit: max 5 requests per 60s per IP
        if (!rateLimitService.allowAiRequest(clientIp)) {
            log.warn("AI rate limit triggered, IP: {}", clientIp);
            return new ResponseDTO(429, "请求太频繁啦，请稍后再试～ 😅", null);
        }

        // Input validation
        String question = request.getQuestion();
        if (question == null || question.trim().isEmpty()) {
            return new ResponseDTO(400, "问题不能为空哦～", null);
        }
        if (question.length() > 500) {
            return new ResponseDTO(400, "问题太长啦，请精简一下～", null);
        }

        List<ChatMessage> history = request.getHistory();
        if (history != null && history.size() > 20) {
            return new ResponseDTO(400, "对话历史太长了，请清空重新开始吧～", null);
        }

        log.info("AI request from IP: {}, question length: {}", clientIp, question.length());
        String answer = aiService.getAnswer(question, history);
        return new ResponseDTO(200, "success", answer);
    }

    /**
     * Extract client IP, supporting reverse proxy headers
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // X-Forwarded-For may contain multiple IPs, take the first one
            ip = ip.split(",")[0].trim();
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
