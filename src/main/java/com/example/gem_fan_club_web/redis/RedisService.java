package com.example.gem_fan_club_web.redis;

import com.example.gem_fan_club_web.constants.Constants;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class RedisService {
    @Resource
    private RedisUtils redisUtil;

    public String setCheckCodeKey(String code){
        String checkCodeKey = UUID.randomUUID().toString();
        redisUtil.set(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey, code,10, TimeUnit.MINUTES);
        return checkCodeKey;
    }

    public void cleanCheckCode(String checkCodeKey){
        redisUtil.delete(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
    }

    public String getCheckCode(String checkCodeKey) {
        return redisUtil.get(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
    }

    public void setToken(String token, String email) {
        redisUtil.set(Constants.REDIS_KEY_TOKEN_WEB + token, email, 7, TimeUnit.DAYS);
    }

    public String getEmailByToken(String token) {
        return redisUtil.get(Constants.REDIS_KEY_TOKEN_WEB + token);
    }

    public void cleanToken(String token) {
        redisUtil.delete(Constants.REDIS_KEY_TOKEN_WEB + token);
    }
}
