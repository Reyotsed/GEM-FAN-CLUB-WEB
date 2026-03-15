package com.example.gem_fan_club_web.service;

import com.example.gem_fan_club_web.dto.AiChatRequest;
import com.example.gem_fan_club_web.dto.AiResponse;
import com.example.gem_fan_club_web.dto.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class AIService {

    private final RestTemplate restTemplate;
    private final WebClient aiWebClient;

    @Value("${ai.service.url}")
    private String aiServiceUrl;

    public AIService(RestTemplate restTemplate, WebClient aiWebClient) {
        this.restTemplate = restTemplate;
        this.aiWebClient = aiWebClient;
    }

    /**
     * 调用Python AI服务获取回答（同步，非流式）
     */
    public String getAnswer(String question, List<ChatMessage> history) {
        AiChatRequest requestPayload = new AiChatRequest();
        requestPayload.setQuestion(question);
        requestPayload.setHistory(history);

        log.info("正在向AI服务 [{}] 发送请求...", aiServiceUrl);

        try {
            AiResponse response = restTemplate.postForObject(aiServiceUrl, requestPayload, AiResponse.class);
            if (response != null && response.getAnswer() != null) {
                log.info("成功接收到AI服务的响应。{}", response.getAnswer());
                return response.getAnswer();
            } else {
                log.warn("AI服务返回了空的响应。");
                return "抱歉，AI好像没有给我回应耶...";
            }
        } catch (RestClientException e) {
            log.error("调用AI服务失败: {}", e.getMessage());
            return "哎呀，我和我的AI小伙伴暂时失联了，请稍后再试试吧！";
        }
    }

    /**
     * 流式调用Python AI服务，通过 SseEmitter 将 SSE 事件实时转发给前端
     */
    public void getAnswerStream(String question, List<ChatMessage> history, SseEmitter emitter) {
        AiChatRequest requestPayload = new AiChatRequest();
        requestPayload.setQuestion(question);
        requestPayload.setHistory(history);

        log.info("[Stream] 正在向AI服务发送流式请求...");

        // Subscribe to the upstream SSE stream from RAG service
        Disposable subscription = aiWebClient.post()
                .uri("/chat_gem/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(requestPayload)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {})
                .doOnNext(sse -> {
                    try {
                        String eventType = sse.event() != null ? sse.event() : "token";
                        String data = sse.data() != null ? sse.data() : "{}";

                        // Forward the SSE event to the frontend via SseEmitter
                        // IMPORTANT: use MediaType.TEXT_PLAIN to prevent Jackson from
                        // double-encoding the JSON string (the data is already JSON text
                        // from the upstream RAG service).
                        SseEmitter.SseEventBuilder event = SseEmitter.event()
                                .name(eventType)
                                .data(data, MediaType.TEXT_PLAIN);
                        emitter.send(event);

                        // If this is a "done" event, complete the emitter
                        if ("done".equals(eventType)) {
                            emitter.complete();
                        }
                    } catch (IOException e) {
                        log.warn("[Stream] 发送SSE事件失败（客户端可能已断开）: {}", e.getMessage());
                        emitter.completeWithError(e);
                    }
                })
                .doOnError(error -> {
                    log.error("[Stream] 上游SSE流出错: {}", error.getMessage());
                    try {
                        SseEmitter.SseEventBuilder errorEvent = SseEmitter.event()
                                .name("error")
                                .data("{\"message\":\"AI服务连接失败，请稍后再试\"}");
                        emitter.send(errorEvent);
                    } catch (IOException ignored) {
                    }
                    emitter.completeWithError(error);
                })
                .doOnComplete(() -> {
                    log.info("[Stream] 上游SSE流结束");
                    // emitter may already be completed by "done" event
                })
                .subscribe();

        // If the client disconnects early, cancel the upstream subscription
        emitter.onCompletion(subscription::dispose);
        emitter.onTimeout(() -> {
            log.warn("[Stream] SseEmitter 超时");
            subscription.dispose();
            emitter.complete();
        });
        emitter.onError(t -> {
            log.warn("[Stream] SseEmitter 出错: {}", t.getMessage());
            subscription.dispose();
        });
    }
}