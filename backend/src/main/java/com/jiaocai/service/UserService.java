package com.jiaocai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jiaocai.entity.User;

public interface UserService extends IService<User> {
    User login(String phone, String password);
    User register(User user);
    User recharge(String userId, Double amount);
}
