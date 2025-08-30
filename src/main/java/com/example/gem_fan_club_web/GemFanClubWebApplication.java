package com.example.gem_fan_club_web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAspectJAutoProxy(exposeProxy = true)
@EnableScheduling  // 启用定时任务
@SpringBootApplication
public class GemFanClubWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(GemFanClubWebApplication.class, args);
	}

}
