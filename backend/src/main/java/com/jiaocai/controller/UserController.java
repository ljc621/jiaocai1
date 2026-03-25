package com.jiaocai.controller;

import com.jiaocai.common.Result;
import com.jiaocai.entity.User;
import com.jiaocai.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public Result<java.util.List<User>> list() {
        return Result.success(userService.list());
    }

    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable String id) {
        return Result.success(userService.getById(id));
    }

    @PostMapping
    public Result<User> create(@RequestBody User user) {
        // Set default values if not provided
        if (user.getCreditScore() == null) {
            user.setCreditScore(605); // Default to 605
        }
        if (user.getVerified() == null) {
            user.setVerified(false);
        }
        if (user.getRole() == null) {
            user.setRole("user");
        }
        // Set default password if missing (in real app, should be encrypted)
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword("123456");
        }
        userService.save(user);
        return Result.success(user);
    }

    @PutMapping
    public Result<User> update(@RequestBody User user) {
        if (user.getPassword() != null && user.getPassword().isEmpty()) {
            user.setPassword(null);
        }
        userService.updateById(user);
        return Result.success(user);
    }

    @PostMapping("/{id}/recharge")
    public Result<User> recharge(@PathVariable String id, @RequestBody Map<String, Double> payload) {
        return Result.success(userService.recharge(id, payload.get("amount")));
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable String id) {
        userService.removeById(id);
        return Result.success("User deleted");
    }
}
