package com.jiaocai.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiaocai.entity.Order;
import com.jiaocai.entity.Textbook;
import com.jiaocai.service.OrderService;
import com.jiaocai.service.TextbookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderTimeoutTask {

    @Autowired
    private OrderService orderService;

    @Autowired
    private TextbookService textbookService;

    // Run every minute, with initial delay to allow database initialization
    @Scheduled(initialDelay = 10000, fixedRate = 60000)
    public void checkOrderTimeout() {
        // Timeout threshold: 15 minutes
        LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(15);

        List<Order> pendingOrders = orderService.list(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, "pending")
                .lt(Order::getCreatedAt, timeoutTime));

        for (Order order : pendingOrders) {
            try {
                orderService.cancelOrder(order.getId());
            } catch (Exception e) {
                // Log error but continue with other orders
                System.err.println("Failed to cancel expired order " + order.getId() + ": " + e.getMessage());
            }
        }
    }
}
