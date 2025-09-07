package com.example.gem_fan_club_web.service;

import com.example.gem_fan_club_web.model.Order;
import com.example.gem_fan_club_web.repository.OrderRepository;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 基于Redis Stream的订单异步处理服务 (重构版)
 * 采用单一阻塞循环模式，具备高效率和强大的错误恢复能力。
 */
@Slf4j
@Service
public class OrderStreamService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    @Qualifier("asyncExecutor")
    private Executor asyncExecutor;

    private static final String ORDER_STREAM_KEY = "gem_fan_club:order:stream";
    private static final String GROUP = "order_group";
    // 消费者名称最好包含主机名/IP和端口，以保证在分布式环境中的唯一性
    private static final String CONSUMER = "order_consumer_" + System.getProperty("server.port", "7071");

    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread consumerThread; // 持有消费者线程的引用，以便在关闭时中断它

    /**
     * 发送订单消息到Stream。
     * 生产者逻辑通常是正确的，保持不变。
     */
    public void sendOrderMessage(String orderId, String userId, String ticketId) {
        try {
            Map<String, String> messageMap = new HashMap<>();
            messageMap.put("orderId", orderId);
            messageMap.put("userId", userId);
            messageMap.put("ticketId", ticketId);
            messageMap.put("createTime", LocalDateTime.now().toString());
            // 订单状态等信息也应放入消息体
            // messageMap.put("status", "0");

            var messageId = stringRedisTemplate.opsForStream().add(ORDER_STREAM_KEY, messageMap);
            log.info("订单消息发送成功, Stream: '{}', MessageId: {}, OrderId: {}", ORDER_STREAM_KEY, messageId, orderId);

        } catch (Exception e) {
            log.error("订单消息发送到Stream失败, OrderId: {}", orderId, e);
        }
    }

    /**
     * 应用启动后，启动消费者。
     * 这是重构的核心，采用单一的、阻塞的循环。
     */
    @EventListener(ApplicationReadyEvent.class)
    public void startConsumer() {
        if (running.compareAndSet(false, true)) {
            // 使用 Executor 来管理线程生命周期
            asyncExecutor.execute(() -> {
                consumerThread = Thread.currentThread(); // 获取当前线程引用
                log.info("启动 Redis Stream 消费者: {}", CONSUMER);

                // 启动时，先尝试创建一次组，为冷启动做准备
                tryCreateGroup();

                while (running.get() && !consumerThread.isInterrupted()) {
                    try {
                        Consumer consumer = Consumer.from(GROUP, CONSUMER);
                        StreamOffset<String> streamOffset = StreamOffset.create(ORDER_STREAM_KEY, ReadOffset.from(">"));
                        StreamReadOptions options = StreamReadOptions.empty()
                                .count(10) // 每次最多拉取10条，提高吞吐量
                                .block(Duration.ofSeconds(2)); // 关键：长阻塞等待，极大降低CPU和网络开销

                        List<MapRecord<String, Object, Object>> records = stringRedisTemplate.opsForStream()
                                .read(consumer, options, streamOffset);

                        if (records == null || records.isEmpty()) {
                            // 阻塞超时，没有新消息，继续下一次循环
                            continue;
                        }

                        // 处理读取到的消息
                        for (MapRecord<String, Object, Object> record : records) {
                            processOrderMessage(record);
                        }

                    } catch (RedisSystemException e) {
                        // 这是处理 NOGROUP 错误的关键！
                        if (e.getCause() != null && e.getCause().getMessage().contains("NOGROUP")) {
                            log.warn("消费者组 '{}' 不存在，可能已被删除。正在尝试重建...", GROUP);
                            tryCreateGroup();
                        } else {
                            log.error("消费时发生Redis系统异常", e);
                            sleepSilently(5000); // 发生其他错误时，短暂等待避免CPU空转
                        }
                    } catch (Exception e) {
                        // 捕获所有其他异常，防止线程意外终止
                        log.error("消费时发生未知异常", e);
                        sleepSilently(5000);
                    }
                }
                log.info("Redis Stream 消费者已停止: {}", CONSUMER);
            });
        }
    }

    /**
     * 尝试创建消费者组。
     * 这个方法现在更加健壮，能处理组已存在和Stream不存在的情况。
     */
    private void tryCreateGroup() {
        try {
            // 使用 "0-0" 作为起始ID是创建组最可靠的方式
            stringRedisTemplate.opsForStream().createGroup(ORDER_STREAM_KEY, ReadOffset.from("0-0"), GROUP);
            log.info("消费者组 '{}' 在 Stream '{}' 上已创建或确认存在", GROUP, ORDER_STREAM_KEY);
        } catch (RedisSystemException e) {
            // 这是预期的异常，因为组可能已经存在
            if (e.getCause() != null && e.getCause().getMessage().contains("BUSYGROUP")) {
                log.info("消费者组 '{}' 已存在，无需创建", GROUP);
            }
            // 另一个预期异常是Stream还不存在，等待生产者创建它
            else if (e.getCause() != null && e.getCause().getMessage().contains("no such key")) {
                 log.warn("Stream '{}' 尚不存在，将等待生产者自动创建...", ORDER_STREAM_KEY);
                 sleepSilently(1000);
            } else {
                log.error("创建消费者组时发生未知错误", e);
            }
        }
    }

    /**
     * 处理单个订单消息的业务逻辑
     */
    private void processOrderMessage(MapRecord<String, Object, Object> record) {
        try {
            Map<Object, Object> value = record.getValue();
            String orderId = (String) value.get("orderId");

            if (orderRepository.existsById(orderId)) {
                log.warn("订单已存在，跳过重复处理并直接ACK。OrderId: {}", orderId);
                acknowledgeMessage(record.getId());
                return;
            }
            
            Order order = Order.builder()
                    .id(orderId)
                    .userId((String) value.get("userId"))
                    .ticketId((String) value.get("ticketId"))
                    .status(0) // 假设0是初始状态
                    .createTime(parseDateTimeOrNow((String) value.get("createTime")))
                    .build();

            orderRepository.save(order);
            acknowledgeMessage(record.getId());
            log.info("订单持久化成功并已ACK。OrderId: {}", orderId);
            
        } catch (Exception e) {
            // 重要的：如果处理失败，不要ACK，让消息保留在队列中以便后续处理（例如通过XPENDING和XCLAIM）
            log.error("处理订单消息失败，消息将保留待处理。RecordId: {}, OrderId: {}", record.getId(), record.getValue().get("orderId"), e);
        }
    }
    
    private void acknowledgeMessage(RecordId recordId) {
        stringRedisTemplate.opsForStream().acknowledge(ORDER_STREAM_KEY, GROUP, recordId);
    }

    private LocalDateTime parseDateTimeOrNow(String s) {
        try {
            if (s == null) return LocalDateTime.now();
            return LocalDateTime.parse(s);
        } catch (DateTimeParseException e) {
            log.warn("无法解析日期时间字符串 '{}'，使用当前时间代替", s);
            return LocalDateTime.now();
        }
    }
    
    private void sleepSilently(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // 在休眠时被中断，需要重置中断状态，以便外层循环能正确退出
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 应用关闭时，优雅地停止消费者线程。
     */
    @PreDestroy
    public void shutdown() {
        log.info("准备关闭 Redis Stream 消费者...");
        running.set(false);
        if (consumerThread != null && consumerThread.isAlive()) {
            // 中断线程，这将导致阻塞的 read() 操作立即抛出异常，从而使循环能够退出
            consumerThread.interrupt();
        }
    }
}