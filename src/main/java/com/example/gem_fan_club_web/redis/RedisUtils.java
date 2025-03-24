package com.example.gem_fan_club_web.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtils {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // 设置值
    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    // 设置值并设置过期时间
    public void set(String key, String value, long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, time, unit);
    }

    // 获取值
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    // 删除键
    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    // 检查键是否存在
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    // 设置哈希值
    public void setHash(String key, String hashKey, String value) {
        stringRedisTemplate.opsForHash().put(key, hashKey, value);
    }

    // 获取哈希值
    public String getHash(String key, String hashKey) {
        return (String) stringRedisTemplate.opsForHash().get(key, hashKey);
    }
}