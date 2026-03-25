package com.jiaocai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jiaocai.entity.Message;
import com.jiaocai.entity.User;

import java.util.List;
import java.util.Map;

public interface MessageService extends IService<Message> {
    List<Map<String, Object>> getConversations(String userId);
    List<Message> getMessages(String userId1, String userId2);
    Message sendMessage(Message message);
    void markAsRead(String userId, String otherUserId);
    long getUnreadCount(String userId);
}
