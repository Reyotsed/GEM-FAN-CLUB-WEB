package com.example.gem_fan_club_web.redis;

import com.example.gem_fan_club_web.constants.Constants;
import com.example.gem_fan_club_web.model.quote.QuotePicture;
import com.example.gem_fan_club_web.redis.RedisUtils.QuotePictureListWrapper;
import com.example.gem_fan_club_web.redis.RedisUtils.QuotePictureWrapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
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

    public void setTicketStock(String ticketId, String stock){
        redisUtil.set(Constants.REDIS_TICKET_STOCK + ticketId, stock);
    }

    public String getTicketStock(String ticketId){
        return redisUtil.get(Constants.REDIS_TICKET_STOCK + ticketId);
    }

    public void cleanTicketStock(String ticketId){
        redisUtil.delete(Constants.REDIS_TICKET_STOCK + ticketId);
    }

    public void cleanTicketOrder(String ticketId){
        redisUtil.delete(Constants.REDIS_TICKET_ORDER + ticketId);
    }

    public void cleanTicketOrderStream(){
        redisUtil.delete(Constants.REDIS_TICKET_ORDER_STREAM);
    }

    // QuotePicture缓存相关方法

    /**
     * 设置单个QuotePicture缓存（逻辑过期），过期时间1小时
     */
    public void setQuotePictureCache(Integer pictureId, QuotePicture quotePicture) {
        String key = Constants.REDIS_QUOTE_PICTURE_PREFIX + "single:" + pictureId;
        // 1小时 = 3600秒
        redisUtil.setQuotePictureWithLogicExpire(key, quotePicture, 3600);
    }

    /**
     * 获取单个QuotePicture缓存（逻辑过期）
     */
    public QuotePictureWrapper getQuotePictureCache(Integer pictureId) {
        String key = Constants.REDIS_QUOTE_PICTURE_PREFIX + "single:" + pictureId;
        return redisUtil.getQuotePictureWithLogicExpire(key);
    }

    /**
     * 设置QuotePicture列表缓存（逻辑过期），过期时间1小时
     */
    public void setQuotePictureListCache(Integer quoteId, List<QuotePicture> quotePictures) {
        String key = Constants.REDIS_QUOTE_PICTURE_PREFIX + "list:" + quoteId;
        // 1小时 = 3600秒
        redisUtil.setQuotePictureListWithLogicExpire(key, quotePictures, 3600);
    }

    /**
     * 获取QuotePicture列表缓存（逻辑过期）
     */
    public QuotePictureListWrapper getQuotePictureListCache(Integer quoteId) {
        String key = Constants.REDIS_QUOTE_PICTURE_PREFIX + "list:" + quoteId;
        return redisUtil.getQuotePictureListWithLogicExpire(key);
    }

    /**
     * 删除单个QuotePicture缓存
     */
    public void deleteQuotePictureCache(Integer pictureId) {
        String key = Constants.REDIS_QUOTE_PICTURE_PREFIX + "single:" + pictureId;
        redisUtil.delete(key);
    }

    /**
     * 删除QuotePicture列表缓存
     */
    public void deleteQuotePictureListCache(Integer quoteId) {
        String key = Constants.REDIS_QUOTE_PICTURE_PREFIX + "list:" + quoteId;
        redisUtil.delete(key);
    }

    /**
     * 清除所有QuotePicture相关缓存
     */
    public void clearAllQuotePictureCache() {
        // 这里可以添加批量删除逻辑，或者通过模式匹配删除
        // 由于Redis的限制，这里提供手动删除的方法
    }

    /**
     * 清理可能存在的旧格式缓存
     * 用于解决序列化格式不兼容问题
     */
    public void cleanOldFormatCache(String key) {
        try {
            if (redisUtil.hasKey(key)) {
                String value = redisUtil.get(key);
                if (value != null && value.contains("\"expired\"")) {
                    // 检测到旧格式缓存，清理掉
                    redisUtil.delete(key);
                }
            }
        } catch (Exception e) {
            // 忽略清理过程中的错误，不影响正常业务
        }
    }

    /**
     * 安全获取QuotePicture列表缓存，自动清理旧格式
     */
    public QuotePictureListWrapper getQuotePictureListCacheSafely(Integer quoteId) {
        String key = Constants.REDIS_QUOTE_PICTURE_PREFIX + "list:" + quoteId;
        
        try {
            // 尝试获取缓存
            QuotePictureListWrapper wrapper = redisUtil.getQuotePictureListWithLogicExpire(key);
            if (wrapper != null) {
                return wrapper;
            }
        } catch (Exception e) {
            // 如果反序列化失败，可能是旧格式，清理掉
            redisUtil.delete(key);
        }
        
        return null;
    }

    // 兼容性方法，保留传统过期方式
    /**
     * 设置单个QuotePicture缓存（传统过期方式）
     */
    public void setQuotePictureCacheWithExpire(Integer pictureId, QuotePicture quotePicture, long time, TimeUnit unit) {
        String key = Constants.REDIS_QUOTE_PICTURE_PREFIX + "single:" + pictureId;
        redisUtil.setQuotePicture(key, quotePicture, time, unit);
    }

    /**
     * 设置QuotePicture列表缓存（传统过期方式）
     */
    public void setQuotePictureListCacheWithExpire(Integer quoteId, List<QuotePicture> quotePictures, long time, TimeUnit unit) {
        String key = Constants.REDIS_QUOTE_PICTURE_PREFIX + "list:" + quoteId;
        redisUtil.setQuotePictureList(key, quotePictures, time, unit);
    }
}
