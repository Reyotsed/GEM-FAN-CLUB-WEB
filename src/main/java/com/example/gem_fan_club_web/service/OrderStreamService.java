package com.example.gem_fan_club_web.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.springframework.data.domain.Range;

import com.example.gem_fan_club_web.model.Order;
import com.example.gem_fan_club_web.repository.OrderRepository;

/**
 * 基于Redis Stream的订单异步处理服务
 */
@Slf4j
@Service
public class OrderStreamService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private OrderRepository orderRepository;

    private static final String ORDER_STREAM_KEY = "gem_fan_club:order:stream";
    private static final String GROUP = "order_group";
    private static final String CONSUMER = "order_consumer_" + System.getProperty("server.port", "7071");

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
            var messageId = stringRedisTemplate.opsForStream().add(ORDER_STREAM_KEY, messageMap);

            log.info("订单消息发送到Stream成功，messageId: {}, orderId: {}, userId: {}, ticketId: {}", 
                    messageId, orderId, userId, ticketId);

        } catch (Exception e) {
            log.error("订单消息发送到Stream失败，orderId: {}, userId: {}, ticketId: {}", 
                    orderId, userId, ticketId, e);
        }
    }

    private boolean groupCreated = false;
    
    /**
     * 确保消费者组存在（只创建一次）
     */
    private void ensureGroupExists() {
        if (groupCreated) {
            return;
        }
        
        try {
            // 检查Stream是否存在
            if (!stringRedisTemplate.hasKey(ORDER_STREAM_KEY)) {
                log.info("Stream {} 不存在，等待消息发送", ORDER_STREAM_KEY);
                return;
            }
            
            // 先尝试删除现有组（如果存在）
            try {
                stringRedisTemplate.opsForStream().destroyGroup(ORDER_STREAM_KEY, GROUP);
                log.info("已删除现有消费者组 {}", GROUP);
            } catch (Exception ignored) {
                // 组不存在，忽略
            }
            
            // 创建新组，从Stream开头读取所有消息
            stringRedisTemplate.opsForStream().createGroup(ORDER_STREAM_KEY, ReadOffset.from("0"), GROUP);
            groupCreated = true;
            log.info("消费者组 {} 已创建，将从Stream开头读取所有消息", GROUP);
        } catch (Exception e) {
            log.error("创建消费者组失败", e);
        }
    }

    /**
     * 定时消费订单消息：优先读取pending，再读新消息
     */
    @Scheduled(fixedRate = 1000)
    public void consumeOrderMessages() {
        try {
            // 检查Stream是否存在
            if (!stringRedisTemplate.hasKey(ORDER_STREAM_KEY)) {
                return; // Stream不存在，直接返回
            }
            
            ensureGroupExists();
            
            if (!groupCreated) {
                return; // 消费者组未创建，等待下次调度
            }
            
            // 1) 先处理Pending消息（超时未确认的消息）
            processPendingMessages();
            
            // 2) 再处理新消息
            processNewMessages();
            
        } catch (org.springframework.dao.QueryTimeoutException e) {
            log.warn("Redis 查询超时，将在下次调度时重试: {}", e.getMessage());
        } catch (Exception e) {
            log.error("消费订单消息异常", e);
        }
    }

    /**
     * 处理Pending消息（超时未确认的消息）
     */
    private void processPendingMessages() {
        try {
            // 获取pending消息列表
            PendingMessages pendingMessages = stringRedisTemplate.opsForStream()
                    .pending(ORDER_STREAM_KEY, Consumer.from(GROUP, CONSUMER), 
                            Range.closed("0", "+"), 100); // 增加批次大小
            
            if (pendingMessages.isEmpty()) {
                return;
            }
            
            log.info("发现 {} 条Pending消息", pendingMessages.size());
            
            // 处理所有pending消息（不限制超时时间）
            for (PendingMessage pendingMessage : pendingMessages) {
                try {
                    // 声明消息所有权，重新处理
                    List<MapRecord<String, Object, Object>> claimedMessages = 
                            stringRedisTemplate.opsForStream().claim(ORDER_STREAM_KEY, 
                                    GROUP, CONSUMER, 
                                    Duration.ofSeconds(0), // 立即声明
                                    RecordId.of(pendingMessage.getIdAsString()));
                    
                    for (MapRecord<String, Object, Object> record : claimedMessages) {
                        processOrderMessage(record);
                    }
                } catch (Exception e) {
                    log.error("处理Pending消息失败: {}", pendingMessage.getIdAsString(), e);
                }
            }
        } catch (Exception e) {
            log.error("处理Pending消息异常", e);
        }
    }

    /**
     * 处理新消息
     */
    private void processNewMessages() {
        try {
            Consumer consumer = Consumer.from(GROUP, CONSUMER);
            StreamOffset<String> streamOffset = StreamOffset.create(ORDER_STREAM_KEY, ReadOffset.from(">"));
            StreamReadOptions options = StreamReadOptions.empty().count(100); // 增加批次大小

            List<MapRecord<String, Object, Object>> records = stringRedisTemplate.opsForStream()
                    .read(consumer, options, streamOffset);

            if (records == null || records.isEmpty()) {
                return;
            }

            log.info("读取到 {} 条新消息", records.size());

            for (MapRecord<String, Object, Object> record : records) {
                processOrderMessage(record);
            }
        } catch (org.springframework.dao.QueryTimeoutException e) {
            log.warn("Redis Stream 读取超时: {}", e.getMessage());
            throw e; // 重新抛出，让上层处理
        } catch (Exception e) {
            log.error("读取 Stream 消息失败", e);
        }
    }
    
    /**
     * 处理单个订单消息
     */
    private void processOrderMessage(MapRecord<String, Object, Object> record) {
        try {
            Map<Object, Object> value = record.getValue();
            String orderId = (String) value.get("orderId");
            String userId = (String) value.get("userId");
            String ticketId = (String) value.get("ticketId");
            String createTimeStr = (String) value.get("createTime");
            String statusStr = (String) value.get("status");

            // 检查订单是否已存在（幂等性检查）
            if (orderRepository.existsById(orderId)) {
                log.info("订单已存在，跳过处理: {}", orderId);
                // 直接ACK，避免重复处理
                stringRedisTemplate.opsForStream().acknowledge(ORDER_STREAM_KEY, GROUP, record.getId());
                return;
            }

            Order order = Order.builder()
                    .id(orderId)
                    .userId(userId)
                    .ticketId(ticketId)
                    .status(parseIntOrDefault(statusStr, 0))
                    .createTime(parseDateTimeOrNow(createTimeStr))
                    .build();

            orderRepository.save(order);

            // ACK消息
            stringRedisTemplate.opsForStream().acknowledge(ORDER_STREAM_KEY, GROUP, record.getId());
            log.info("订单持久化成功并ACK，orderId: {}", orderId);
        } catch (Exception ex) {
            log.error("处理订单消息失败，recordId: {}", record.getId(), ex);
        }
    }

    private int parseIntOrDefault(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return def;
        }
    }

    private LocalDateTime parseDateTimeOrNow(String s) {
        try {
            return LocalDateTime.parse(s);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}
