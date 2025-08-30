package com.example.gem_fan_club_web.config; // 建议创建一个config包

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestConfig {

    @Bean
    public RestTemplate restTemplate() {
        // 在这里可以进行更复杂的配置，比如设置超时时间等
        return new RestTemplate();
    }
}