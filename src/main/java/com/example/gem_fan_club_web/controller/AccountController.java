package com.example.gem_fan_club_web.controller;

import com.example.gem_fan_club_web.constants.Constants;
import com.example.gem_fan_club_web.model.ResponseDTO;
import com.example.gem_fan_club_web.redis.RedisService;
import com.example.gem_fan_club_web.redis.RedisUtils;
import com.example.gem_fan_club_web.service.UserService;
import com.example.gem_fan_club_web.utils.StringTools;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.wf.captcha.ArithmeticCaptcha;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private UserService userService;
    @Autowired
    private RedisService redisService;


    @PostMapping("/checkCode")
    public ResponseDTO checkCode(){
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 50);
        String code = captcha.text();
        String checkCodeKey = redisService.setCheckCodeKey(code);

        String checkCodeBase64 = captcha.toBase64();
        Map<String,String> result = new HashMap<>();
        result.put("checkCode", checkCodeBase64);
        result.put("checkCodeKey", checkCodeKey);

        return new ResponseDTO(200,"success",result);
    }


    @PostMapping("/register")
    public ResponseDTO register(
            @RequestParam("email") String email,
            @RequestParam("nickName") String nickName,
            @RequestParam("password") String password,
            @RequestParam("checkCodeKey") String checkCodeKey,
            @RequestParam("checkCode") String checkCode)
    {
        try {
            if (!checkCode.equals(redisService.getCheckCode(checkCodeKey))) {
                return new ResponseDTO(500, "图片验证码错误", null);
            }
            if (userService.register(email,nickName,password)){
                return new ResponseDTO(200, "success", null);
            }else{
                return new ResponseDTO(502,"邮箱已存在",null);
            }
        }finally {
            redisService.cleanCheckCode(checkCodeKey);
        }
    }


    @PostMapping("/login")
    public ResponseDTO login(
            HttpServletRequest request,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam(value = "checkCodeKey", required = false) String checkCodeKey,
            @RequestParam(value = "checkCode", required = false) String checkCode
    ) {
        try{
            if (!checkCode.equals(redisService.getCheckCode(checkCodeKey))){
                return new ResponseDTO(500,"图片验证码错误",null);
            }
            // 登录验证，成功就生成token返回回去
            if (userService.login(email, password)){
                String token = UUID.randomUUID().toString();
                redisService.setToken(token,email);
                return new ResponseDTO(200,"success",new HashMap<String,Object>(){{
                    put("token",token);
                    put("userInfo",userService.getUserByEmail(email));
                }});
            }else{
                // 登录失败就删除token
                Cookie[] cookies = request.getCookies();
                String token = null;
                if (cookies != null) {
                    for(Cookie cookie:cookies){
                        if(cookie.getName().equals(Constants.TOKEN_WEB)){
                            token = cookie.getValue();
                        }
                    }
                }
                redisService.cleanToken(token);
                return new ResponseDTO(500,"登录失败", null);
            }
        }finally {
            // 用过之后就把验证码删除
            redisService.cleanCheckCode(checkCodeKey);
        }
    }

    @PostMapping("/autoLogin")
    public ResponseDTO autoLogin(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader(Constants.TOKEN_HEADER);
        if(token != null) {
            token = token.replace(Constants.TOKEN_BEARER, "");
            String email = redisService.getEmailByToken(token);
            if (StringTools.isEmpty(email)) {
                return new ResponseDTO(501,"token失效",null);
            }else{
                return new ResponseDTO(200,"success", userService.getUserByEmail(email));
            }
        }else{
            return new ResponseDTO(501,"请求无token",null);
        }
    }
}