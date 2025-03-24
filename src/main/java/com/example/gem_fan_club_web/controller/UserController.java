package com.example.gem_fan_club_web.controller;

import com.example.gem_fan_club_web.model.ResponseDTO;
import com.example.gem_fan_club_web.model.User;
import com.example.gem_fan_club_web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/getUserInfo")
    public ResponseDTO getUserInfoById(@RequestParam String userId) {
        User user = userService.getUserById(userId);
        return new ResponseDTO(200,"success",user);
    }
}
