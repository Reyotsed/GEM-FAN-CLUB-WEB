package com.example.gem_fan_club_web.service;

import com.example.gem_fan_club_web.constants.Constants;
import com.example.gem_fan_club_web.model.SiteStats;
import com.example.gem_fan_club_web.redis.RedisUtils;
import com.example.gem_fan_club_web.repository.SiteStatsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class StatsService {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private SiteStatsRepository siteStatsRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    // 允许的 PV key 白名单，防止恶意构造
    private static final Set<String> VALID_PV_KEYS = Set.of(
        "home", "song", "quote", "quote_detail", "picture", "shop",
        "ai", "info", "games", "guess_song", "lyrics_chain", "ticket_rush", "quiz", "user"
    );

    // 允许的 Event key 白名单
    private static final Set<String> VALID_EVENT_KEYS = Set.of(
        "game_start:guess_song", "game_complete:guess_song",
        "game_start:lyrics_chain", "game_complete:lyrics_chain",
        "game_start:ticket_rush", "game_complete:ticket_rush",
        "ai_chat", "song_play", "quote_like", "quote_comment",
        "login", "register"
    );

    /**
     * 记录页面访问（PV）
     */
    public void trackPageView(String pageKey, String userId) {
        if (!VALID_PV_KEYS.contains(pageKey)) {
            log.warn("无效的 PV key: {}", pageKey);
            return;
        }

        String today = LocalDate.now().format(DATE_FMT);

        // Redis 原子自增：每日 PV
        String dailyKey = Constants.REDIS_STATS_PV_DAILY + today + ":" + pageKey;
        redisUtils.increment(dailyKey);

        // Redis 原子自增：总 PV
        String totalKey = Constants.REDIS_STATS_PV_TOTAL + pageKey;
        redisUtils.increment(totalKey);

        // UV 统计（基于 HyperLogLog）
        if (userId != null && !userId.isEmpty()) {
            String uvKey = Constants.REDIS_STATS_UV_DAILY + today;
            redisUtils.pfAdd(uvKey, userId);
        }
    }

    /**
     * 记录用户行为事件
     */
    public void trackEvent(String eventKey, String userId) {
        if (!VALID_EVENT_KEYS.contains(eventKey)) {
            log.warn("无效的 Event key: {}", eventKey);
            return;
        }

        String today = LocalDate.now().format(DATE_FMT);

        // Redis 原子自增：每日事件
        String dailyKey = Constants.REDIS_STATS_EVENT_DAILY + today + ":" + eventKey;
        redisUtils.increment(dailyKey);

        // Redis 原子自增：总事件
        String totalKey = Constants.REDIS_STATS_EVENT_TOTAL + eventKey;
        redisUtils.increment(totalKey);

        // UV 统计
        if (userId != null && !userId.isEmpty()) {
            String uvKey = Constants.REDIS_STATS_UV_DAILY + today;
            redisUtils.pfAdd(uvKey, userId);
        }
    }

    /**
     * 获取今日统计概览
     */
    public Map<String, Object> getTodayOverview() {
        String today = LocalDate.now().format(DATE_FMT);
        Map<String, Object> overview = new LinkedHashMap<>();

        // 今日总 PV
        long todayTotalPv = 0;
        Map<String, Long> todayPvDetail = new LinkedHashMap<>();
        for (String pageKey : VALID_PV_KEYS) {
            String key = Constants.REDIS_STATS_PV_DAILY + today + ":" + pageKey;
            String val = redisUtils.get(key);
            long count = val != null ? Long.parseLong(val) : 0;
            todayPvDetail.put(pageKey, count);
            todayTotalPv += count;
        }
        overview.put("todayPv", todayTotalPv);
        overview.put("todayPvDetail", todayPvDetail);

        // 今日 UV
        String uvKey = Constants.REDIS_STATS_UV_DAILY + today;
        Long todayUv = redisUtils.pfCount(uvKey);
        overview.put("todayUv", todayUv != null ? todayUv : 0);

        // 今日事件统计
        Map<String, Long> todayEventDetail = new LinkedHashMap<>();
        for (String eventKey : VALID_EVENT_KEYS) {
            String key = Constants.REDIS_STATS_EVENT_DAILY + today + ":" + eventKey;
            String val = redisUtils.get(key);
            long count = val != null ? Long.parseLong(val) : 0;
            todayEventDetail.put(eventKey, count);
        }
        overview.put("todayEvents", todayEventDetail);

        // 历史累计 PV
        long allTimePv = 0;
        Map<String, Long> totalPvDetail = new LinkedHashMap<>();
        for (String pageKey : VALID_PV_KEYS) {
            String key = Constants.REDIS_STATS_PV_TOTAL + pageKey;
            String val = redisUtils.get(key);
            long count = val != null ? Long.parseLong(val) : 0;
            totalPvDetail.put(pageKey, count);
            allTimePv += count;
        }
        overview.put("totalPv", allTimePv);
        overview.put("totalPvDetail", totalPvDetail);

        // 历史累计事件
        Map<String, Long> totalEventDetail = new LinkedHashMap<>();
        for (String eventKey : VALID_EVENT_KEYS) {
            String key = Constants.REDIS_STATS_EVENT_TOTAL + eventKey;
            String val = redisUtils.get(key);
            long count = val != null ? Long.parseLong(val) : 0;
            totalEventDetail.put(eventKey, count);
        }
        overview.put("totalEvents", totalEventDetail);

        return overview;
    }

    /**
     * 获取指定日期范围的趋势数据（从 MySQL 查）
     */
    public Map<String, Object> getTrend(String statKey, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<SiteStats> statsList = siteStatsRepository.findByStatKeyAndStatDateBetween(statKey, startDate, endDate);

        // 构建日期到数值的映射
        Map<String, Integer> dateMap = new LinkedHashMap<>();
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            dateMap.put(d.toString(), 0);
        }
        for (SiteStats s : statsList) {
            dateMap.put(s.getStatDate().toString(), s.getStatCount());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("statKey", statKey);
        result.put("dates", new ArrayList<>(dateMap.keySet()));
        result.put("counts", new ArrayList<>(dateMap.values()));
        return result;
    }

    /**
     * 定时任务：每天凌晨 00:05 将昨天的 Redis 计数持久化到 MySQL
     */
    @Scheduled(cron = "0 5 0 * * ?")
    public void persistYesterdayStats() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String dateStr = yesterday.format(DATE_FMT);
        log.info("开始持久化 {} 的统计数据到 MySQL", yesterday);

        // 持久化 PV 数据
        for (String pageKey : VALID_PV_KEYS) {
            String redisKey = Constants.REDIS_STATS_PV_DAILY + dateStr + ":" + pageKey;
            persistSingleStat(yesterday, "pv:" + pageKey, redisKey);
        }

        // 持久化 Event 数据
        for (String eventKey : VALID_EVENT_KEYS) {
            String redisKey = Constants.REDIS_STATS_EVENT_DAILY + dateStr + ":" + eventKey;
            persistSingleStat(yesterday, "event:" + eventKey, redisKey);
        }

        // 持久化 UV
        String uvRedisKey = Constants.REDIS_STATS_UV_DAILY + dateStr;
        Long uvCount = redisUtils.pfCount(uvRedisKey);
        if (uvCount != null && uvCount > 0) {
            saveStat(yesterday, "uv", uvCount.intValue());
        }

        log.info("持久化 {} 统计数据完成", yesterday);
    }

    /**
     * 手动触发持久化指定日期的统计数据
     */
    public void persistStatsForDate(LocalDate date) {
        String dateStr = date.format(DATE_FMT);
        log.info("手动持久化 {} 的统计数据到 MySQL", date);

        for (String pageKey : VALID_PV_KEYS) {
            String redisKey = Constants.REDIS_STATS_PV_DAILY + dateStr + ":" + pageKey;
            persistSingleStat(date, "pv:" + pageKey, redisKey);
        }

        for (String eventKey : VALID_EVENT_KEYS) {
            String redisKey = Constants.REDIS_STATS_EVENT_DAILY + dateStr + ":" + eventKey;
            persistSingleStat(date, "event:" + eventKey, redisKey);
        }

        String uvRedisKey = Constants.REDIS_STATS_UV_DAILY + dateStr;
        Long uvCount = redisUtils.pfCount(uvRedisKey);
        if (uvCount != null && uvCount > 0) {
            saveStat(date, "uv", uvCount.intValue());
        }
    }

    private void persistSingleStat(LocalDate date, String statKey, String redisKey) {
        try {
            String val = redisUtils.get(redisKey);
            if (val != null) {
                int count = Integer.parseInt(val);
                if (count > 0) {
                    saveStat(date, statKey, count);
                }
            }
        } catch (Exception e) {
            log.error("持久化统计项失败: date={}, statKey={}, redisKey={}", date, statKey, redisKey, e);
        }
    }

    private void saveStat(LocalDate date, String statKey, int count) {
        Optional<SiteStats> existing = siteStatsRepository.findByStatDateAndStatKey(date, statKey);
        if (existing.isPresent()) {
            SiteStats stats = existing.get();
            stats.setStatCount(count);
            siteStatsRepository.save(stats);
        } else {
            SiteStats stats = new SiteStats();
            stats.setStatDate(date);
            stats.setStatKey(statKey);
            stats.setStatCount(count);
            siteStatsRepository.save(stats);
        }
    }
}
