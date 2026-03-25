package com.jiaocai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiaocai.entity.User;
import com.jiaocai.mapper.UserMapper;
import com.jiaocai.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User login(String phone, String password) {
        // Simple mock login - in production use BCrypt
        return this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, phone)
                .eq(User::getPassword, password));
    }

    @Override
    public User register(User user) {
        this.save(user);
        return user;
    }

    @Override
    public User recharge(String userId, Double amount) {
        if (amount == null || amount <= 0) {
            throw new RuntimeException("Invalid amount");
        }
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        Double currentBalance = user.getBalance() == null ? 0.0 : user.getBalance();
        user.setBalance(currentBalance + amount);
        this.updateById(user);
        return user;
    }
}
