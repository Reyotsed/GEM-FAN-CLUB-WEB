package com.example.gem_fan_club_web.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 基于Redis Stream的订单异步处理服务
 */
@Slf4j
@Service
public class OrderStreamService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String ORDER_STREAM_KEY = "gem_fan_club:order:stream";

    /**
     * 发送订单消息到Stream
     */
    public void sendOrderMessage(String orderId, String userId, String ticketId) {
        try {
            Map<String, String> messageMap = new HashMap<>();
            messageMap.put("orderId", orderId);
            messageMap.put("userId", userId);
            messageMap.put("ticketId", ticketId);
            messageMap.put("createTime", LocalDateTime.now().toString());
            messageMap.put("status", "0");

            // 发送消息到Stream
            var messageId = stringRedisTemplate.opsForStream()
                .add(ORDER_STREAM_KEY, messageMap);

            log.info("订单消息发送到Stream成功，messageId: {}, orderId: {}, userId: {}, ticketId: {}", 
                    messageId, orderId, userId, ticketId);

        } catch (Exception e) {
            log.error("订单消息发送到Stream失败，orderId: {}, userId: {}, ticketId: {}", 
                    orderId, userId, ticketId, e);
        }
    }

    /**
     * 定时消费订单消息（简化版本）
     */
    @Scheduled(fixedRate = 1000) // 每1秒消费一次
    public void consumeOrderMessages() {
        try {
            // 简化版本：直接处理，不使用消费者组
//            log.info("开始消费订单消息...");
            
            // TODO: 这里可以实现更复杂的消息消费逻辑
            // 目前先记录日志，后续可以完善
            
        } catch (Exception e) {
            log.error("消费订单消息异常", e);
        }
    }
}
