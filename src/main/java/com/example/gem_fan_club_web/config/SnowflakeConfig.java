package com.example.gem_fan_club_web.config;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.IdUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 雪花算法配置类
 * 使用Hutool的雪花算法实现
 * 支持区分不同服务的ID生成器
 */
@Configuration
public class SnowflakeConfig {

    @Value("${snowflake.worker-id:-1}")
    private long workerId;
    
    @Value("${snowflake.data-center-id:1}")
    private long dataCenterId;

    /**
     * 默认雪花算法实例
     * 用于通用业务
     */
    @Bean
    public Snowflake snowflake() {
        return createSnowflake(workerId, dataCenterId);
    }
    
    /**
     * 订单服务专用雪花算法
     * workerId = 1, dataCenterId = 1
     */
    @Bean("orderSnowflake")
    public Snowflake orderSnowflake() {
        return createSnowflake(1L, 1L);
    }
    
    /**
     * 用户服务专用雪花算法
     * workerId = 2, dataCenterId = 1
     */
    @Bean("userSnowflake")
    public Snowflake userSnowflake() {
        return createSnowflake(2L, 1L);
    }
    
    /**
     * 商品服务专用雪花算法
     * workerId = 3, dataCenterId = 1
     */
    @Bean("productSnowflake")
    public Snowflake productSnowflake() {
        return createSnowflake(3L, 1L);
    }
    
    /**
     * 优惠券服务专用雪花算法
     * workerId = 4, dataCenterId = 1
     */
    @Bean("voucherSnowflake")
    public Snowflake voucherSnowflake() {
        return createSnowflake(4L, 1L);
    }
    
    /**
     * 创建雪花算法实例
     * @param workerId 工作机器ID (0~31)
     * @param dataCenterId 数据中心ID (0~31)
     * @return Snowflake实例
     */
    private Snowflake createSnowflake(long workerId, long dataCenterId) {
        // 如果workerId为-1，则使用IP地址最后一段
        if (workerId == -1) {
            workerId = NetUtil.ipv4ToLong(NetUtil.getLocalhostStr()) % 32;
        }
        
        // 验证参数合法性
        if (workerId < 0 || workerId > 31) {
            throw new IllegalArgumentException("Worker ID must be between 0 and 31");
        }
        if (dataCenterId < 0 || dataCenterId > 31) {
            throw new IllegalArgumentException("DataCenter ID must be between 0 and 31");
        }
        
        return IdUtil.getSnowflake(workerId, dataCenterId);
    }
} 