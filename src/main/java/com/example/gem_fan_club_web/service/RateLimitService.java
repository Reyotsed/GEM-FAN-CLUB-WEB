package com.example.gem_fan_club_web.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 基于Lua脚本的限流服务
 * 使用Redis ZSet + 滑动窗口实现
 */
@Slf4j
@Service
public class RateLimitService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 限流Lua脚本
    private static final DefaultRedisScript<Long> RATE_LIMIT_LUA;
    static {
        RATE_LIMIT_LUA = new DefaultRedisScript<>();
        RATE_LIMIT_LUA.setLocation(new ClassPathResource("rate_limit.lua"));
        RATE_LIMIT_LUA.setResultType(Long.class);
    }

    // 限流配置
    private static final long WINDOW_SIZE = 1000; // 1秒窗口
    private static final int MAX_REQUESTS_PER_WINDOW = 1000; // 每秒最多1000个请求
    private static final int MAX_USER_REQUESTS_PER_WINDOW = 3; // 每个用户每秒最多3个请求

    /**
     * 检查票务级别限流
     * @param ticketId 票务ID
     * @return true-允许请求, false-限流拒绝
     */
    public boolean allowTicketRequest(String ticketId) {
        try {
            String key = "rate_limit:ticket:" + ticketId;
            long currentTime = System.currentTimeMillis();
            
            Long result = stringRedisTemplate.execute(
                RATE_LIMIT_LUA,
                Collections.singletonList(key),
                String.valueOf(currentTime),
                String.valueOf(WINDOW_SIZE),
                String.valueOf(MAX_REQUESTS_PER_WINDOW),
                "" // 票务级别不需要用户ID
            );

            if (result == null || result == 0) {
                log.warn("票务限流触发，ticketId: {}", ticketId);
                return false;
            }

            log.debug("票务限流检查通过，ticketId: {}, 当前请求数: {}", ticketId, result);
            return true;

        } catch (Exception e) {
            log.error("票务限流检查异常，ticketId: {}", ticketId, e);
            // 异常情况下允许请求，避免影响正常业务
            return true;
        }
    }

    /**
     * 检查用户级别限流
     * @param userId 用户ID
     * @param ticketId 票务ID
     * @return true-允许请求, false-限流拒绝
     */
    public boolean allowUserRequest(String userId, String ticketId) {
        try {
            String key = "rate_limit:user:" + userId + ":" + ticketId;
            long currentTime = System.currentTimeMillis();
            
            Long result = stringRedisTemplate.execute(
                RATE_LIMIT_LUA,
                Collections.singletonList(key),
                String.valueOf(currentTime),
                String.valueOf(WINDOW_SIZE),
                String.valueOf(MAX_USER_REQUESTS_PER_WINDOW),
                userId
            );

            if (result == null || result == 0) {
                log.warn("用户限流触发，userId: {}, ticketId: {}", userId, ticketId);
                return false;
            }

            log.debug("用户限流检查通过，userId: {}, ticketId: {}, 当前请求数: {}", userId, ticketId, result);
            return true;

        } catch (Exception e) {
            log.error("用户限流检查异常，userId: {}, ticketId: {}", userId, ticketId, e);
            // 异常情况下允许请求，避免影响正常业务
            return true;
        }
    }

    /**
     * 检查全局限流
     * @return true-允许请求, false-限流拒绝
     */
    public boolean allowGlobalRequest() {
        try {
            String key = "rate_limit:global";
            long currentTime = System.currentTimeMillis();
            
            Long result = stringRedisTemplate.execute(
                RATE_LIMIT_LUA,
                Collections.singletonList(key),
                String.valueOf(currentTime),
                String.valueOf(WINDOW_SIZE),
                String.valueOf(MAX_REQUESTS_PER_WINDOW * 10), // 全局限流阈值
                "" // 全局限流不需要用户ID
            );

            if (result == null || result == 0) {
                log.warn("全局限流触发");
                return false;
            }

            log.debug("全局限流检查通过，当前请求数: {}", result);
            return true;

        } catch (Exception e) {
            log.error("全局限流检查异常", e);
            // 异常情况下允许请求，避免影响正常业务
            return true;
        }
    }

    /**
     * 综合限流检查
     * @param userId 用户ID
     * @param ticketId 票务ID
     * @return true-允许请求, false-限流拒绝
     */
    public boolean allowRequest(String userId, String ticketId) {
        // 1. 检查全局限流
        if (!allowGlobalRequest()) {
            return false;
        }
        
        // 2. 检查票务级别限流
        if (!allowTicketRequest(ticketId)) {
            return false;
        }
        
        // 3. 检查用户级别限流
        if (!allowUserRequest(userId, ticketId)) {
            return false;
        }
        
        return true;
    }

    /**
     * 获取限流统计信息
     */
    public void getRateLimitStats(String ticketId) {
        try {
            String ticketKey = "rate_limit:ticket:" + ticketId;
            String globalKey = "rate_limit:global";
            
            Long ticketCount = stringRedisTemplate.opsForZSet().zCard(ticketKey);
            Long globalCount = stringRedisTemplate.opsForZSet().zCard(globalKey);
            
            log.info("限流统计 - 票务{}: {}个请求, 全局: {}个请求", ticketId, ticketCount, globalCount);
            
        } catch (Exception e) {
            log.error("获取限流统计信息失败", e);
        }
    }
}
