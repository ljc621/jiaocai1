package com.jiaocai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiaocai.common.Result;
import com.jiaocai.entity.User;
import com.jiaocai.entity.WalletTransaction;
import com.jiaocai.service.UserService;
import com.jiaocai.service.WalletTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
@CrossOrigin
public class WalletController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private WalletTransactionService walletTransactionService;

    @GetMapping("/balance/{userId}")
    public Result<Double> getBalance(@PathVariable String userId) {
        User user = userService.getById(userId);
        if (user == null) return Result.error("User not found");
        return Result.success(user.getBalance() == null ? 0.0 : user.getBalance());
    }

    @GetMapping("/transactions/{userId}")
    public Result<List<WalletTransaction>> listTransactions(@PathVariable String userId) {
        LambdaQueryWrapper<WalletTransaction> query = new LambdaQueryWrapper<>();
        query.eq(WalletTransaction::getUserId, userId);
        query.orderByDesc(WalletTransaction::getCreatedAt);
        return Result.success(walletTransactionService.list(query));
    }

    @PostMapping("/recharge")
    public Result<String> recharge(@RequestBody Map<String, Object> payload) {
        String userId = (String) payload.get("userId");
        Object amountObj = payload.get("amount");
        Double amount = amountObj instanceof Integer ? ((Integer) amountObj).doubleValue() : Double.valueOf(amountObj.toString());
        
        try {
            walletTransactionService.recharge(userId, amount);
            return Result.success("Recharge successful");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/withdraw")
    public Result<String> withdraw(@RequestBody Map<String, Object> payload) {
        String userId = (String) payload.get("userId");
        Object amountObj = payload.get("amount");
        Double amount = amountObj instanceof Integer ? ((Integer) amountObj).doubleValue() : Double.valueOf(amountObj.toString());

        try {
            walletTransactionService.withdraw(userId, amount);
            return Result.success("Withdraw successful");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/exchange")
    public Result<String> exchange(@RequestBody Map<String, Object> payload) {
        String userId = (String) payload.get("userId");
        Object pointsObj = payload.get("points");
        Integer points = pointsObj instanceof Integer ? (Integer) pointsObj : Integer.valueOf(pointsObj.toString());

        try {
            walletTransactionService.exchange(userId, points);
            return Result.success("Exchange successful");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/transactions/{id}")
    public Result<String> deleteTransaction(@PathVariable String id) {
        try {
            boolean removed = walletTransactionService.removeById(id);
            if (removed) {
                return Result.success("Transaction deleted successfully");
            } else {
                return Result.error("Transaction not found");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
