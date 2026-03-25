package com.jiaocai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jiaocai.entity.PointTransaction;
import java.util.List;

public interface PointTransactionService extends IService<PointTransaction> {
    void addPoints(String userId, Integer points, String type, String description);
    void deductPoints(String userId, Integer points, String type, String description);
    List<PointTransaction> getTransactionsByUserId(String userId);
}
