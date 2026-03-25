package com.jiaocai.controller;

import com.jiaocai.common.Result;
import com.jiaocai.entity.PointTransaction;
import com.jiaocai.service.PointTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/points")
@CrossOrigin
public class PointController {

    @Autowired
    private PointTransactionService pointTransactionService;

    @GetMapping("/transactions/{userId}")
    public Result<List<PointTransaction>> listTransactions(@PathVariable String userId) {
        return Result.success(pointTransactionService.getTransactionsByUserId(userId));
    }

    @DeleteMapping("/transactions/{id}")
    public Result<String> deleteTransaction(@PathVariable String id) {
        pointTransactionService.removeById(id);
        return Result.success("Deleted successfully");
    }
}
