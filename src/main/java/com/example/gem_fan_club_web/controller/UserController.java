package com.example.gem_fan_club_web.controller;

import com.example.gem_fan_club_web.model.ResponseDTO;
import com.example.gem_fan_club_web.model.User;
import com.example.gem_fan_club_web.model.quote.Quote;
import com.example.gem_fan_club_web.service.UserService;
import com.example.gem_fan_club_web.utils.FileTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private FileTools fileTools = new FileTools();

    @Autowired
    private UserService userService;

    @GetMapping("/getUserInfo")
    public ResponseDTO getUserInfoById(@RequestParam String userId) {
        User user = userService.getUserById(userId);
        return new ResponseDTO(200,"success",user);
    }

    @PostMapping("updateUserInfo")
    public ResponseDTO updateUserInfo(
           @RequestParam("userId") String userId,
           @RequestParam("nickName") String nickName,
           @RequestParam("birthday") String birthday,
           @RequestParam("personIntroduction") String personalIntroduction
    ) {
        User user = userService.getUserById(userId);
        user.setNickName(nickName);
        user.setBirthday(birthday);
        user.setPersonIntroduction(personalIntroduction);

        userService.saveUser(user);
        return new ResponseDTO(200,"success",user);
    }

    @PostMapping("/updateAvatar")
    public ResponseDTO updateAvatar(
            @RequestParam("userId") String userId,
            @RequestParam("image") MultipartFile image) {

        // 这里可以添加保存文件和其他业务逻辑
        try {

            String imagePath = fileTools.saveFile(image,uploadDir);
            User user = userService.getUserById(userId);
            user.setAvatar(imagePath);
            userService.saveUser(user);
            return new ResponseDTO(200,"success",imagePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
