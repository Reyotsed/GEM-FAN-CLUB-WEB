package com.example.gem_fan_club_web.service;

import com.example.gem_fan_club_web.model.User;
import com.example.gem_fan_club_web.repository.UserRepository;
import com.example.gem_fan_club_web.utils.StringTools;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Boolean login(String email, String password) {
        password = StringTools.encodeByMd5(password);
        User user = userRepository.findByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            user.setLastLoginTime(new Date());
            userRepository.save(user);
            return true;
        } else {
            return false;
        }
    }

    public User getUserByEmail(String email){
        return userRepository.findByEmail(email);
    }

    public boolean register(String email, String nickName, String password) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            return false;
        }
        password = StringTools.encodeByMd5(password);
        user = new User();
        user.setEmail(email);
        user.setUserId(StringTools.getRandomNumber(10));
        user.setNickName(nickName);
        user.setPassword(password);
        user.setJoinTime(new Date());
        user.setLastLoginTime(new Date());
        user.setTheme(1);
        user.setSex(2);
        user.setStatus(1);
//        System.out.println(user);
        userRepository.save(user);
        return true;
    }

    public User getUserById(String userId) {
        return userRepository.findById(userId).orElse(null);
    }
}