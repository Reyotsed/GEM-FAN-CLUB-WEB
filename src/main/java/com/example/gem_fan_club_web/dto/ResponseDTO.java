package com.example.gem_fan_club_web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 响应包装类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
// 自定义响应对象
public class ResponseDTO {
    public int code = 200;
    public String message = "OK";
    public Object data;


}
