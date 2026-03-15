package com.example.gem_fan_club_web.controller;

import com.example.gem_fan_club_web.dto.ResponseDTO;
import com.example.gem_fan_club_web.dto.StatsTrackRequest;
import com.example.gem_fan_club_web.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/stats")
public class StatsController {

    @Autowired
    private StatsService statsService;

    /**
     * 埋点上报接口（前端统一调用此接口上报 PV / 事件）
     */
    @PostMapping("/track")
    public ResponseDTO track(@RequestBody StatsTrackRequest request) {
        if (request.getType() == null || request.getKey() == null) {
            return new ResponseDTO(400, "参数缺失", null);
        }

        switch (request.getType()) {
            case "pv":
                statsService.trackPageView(request.getKey(), request.getUserId());
                break;
            case "event":
                statsService.trackEvent(request.getKey(), request.getUserId());
                break;
            default:
                return new ResponseDTO(400, "无效的 type，仅支持 pv / event", null);
        }

        return new ResponseDTO(200, "success", null);
    }

    /**
     * 获取今日统计概览（含今日PV、UV、事件 + 历史累计）
     */
    @GetMapping("/overview")
    public ResponseDTO getOverview() {
        Map<String, Object> overview = statsService.getTodayOverview();
        return new ResponseDTO(200, "success", overview);
    }

    /**
     * 获取指定统计项的趋势数据（最近 N 天）
     * @param statKey 统计项 key，如 pv:home, event:ai_chat
     * @param days 天数，默认7天
     */
    @GetMapping("/trend")
    public ResponseDTO getTrend(
            @RequestParam("statKey") String statKey,
            @RequestParam(value = "days", defaultValue = "7") int days) {
        if (days < 1 || days > 90) {
            return new ResponseDTO(400, "days 参数范围 1-90", null);
        }
        Map<String, Object> trend = statsService.getTrend(statKey, days);
        return new ResponseDTO(200, "success", trend);
    }

    /**
     * 手动触发持久化指定日期的统计数据到 MySQL
     */
    @PostMapping("/persist")
    public ResponseDTO persist(
            @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        statsService.persistStatsForDate(date);
        return new ResponseDTO(200, "success", null);
    }
}
