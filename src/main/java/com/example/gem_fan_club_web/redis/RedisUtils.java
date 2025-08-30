package com.example.gem_fan_club_web.redis;

import com.example.gem_fan_club_web.model.quote.QuotePicture;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisUtils {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
    private final ObjectMapper objectMapper;

    public RedisUtils() {
        this.objectMapper = new ObjectMapper();
        // 配置ObjectMapper
        this.objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 忽略未知字段，提高兼容性
        this.objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // 注册Java 8时间模块
        this.objectMapper.registerModule(new JavaTimeModule());
    }

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

    // 设置QuotePicture对象缓存（逻辑过期）
    public void setQuotePictureWithLogicExpire(String key, QuotePicture quotePicture, long expireSeconds) {
        try {
            // 创建包装对象，包含数据和过期时间
            QuotePictureWrapper wrapper = new QuotePictureWrapper();
            wrapper.setData(quotePicture);
            wrapper.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
            
            String jsonValue = objectMapper.writeValueAsString(wrapper);
            log.debug("序列化QuotePicture成功，key: {}, data: {}", key, jsonValue);
            
            // 设置缓存，永不过期
            stringRedisTemplate.opsForValue().set(key, jsonValue);
        } catch (JsonProcessingException e) {
            log.error("序列化QuotePicture失败，key: {}, quotePicture: {}", key, quotePicture, e);
            throw new RuntimeException("序列化QuotePicture失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("设置QuotePicture缓存失败，key: {}, quotePicture: {}", key, quotePicture, e);
            throw new RuntimeException("设置QuotePicture缓存失败: " + e.getMessage(), e);
        }
    }

    // 获取QuotePicture对象缓存（逻辑过期）
    public QuotePictureWrapper getQuotePictureWithLogicExpire(String key) {
        try {
            String jsonValue = stringRedisTemplate.opsForValue().get(key);
            if (jsonValue == null) {
                return null;
            }
            QuotePictureWrapper wrapper = objectMapper.readValue(jsonValue, QuotePictureWrapper.class);
            log.debug("反序列化QuotePicture成功，key: {}", key);
            return wrapper;
        } catch (JsonProcessingException e) {
            log.error("反序列化QuotePicture失败，key: {}, jsonValue: {}", key, 
                     stringRedisTemplate.opsForValue().get(key), e);
            throw new RuntimeException("反序列化QuotePicture失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("获取QuotePicture缓存失败，key: {}", key, e);
            throw new RuntimeException("获取QuotePicture缓存失败: " + e.getMessage(), e);
        }
    }

    // 设置QuotePicture列表缓存（逻辑过期）
    public void setQuotePictureListWithLogicExpire(String key, List<QuotePicture> quotePictures, long expireSeconds) {
        try {
            // 创建包装对象，包含数据和过期时间
            QuotePictureListWrapper wrapper = new QuotePictureListWrapper();
            wrapper.setData(quotePictures);
            wrapper.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
            
            String jsonValue = objectMapper.writeValueAsString(wrapper);
            log.debug("序列化QuotePicture列表成功，key: {}, count: {}, data: {}", 
                     key, quotePictures.size(), jsonValue);
            
            // 设置缓存，永不过期
            stringRedisTemplate.opsForValue().set(key, jsonValue);
        } catch (JsonProcessingException e) {
            log.error("序列化QuotePicture列表失败，key: {}, count: {}, error: {}", 
                     key, quotePictures != null ? quotePictures.size() : 0, e.getMessage(), e);
            throw new RuntimeException("序列化QuotePicture列表失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("设置QuotePicture列表缓存失败，key: {}, count: {}", 
                     key, quotePictures != null ? quotePictures.size() : 0, e);
            throw new RuntimeException("设置QuotePicture列表缓存失败: " + e.getMessage(), e);
        }
    }

    // 获取QuotePicture列表缓存（逻辑过期）
    public QuotePictureListWrapper getQuotePictureListWithLogicExpire(String key) {
        try {
            String jsonValue = stringRedisTemplate.opsForValue().get(key);
            if (jsonValue == null) {
                return null;
            }
            QuotePictureListWrapper wrapper = objectMapper.readValue(jsonValue, QuotePictureListWrapper.class);
            log.debug("反序列化QuotePicture列表成功，key: {}", key);
            return wrapper;
        } catch (JsonProcessingException e) {
            log.error("反序列化QuotePicture列表失败，key: {}, jsonValue: {}", key, 
                     stringRedisTemplate.opsForValue().get(key), e);
            throw new RuntimeException("反序列化QuotePicture列表失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("获取QuotePicture列表缓存失败，key: {}", key, e);
            throw new RuntimeException("获取QuotePicture列表缓存失败: " + e.getMessage(), e);
        }
    }

    // 设置QuotePicture对象缓存（传统过期方式，保留兼容性）
    public void setQuotePicture(String key, QuotePicture quotePicture, long time, TimeUnit unit) {
        try {
            String jsonValue = objectMapper.writeValueAsString(quotePicture);
            log.debug("序列化QuotePicture成功（传统方式），key: {}", key);
            stringRedisTemplate.opsForValue().set(key, jsonValue, time, unit);
        } catch (JsonProcessingException e) {
            log.error("序列化QuotePicture失败（传统方式），key: {}, quotePicture: {}", key, quotePicture, e);
            throw new RuntimeException("序列化QuotePicture失败: " + e.getMessage(), e);
        }
    }

    // 获取QuotePicture对象缓存
    public QuotePicture getQuotePicture(String key) {
        try {
            String jsonValue = stringRedisTemplate.opsForValue().get(key);
            if (jsonValue == null) {
                return null;
            }
            QuotePicture quotePicture = objectMapper.readValue(jsonValue, QuotePicture.class);
            log.debug("反序列化QuotePicture成功（传统方式），key: {}", key);
            return quotePicture;
        } catch (JsonProcessingException e) {
            log.error("反序列化QuotePicture失败（传统方式），key: {}, jsonValue: {}", key, 
                     stringRedisTemplate.opsForValue().get(key), e);
            throw new RuntimeException("反序列化QuotePicture失败: " + e.getMessage(), e);
        }
    }

    // 设置QuotePicture列表缓存
    public void setQuotePictureList(String key, List<QuotePicture> quotePictures, long time, TimeUnit unit) {
        try {
            String jsonValue = objectMapper.writeValueAsString(quotePictures);
            log.debug("序列化QuotePicture列表成功（传统方式），key: {}, count: {}", key, quotePictures.size());
            stringRedisTemplate.opsForValue().set(key, jsonValue, time, unit);
        } catch (JsonProcessingException e) {
            log.error("序列化QuotePicture列表失败（传统方式），key: {}, count: {}, error: {}", 
                     key, quotePictures != null ? quotePictures.size() : 0, e.getMessage(), e);
            throw new RuntimeException("序列化QuotePicture列表失败: " + e.getMessage(), e);
        }
    }

    // 获取QuotePicture列表缓存
    public List<QuotePicture> getQuotePictureList(String key) {
        try {
            String jsonValue = stringRedisTemplate.opsForValue().get(key);
            if (jsonValue == null) {
                return null;
            }
            List<QuotePicture> quotePictures = objectMapper.readValue(jsonValue, new TypeReference<List<QuotePicture>>() {});
            log.debug("反序列化QuotePicture列表成功（传统方式），key: {}", key);
            return quotePictures;
        } catch (JsonProcessingException e) {
            log.error("反序列化QuotePicture列表失败（传统方式），key: {}, jsonValue: {}", key, 
                     stringRedisTemplate.opsForValue().get(key), e);
            throw new RuntimeException("反序列化QuotePicture列表失败: " + e.getMessage(), e);
        }
    }

    // 包装类，用于逻辑过期
    public static class QuotePictureWrapper {
        private QuotePicture data;
        private LocalDateTime expireTime;

        // 默认构造函数
        public QuotePictureWrapper() {}

        public QuotePictureWrapper(QuotePicture data, LocalDateTime expireTime) {
            this.data = data;
            this.expireTime = expireTime;
        }

        public QuotePicture getData() {
            return data;
        }

        public void setData(QuotePicture data) {
            this.data = data;
        }

        public LocalDateTime getExpireTime() {
            return expireTime;
        }

        public void setExpireTime(LocalDateTime expireTime) {
            this.expireTime = expireTime;
        }

        @JsonIgnore
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expireTime);
        }
    }

    // 包装类，用于逻辑过期
    public static class QuotePictureListWrapper {
        private List<QuotePicture> data;
        private LocalDateTime expireTime;

        // 默认构造函数
        public QuotePictureListWrapper() {}

        public QuotePictureListWrapper(List<QuotePicture> data, LocalDateTime expireTime) {
            this.data = data;
            this.expireTime = expireTime;
        }

        public List<QuotePicture> getData() {
            return data;
        }

        public void setData(List<QuotePicture> data) {
            this.data = data;
        }

        public LocalDateTime getExpireTime() {
            return expireTime;
        }

        public void setExpireTime(LocalDateTime expireTime) {
            this.expireTime = expireTime;
        }

        @JsonIgnore
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expireTime);
        }
    }
}