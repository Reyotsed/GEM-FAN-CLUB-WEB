package com.example.gem_fan_club_web.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Table;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "user_info")
@Data
public class User {
    /**
     * 用户id
     */
    @Id
    private String userId;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 邮箱
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * 密码
     */
    @Column(nullable = false)
    private String password;

    /**
     * 0:女 1:男 2：未知
     */
    private Integer sex;

    /**
     * 出生日期
     */
    private String birthday;

    /**
     * 个人简介
     */
    private String personIntroduction;

    /**
     * 加入时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date joinTime;

    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastLoginTime;

    /**
     * 最后登录ip
     */
    private String lastLoginIp;

    /**
     * 0:禁用 1:正常
     */
    private Integer status;

    /**
     * 主题
     */
    private Integer theme;

    private String avatar;
}
