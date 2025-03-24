package com.example.gem_fan_club_web.constants;

public class Constants {
    // 放在Redis的哪个文件夹里面，一般一个项目都放在一个文件夹里：
    public static final String REDIS_KEY_PREFIX = "gem_fan_club:";

    // checkCode放在哪个文件夹里:
    public static String REDIS_KEY_CHECK_CODE = REDIS_KEY_PREFIX + "checkcode:";


    public static final String REDIS_KEY_TOKEN_WEB = REDIS_KEY_PREFIX + "token:web:";

    public static final String TOKEN_WEB = "token";

    public static final String TOKEN_HEADER = "Authorization";

    public static final String TOKEN_BEARER = "Bearer ";
}
