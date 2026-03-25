package com.jiaocai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiaocai.entity.Message;
import com.jiaocai.entity.User;
import com.jiaocai.mapper.MessageMapper;
import com.jiaocai.service.MessageService;
import com.jiaocai.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Autowired
    private UserService userService;

    @Override
    public List<Map<String, Object>> getConversations(String userId) {
        // 1. Get all messages involving this user
        LambdaQueryWrapper<Message> query = new LambdaQueryWrapper<>();
        query.eq(Message::getSenderId, userId).or().eq(Message::getReceiverId, userId);
        query.orderByDesc(Message::getCreatedAt);
        List<Message> messages = this.list(query);

        // 2. Group by other user ID and keep the first one (latest)
        Map<String, Message> latestMessages = new HashMap<>();
        Map<String, Long> unreadCounts = new HashMap<>();

        for (Message msg : messages) {
            String otherId = msg.getSenderId().equals(userId) ? msg.getReceiverId() : msg.getSenderId();
            
            if (!latestMessages.containsKey(otherId)) {
                latestMessages.put(otherId, msg);
            }

            // Calculate unread count: message sent by otherId to current userId and is not read
            if (msg.getSenderId().equals(otherId) && msg.getReceiverId().equals(userId) && !msg.getIsRead()) {
                unreadCounts.put(otherId, unreadCounts.getOrDefault(otherId, 0L) + 1);
            }
        }

        // 3. Build result list with User info
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Message> entry : latestMessages.entrySet()) {
            String otherUserId = entry.getKey();
            Message lastMsg = entry.getValue();
            
            User otherUser = userService.getById(otherUserId);
            if (otherUser != null) {
                Map<String, Object> conversation = new HashMap<>();
                conversation.put("userId", otherUserId);
                conversation.put("userName", otherUser.getName());
                conversation.put("userAvatar", otherUser.getAvatar());
                conversation.put("lastMessage", lastMsg.getContent());
                conversation.put("lastTime", lastMsg.getCreatedAt());
                conversation.put("unreadCount", unreadCounts.getOrDefault(otherUserId, 0L));
                result.add(conversation);
            }
        }
        
        // Sort by last message time
        result.sort((a, b) -> ((LocalDateTime)b.get("lastTime")).compareTo((LocalDateTime)a.get("lastTime")));
        
        return result;
    }

    @Override
    public List<Message> getMessages(String userId1, String userId2) {
        LambdaQueryWrapper<Message> query = new LambdaQueryWrapper<>();
        query.and(wrapper -> wrapper
                .eq(Message::getSenderId, userId1).eq(Message::getReceiverId, userId2)
                .or()
                .eq(Message::getSenderId, userId2).eq(Message::getReceiverId, userId1)
        );
        query.orderByAsc(Message::getCreatedAt);
        return this.list(query);
    }

    @Override
    public Message sendMessage(Message message) {
        message.setCreatedAt(LocalDateTime.now());
        message.setIsRead(false);
        this.save(message);
        return message;
    }

    @Override
    public void markAsRead(String userId, String otherUserId) {
        LambdaUpdateWrapper<Message> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Message::getSenderId, otherUserId)
                     .eq(Message::getReceiverId, userId)
                     .set(Message::getIsRead, true);
        this.update(updateWrapper);
    }

    @Override
    public long getUnreadCount(String userId) {
        LambdaQueryWrapper<Message> query = new LambdaQueryWrapper<>();
        query.eq(Message::getReceiverId, userId)
             .eq(Message::getIsRead, false);
        return this.count(query);
    }
}
