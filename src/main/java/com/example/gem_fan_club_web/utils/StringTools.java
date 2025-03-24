package com.example.gem_fan_club_web.utils;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
@Component
public class StringTools {

    public static boolean isEmpty(String str) {
        if (null == str || "".equals(str) || "null".equals(str) || "\u0000".equals(str)) {
            return true;
        } else if ("".equals(str.trim())) {
            return true;
        }
        return false;
    }

    public static String encodeByMd5(String originString) {
        return  StringTools.isEmpty(originString) ? null : DigestUtils.md5DigestAsHex(originString.getBytes());
    }
    public static String getRandomString(Integer count) {
        return RandomStringUtils.random(count,true,true);
    }
    public static String getRandomNumber(int count) {
        return RandomStringUtils.random(count,false,true);
    }
}
