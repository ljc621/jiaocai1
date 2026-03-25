package com.jiaocai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jiaocai.entity.VerificationRequest;

public interface VerificationRequestService extends IService<VerificationRequest> {
    VerificationRequest getPendingRequest(String userId);
    void apply(String userId, String realName, String studentId, String materialUrl);
    void approve(String id);
    void reject(String id);
}
