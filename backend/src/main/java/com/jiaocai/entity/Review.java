package com.jiaocai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("review")
public class Review {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String userId;
    private String sellerId;
    private String textbookId;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private String userName; // For display
    @TableField(exist = false)
    private String userAvatar; // For display
}
