package com.jiaocai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiaocai.entity.User;
import com.jiaocai.entity.VerificationRequest;
import com.jiaocai.mapper.VerificationRequestMapper;
import com.jiaocai.service.UserService;
import com.jiaocai.service.VerificationRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class VerificationRequestServiceImpl extends ServiceImpl<VerificationRequestMapper, VerificationRequest> implements VerificationRequestService {

    @Autowired
    private UserService userService;

    @Override
    public VerificationRequest getPendingRequest(String userId) {
        return this.getOne(new LambdaQueryWrapper<VerificationRequest>()
                .eq(VerificationRequest::getUserId, userId)
                .eq(VerificationRequest::getStatus, "pending")
                .last("LIMIT 1"));
    }

    @Override
    @Transactional
    public void apply(String userId, String realName, String studentId, String materialUrl) {
        // Check if user is already verified
        User user = userService.getById(userId);
        if (user == null) throw new RuntimeException("User not found");
        if (Boolean.TRUE.equals(user.getVerified())) throw new RuntimeException("User already verified");

        // Check if there is already a pending request
        VerificationRequest existing = getPendingRequest(userId);
        if (existing != null) throw new RuntimeException("Verification request already pending");

        VerificationRequest request = new VerificationRequest();
        request.setUserId(userId);
        request.setRealName(realName);
        request.setStudentId(studentId);
        request.setMaterialUrl(materialUrl);
        request.setStatus("pending");
        request.setCreatedAt(LocalDateTime.now());
        this.save(request);
    }

    @Override
    @Transactional
    public void approve(String id) {
        VerificationRequest request = this.getById(id);
        if (request == null) throw new RuntimeException("Request not found");
        if (!"pending".equals(request.getStatus())) throw new RuntimeException("Request is not pending");

        request.setStatus("approved");
        request.setUpdatedAt(LocalDateTime.now());
        this.updateById(request);

        User user = userService.getById(request.getUserId());
        if (user != null) {
            user.setVerified(true);
            user.setStudentId(request.getStudentId()); // Update student ID if needed
            userService.updateById(user);
        }
    }

    @Override
    @Transactional
    public void reject(String id) {
        VerificationRequest request = this.getById(id);
        if (request == null) throw new RuntimeException("Request not found");
        if (!"pending".equals(request.getStatus())) throw new RuntimeException("Request is not pending");

        request.setStatus("rejected");
        request.setUpdatedAt(LocalDateTime.now());
        this.updateById(request);
    }
}
