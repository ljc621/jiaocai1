package com.jiaocai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiaocai.entity.User;
import com.jiaocai.entity.WalletTransaction;
import com.jiaocai.mapper.WalletTransactionMapper;
import com.jiaocai.service.PointTransactionService;
import com.jiaocai.service.UserService;
import com.jiaocai.service.WalletTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class WalletTransactionServiceImpl extends ServiceImpl<WalletTransactionMapper, WalletTransaction> implements WalletTransactionService {

    @Autowired
    private UserService userService;

    @Autowired
    private PointTransactionService pointTransactionService;

    @Override
    public WalletTransactionMapper getBaseMapper() {
        return super.getBaseMapper();
    }

    @Override
    @Transactional
    public void recharge(String userId, Double amount) {
        User user = userService.getById(userId);
        if (user == null) throw new RuntimeException("User not found");

        user.setBalance((user.getBalance() == null ? 0 : user.getBalance()) + amount);
        userService.updateById(user);

        WalletTransaction tx = new WalletTransaction();
        tx.setUserId(userId);
        tx.setAmount(amount);
        tx.setType("recharge");
        tx.setStatus("success");
        tx.setDescription("余额充值");
        tx.setBalance(user.getBalance());
        tx.setCreatedAt(LocalDateTime.now());
        this.save(tx);
    }

    @Override
    @Transactional
    public void withdraw(String userId, Double amount) {
        User user = userService.getById(userId);
        if (user == null) throw new RuntimeException("User not found");
        if (user.getBalance() == null || user.getBalance() < amount) {
            throw new RuntimeException("余额不足");
        }

        user.setBalance(user.getBalance() - amount);
        userService.updateById(user);

        WalletTransaction tx = new WalletTransaction();
        tx.setUserId(userId);
        tx.setAmount(amount);
        tx.setType("withdraw");
        tx.setStatus("success");
        tx.setDescription("余额提现");
        tx.setBalance(user.getBalance());
        tx.setCreatedAt(LocalDateTime.now());
        this.save(tx);
    }

    @Override
    @Transactional
    public void pay(String userId, Double amount, String orderId) {
        User user = userService.getById(userId);
        if (user == null) throw new RuntimeException("User not found");
        if (user.getBalance() == null || user.getBalance() < amount) {
            throw new RuntimeException("余额不足");
        }

        user.setBalance(user.getBalance() - amount);
        userService.updateById(user);

        WalletTransaction tx = new WalletTransaction();
        tx.setUserId(userId);
        tx.setAmount(amount);
        tx.setType("payment");
        tx.setStatus("success");
        tx.setDescription("支付订单: " + orderId);
        tx.setBalance(user.getBalance());
        tx.setCreatedAt(LocalDateTime.now());
        this.save(tx);
    }

    @Override
    @Transactional
    public void income(String userId, Double amount, String orderId) {
        // Skip if amount is 0 (e.g. donation orders)
        if (amount == null || amount <= 0.001) {
            return;
        }

        User user = userService.getById(userId);
        if (user == null) throw new RuntimeException("User not found");

        user.setBalance((user.getBalance() == null ? 0 : user.getBalance()) + amount);
        userService.updateById(user);

        WalletTransaction tx = new WalletTransaction();
        tx.setUserId(userId);
        tx.setAmount(amount);
        tx.setType("income");
        tx.setStatus("success");
        tx.setDescription("订单收入: " + orderId);
        tx.setBalance(user.getBalance());
        tx.setCreatedAt(LocalDateTime.now());
        this.save(tx);
    }

    @Override
    @Transactional
    public void exchange(String userId, Integer points) {
        // 1. Validate and Deduct Points
        if (points < 100) {
            throw new RuntimeException("Minimum exchange amount is 100 points");
        }
        // This service call handles points deduction and recording
        pointTransactionService.deductPoints(userId, points, "exchange_deduct", "积分兑换余额");

        // 2. Add Balance
        User user = userService.getById(userId); // Reload user
        if (user == null) throw new RuntimeException("User not found");

        Double amount = points / 100.0;
        user.setBalance((user.getBalance() == null ? 0 : user.getBalance()) + amount);
        userService.updateById(user);

        // 3. Record Money Transaction
        WalletTransaction tx = new WalletTransaction();
        tx.setUserId(userId);
        tx.setAmount(amount);
        tx.setType("exchange");
        tx.setStatus("success");
        tx.setDescription("积分兑换: " + points + " 积分");
        tx.setBalance(user.getBalance());
        tx.setCreatedAt(LocalDateTime.now());
        this.save(tx);
    }

    @Override
    @Transactional
    public void rewardPoints(String userId, Integer points, String description) {
        pointTransactionService.addPoints(userId, points, "point_reward", description);
    }
}
