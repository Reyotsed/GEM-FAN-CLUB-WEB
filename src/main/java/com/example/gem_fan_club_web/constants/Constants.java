package com.example.gem_fan_club_web.constants;

public class Constants {
    // 放在Redis的哪个文件夹里面，一般一个项目都放在一个文件夹里：
    public static final String REDIS_KEY_PREFIX = "gem_fan_club:";

    // checkCode放在哪个文件夹里:
    public static String REDIS_KEY_CHECK_CODE = REDIS_KEY_PREFIX + "checkcode:";

    public static String REDIS_TICKET_PREFIX = REDIS_KEY_PREFIX + "ticket:";

    public static String REDIS_TICKET_STOCK = REDIS_TICKET_PREFIX + "stock:";

    public static String REDIS_TICKET_ORDER = REDIS_TICKET_PREFIX + "order:";

    public static String REDIS_TICKET_ORDER_STREAM = REDIS_KEY_PREFIX + "order:stream";

    public static String REDIS_QUOTE_PREFIX = REDIS_KEY_PREFIX + "quote:";

    public static String REDIS_QUOTE_PICTURE_PREFIX = REDIS_KEY_PREFIX + "quote:picture:";

    public static final String REDIS_KEY_TOKEN_WEB = REDIS_KEY_PREFIX + "token:web:";

    public static final String TOKEN_WEB = "token";

    public static final String TOKEN_HEADER = "Authorization";

    public static final String TOKEN_BEARER = "Bearer ";

    // ========== 流量统计相关 ==========
    public static final String REDIS_STATS_PREFIX = REDIS_KEY_PREFIX + "stats:";
    public static final String REDIS_STATS_PV_DAILY = REDIS_STATS_PREFIX + "pv:daily:";
    public static final String REDIS_STATS_PV_TOTAL = REDIS_STATS_PREFIX + "pv:total:";
    public static final String REDIS_STATS_EVENT_DAILY = REDIS_STATS_PREFIX + "event:daily:";
    public static final String REDIS_STATS_EVENT_TOTAL = REDIS_STATS_PREFIX + "event:total:";
    public static final String REDIS_STATS_UV_DAILY = REDIS_STATS_PREFIX + "uv:daily:";
}
