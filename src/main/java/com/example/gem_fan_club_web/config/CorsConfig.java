package com.example.gem_fan_club_web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Profile("dev")
    @Bean
    public WebMvcConfigurer devCorsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(
                                "http://localhost:5173",
                                "https://localhost",
                                "capacitor://localhost",
                                "http://localhost"
                        )
                        .allowedMethods("*")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }

    @Profile("prod")
    @Bean
    public WebMvcConfigurer prodCorsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(
                                "http://gemfanclub.com",
                                "http://www.gemfanclub.com",
                                "https://gemfanclub.com",
                                "https://www.gemfanclub.com",
                                "http://140.143.246.54",
                                "http://140.143.246.54:5173",
                                "https://localhost",
                                "capacitor://localhost",
                                "http://localhost"
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}