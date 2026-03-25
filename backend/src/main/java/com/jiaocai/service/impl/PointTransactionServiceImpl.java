package com.jiaocai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiaocai.entity.PointTransaction;
import com.jiaocai.entity.User;
import com.jiaocai.mapper.PointTransactionMapper;
import com.jiaocai.service.PointTransactionService;
import com.jiaocai.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PointTransactionServiceImpl extends ServiceImpl<PointTransactionMapper, PointTransaction> implements PointTransactionService {

    @Autowired
    private UserService userService;

    @Override
    public PointTransactionMapper getBaseMapper() {
        return super.getBaseMapper();
    }

    @Override
    @Transactional
    public void addPoints(String userId, Integer points, String type, String description) {
        if (points == null || points <= 0) return;

        User user = userService.getById(userId);
        if (user == null) throw new RuntimeException("User not found");

        Integer currentPoints = user.getPoints() == null ? 0 : user.getPoints();
        user.setPoints(currentPoints + points);
        userService.updateById(user);

        PointTransaction tx = new PointTransaction();
        tx.setUserId(userId);
        tx.setPoints(points);
        tx.setType(type);
        tx.setDescription(description);
        tx.setBalance(user.getPoints());
        tx.setCreatedAt(LocalDateTime.now());
        this.save(tx);
    }

    @Override
    @Transactional
    public void deductPoints(String userId, Integer points, String type, String description) {
        if (points == null || points <= 0) return;

        User user = userService.getById(userId);
        if (user == null) throw new RuntimeException("User not found");

        Integer currentPoints = user.getPoints() == null ? 0 : user.getPoints();
        if (currentPoints < points) {
            throw new RuntimeException("Insufficient points");
        }
        
        user.setPoints(currentPoints - points);
        userService.updateById(user);

        PointTransaction tx = new PointTransaction();
        tx.setUserId(userId);
        tx.setPoints(-points); // Negative for deduction
        tx.setType(type);
        tx.setDescription(description);
        tx.setBalance(user.getPoints());
        tx.setCreatedAt(LocalDateTime.now());
        this.save(tx);
    }

    @Override
    public List<PointTransaction> getTransactionsByUserId(String userId) {
        LambdaQueryWrapper<PointTransaction> query = new LambdaQueryWrapper<>();
        query.eq(PointTransaction::getUserId, userId);
        query.orderByDesc(PointTransaction::getCreatedAt);
        return this.list(query);
    }
}
