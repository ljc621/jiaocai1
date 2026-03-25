package com.jiaocai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiaocai.common.Result;
import com.jiaocai.entity.VerificationRequest;
import com.jiaocai.service.VerificationRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/verification")
@CrossOrigin
public class VerificationRequestController {

    @Autowired
    private VerificationRequestService verificationRequestService;

    @PostMapping("/apply")
    public Result<String> apply(@RequestBody Map<String, String> payload) {
        String userId = payload.get("userId");
        String realName = payload.get("realName");
        String studentId = payload.get("studentId");
        String materialUrl = payload.get("materialUrl");
        
        verificationRequestService.apply(userId, realName, studentId, materialUrl);
        return Result.success("Application submitted");
    }

    @GetMapping("/pending")
    public Result<List<VerificationRequest>> getPendingRequests() {
        return Result.success(verificationRequestService.list(
                new LambdaQueryWrapper<VerificationRequest>()
                        .eq(VerificationRequest::getStatus, "pending")
                        .orderByAsc(VerificationRequest::getCreatedAt)
        ));
    }
    
    @GetMapping("/status")
    public Result<VerificationRequest> getStatus(@RequestParam String userId) {
        // Get the latest request
        return Result.success(verificationRequestService.getOne(
                new LambdaQueryWrapper<VerificationRequest>()
                        .eq(VerificationRequest::getUserId, userId)
                        .orderByDesc(VerificationRequest::getCreatedAt)
                        .last("LIMIT 1")
        ));
    }

    @PostMapping("/{id}/approve")
    public Result<String> approve(@PathVariable String id) {
        verificationRequestService.approve(id);
        return Result.success("Request approved");
    }

    @PostMapping("/{id}/reject")
    public Result<String> reject(@PathVariable String id) {
        verificationRequestService.reject(id);
        return Result.success("Request rejected");
    }
}
