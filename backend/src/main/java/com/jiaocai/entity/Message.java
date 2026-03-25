package com.jiaocai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("message")
public class Message {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String senderId;
    private String receiverId;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
