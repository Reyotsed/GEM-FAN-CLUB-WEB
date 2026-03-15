package com.example.gem_fan_club_web.controller;

import com.example.gem_fan_club_web.dto.ChatMessage;
import com.example.gem_fan_club_web.dto.ChatRequest;
import com.example.gem_fan_club_web.dto.ResponseDTO;
import com.example.gem_fan_club_web.service.AIService;
import com.example.gem_fan_club_web.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
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
     * SSE 流式端点：实时逐 token 返回 AI 回答
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(
            @RequestBody ChatRequest request,
            HttpServletRequest httpRequest)
    {
        // SseEmitter timeout: 120s (matches RestTemplate / WebClient timeout)
        SseEmitter emitter = new SseEmitter(120_000L);

        String clientIp = getClientIp(httpRequest);

        // Rate limit check
        if (!rateLimitService.allowAiRequest(clientIp)) {
            log.warn("[Stream] AI rate limit triggered, IP: {}", clientIp);
            sendErrorAndComplete(emitter, "请求太频繁啦，请稍后再试～ 😅");
            return emitter;
        }

        // Input validation
        String question = request.getQuestion();
        if (question == null || question.trim().isEmpty()) {
            sendErrorAndComplete(emitter, "问题不能为空哦～");
            return emitter;
        }
        if (question.length() > 500) {
            sendErrorAndComplete(emitter, "问题太长啦，请精简一下～");
            return emitter;
        }

        List<ChatMessage> history = request.getHistory();
        if (history != null && history.size() > 20) {
            sendErrorAndComplete(emitter, "对话历史太长了，请清空重新开始吧～");
            return emitter;
        }

        log.info("[Stream] AI request from IP: {}, question length: {}", clientIp, question.length());

        // Delegate to service — streaming happens asynchronously
        aiService.getAnswerStream(question, history, emitter);

        return emitter;
    }

    /**
     * Send an error SSE event and complete the emitter
     */
    private void sendErrorAndComplete(SseEmitter emitter, String message) {
        try {
            // Escape special characters for safe JSON embedding
            String safeMessage = message
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"");
            SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .name("error")
                    .data("{\"message\":\"" + safeMessage + "\"}");
            emitter.send(event);
            emitter.complete();
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
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
