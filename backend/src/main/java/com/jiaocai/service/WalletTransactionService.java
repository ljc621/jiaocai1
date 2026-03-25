package com.jiaocai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jiaocai.entity.WalletTransaction;

public interface WalletTransactionService extends IService<WalletTransaction> {
    void recharge(String userId, Double amount);
    void withdraw(String userId, Double amount);
    void pay(String userId, Double amount, String orderId);
    void income(String userId, Double amount, String orderId);
    void exchange(String userId, Integer points);

    void rewardPoints(String userId, Integer points, String description);
}
