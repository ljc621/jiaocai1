package com.jiaocai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("verification_request")
public class VerificationRequest {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String userId;
    private String realName;
    private String studentId;
    private String materialUrl; // Image URL
    private String status; // pending, approved, rejected
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
