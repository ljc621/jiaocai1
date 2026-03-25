package com.jiaocai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiaocai.common.Result;
import com.jiaocai.entity.Order;
import com.jiaocai.service.OrderService;
import com.jiaocai.service.TextbookService;
import com.jiaocai.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin
public class StatsController {

    @Autowired
    private UserService userService;
    @Autowired
    private TextbookService textbookService;
    @Autowired
    private OrderService orderService;

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userService.count());
        stats.put("activeTextbooks", textbookService.count());
        stats.put("totalOrders", orderService.count());
        
        // Calculate total volume
        List<Order> orders = orderService.list();
        double totalVolume = orders.stream()
            .filter(o -> o.getAmount() != null)
            .mapToDouble(Order::getAmount)
            .sum();
            
        stats.put("totalVolume", totalVolume); 
        return Result.success(stats);
    }

    @GetMapping("/daily-sales")
    public Result<Map<String, Object>> getDailySales() {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(6).withHour(0).withMinute(0).withSecond(0);

        List<Order> orders = orderService.list(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreatedAt, startDate)
                .in(Order::getStatus, "paid", "shipped", "renting", "returning", "completed"));

        Map<String, Double> salesMap = new TreeMap<>();
        // Initialize map with 0.0 for last 7 days
        for (int i = 0; i < 7; i++) {
            salesMap.put(startDate.plusDays(i).toLocalDate().toString(), 0.0);
        }

        for (Order order : orders) {
            if (order.getCreatedAt() != null) {
                String date = order.getCreatedAt().toLocalDate().toString();
                if (salesMap.containsKey(date)) {
                    salesMap.put(date, salesMap.get(date) + (order.getAmount() != null ? order.getAmount() : 0.0));
                }
            }
        }

        List<String> dates = new ArrayList<>(salesMap.keySet());
        List<Double> values = new ArrayList<>(salesMap.values());
        
        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates);
        result.put("values", values);

        return Result.success(result);
    }
}
