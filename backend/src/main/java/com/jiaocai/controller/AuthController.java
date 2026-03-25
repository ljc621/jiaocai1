package com.jiaocai.controller;

import com.jiaocai.common.Result;
import com.jiaocai.entity.User;
import com.jiaocai.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<User> login(@RequestBody User loginDto) {
        User user = userService.login(loginDto.getPhone(), loginDto.getPassword());
        if (user != null) {
            return Result.success(user);
        }
        return Result.error("Invalid credentials");
    }

    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        return Result.success(userService.register(user));
    }
}
