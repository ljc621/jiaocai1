package com.jiaocai.controller;

import com.jiaocai.common.Result;
import com.jiaocai.entity.Message;
import com.jiaocai.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/conversations")
    public Result<List<Map<String, Object>>> getConversations(@RequestParam String userId) {
        return Result.success(messageService.getConversations(userId));
    }

    @GetMapping
    public Result<List<Message>> getMessages(@RequestParam String user1, @RequestParam String user2) {
        return Result.success(messageService.getMessages(user1, user2));
    }

    @PostMapping
    public Result<Message> sendMessage(@RequestBody Message message) {
        return Result.success(messageService.sendMessage(message));
    }

    @PostMapping("/read")
    public Result<Void> markAsRead(@RequestBody Map<String, String> params) {
        String userId = params.get("userId");
        String otherUserId = params.get("otherUserId");
        messageService.markAsRead(userId, otherUserId);
        return Result.success(null);
    }

    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(@RequestParam String userId) {
        return Result.success(messageService.getUnreadCount(userId));
    }
}
