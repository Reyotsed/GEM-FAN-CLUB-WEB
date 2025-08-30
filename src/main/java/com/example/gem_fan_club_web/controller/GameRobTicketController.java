package com.example.gem_fan_club_web.controller;

import com.example.gem_fan_club_web.dto.ResponseDTO;
import com.example.gem_fan_club_web.redis.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.core.io.ClassPathResource;
import java.util.Collections;
import cn.hutool.core.lang.Snowflake;
import com.example.gem_fan_club_web.service.OrderStreamService;
import com.example.gem_fan_club_web.service.RateLimitService;

@RestController
@RequestMapping("/game/ticket")
public class GameRobTicketController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisService redisService;

    @Autowired
private Snowflake snowflake;

@Autowired
private OrderStreamService orderStreamService;

@Autowired
private RateLimitService rateLimitService;

    private static final DefaultRedisScript<Long> SECKILL_LUA;
    static {
        SECKILL_LUA = new DefaultRedisScript<>();
        SECKILL_LUA.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_LUA.setResultType(Long.class);
    }

    // userId用户发起了一个抢ticketId票的请求
    @PostMapping("/robTicket")
    public ResponseDTO robTicket(
            @RequestParam("userId") String userId,
            @RequestParam("ticketId") String ticketId
    ) {
        try {
            // 1-限流检查
            if (!rateLimitService.allowRequest(userId, ticketId)) {
                return new ResponseDTO(429, "error", "系统繁忙，请稍后重试");
            }
            
            // 2-判断当前库存是否大于0
            Long result = stringRedisTemplate.execute(
                SECKILL_LUA,
                Collections.emptyList(),
                ticketId,
                userId
            );
            if (result == null) {
                return new ResponseDTO(500,"error","系统异常");
            }
            if (result == 1) {
                return new ResponseDTO(500,"error","库存不足");
            }
            if (result == 2) {
                return new ResponseDTO(500,"error","重复下单");
            }
            long orderId = snowflake.nextId();
            
            // 发送订单消息到Redis Stream，异步处理
            orderStreamService.sendOrderMessage(
                String.valueOf(orderId), 
                userId, 
                ticketId
            );

            return new ResponseDTO(200,"success","抢票成功，订单号: " + orderId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseDTO(500,"error","系统异常");
        }
    }

    @GetMapping("/setStock")
    public ResponseDTO setStock(
            @RequestParam("stock") String stock,
            @RequestParam("ticketId") String ticketId
    ) {
        redisService.setTicketStock(ticketId, stock);
        return new ResponseDTO(200,"success","设置库存成功");
    }

    @GetMapping("/getStock")
    public ResponseDTO getStock(
            @RequestParam("ticketId") String ticketId
    ) {
        String stock = redisService.getTicketStock(ticketId);
        return new ResponseDTO(200,"success",stock);
    }

    @GetMapping("/clearStock")
    public ResponseDTO clearStock(
            @RequestParam("ticketId") String ticketId
    ) {
        redisService.cleanTicketStock(ticketId);
        redisService.cleanTicketOrder(ticketId);
        redisService.cleanTicketOrderStream();
        return new ResponseDTO(200,"success","清除库存成功");
    }
}
