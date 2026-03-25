package com.jiaocai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiaocai.common.Result;
import com.jiaocai.entity.Order;
import com.jiaocai.entity.Review;
import com.jiaocai.entity.User;
import com.jiaocai.service.OrderService;
import com.jiaocai.service.ReviewService;
import com.jiaocai.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    @GetMapping
    public Result<List<Order>> list(
            @RequestParam(required = false) String buyerId,
            @RequestParam(required = false) String sellerId) {
        LambdaQueryWrapper<Order> query = new LambdaQueryWrapper<>();
        if (buyerId != null) query.eq(Order::getBuyerId, buyerId);
        if (sellerId != null) query.eq(Order::getSellerId, sellerId);
        query.orderByDesc(Order::getCreatedAt);
        return Result.success(orderService.list(query));
    }

    @GetMapping("/{id}")
    public Result<Order> getById(@PathVariable String id) {
        return Result.success(orderService.getById(id));
    }

    @PostMapping
    public Result<Order> create(@RequestBody Order order) {
        try {
            return Result.success(orderService.createOrder(order));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/batch")
    public Result<List<Order>> createBatch(@RequestBody List<Order> orders) {
        try {
            return Result.success(orderService.createBatchOrders(orders));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/batch/pay")
    public Result<String> payBatch(@RequestBody List<String> orderIds) {
        try {
            orderService.payBatchOrders(orderIds);
            return Result.success("Batch payment successful");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/pay")
    public Result<String> pay(@PathVariable String id) {
        try {
            orderService.payOrder(id);
            return Result.success("Payment successful");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    public Result<String> cancel(@PathVariable String id) {
        try {
            orderService.cancelOrder(id);
            return Result.success("Order cancelled");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/ship")
    public Result<Order> ship(@PathVariable String id, @RequestParam String trackingNumber) {
        Order order = orderService.getById(id);
        if (order == null) return Result.error("Order not found");
        if (!"paid".equals(order.getStatus())) return Result.error("Order not paid");
        
        order.setStatus("shipped");
        order.setShippingStatus("shipped");
        order.setTrackingNumber(trackingNumber);
        order.setShippedAt(LocalDateTime.now());
        orderService.updateById(order);
        return Result.success(order);
    }

    @PostMapping("/{id}/complete")
    public Result<String> complete(@PathVariable String id) {
        try {
            orderService.completeOrder(id);
            return Result.success("Order completed");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/return")
    public Result<String> returnOrder(@PathVariable String id, @RequestParam String trackingNumber) {
        try {
            orderService.returnOrder(id, trackingNumber);
            return Result.success("Return initiated");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/confirm-return")
    public Result<String> confirmReturn(@PathVariable String id) {
        try {
            orderService.confirmReturn(id);
            return Result.success("Return confirmed");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable String id) {
        orderService.removeById(id);
        return Result.success("Order deleted");
    }

    @PostMapping("/{id}/review")
    public Result<String> review(@PathVariable String id, @RequestBody java.util.Map<String, Object> payload) {
        Order order = orderService.getById(id);
        if (order == null) return Result.error("Order not found");
        
        String content = (String) payload.get("content");
        Integer rating = (Integer) payload.get("rating");
        
        order.setReviewContent(content);
        order.setReviewRating(rating);
        orderService.updateById(order);

        // Update seller credit score based on rating
        if (rating != null) {
            User seller = userService.getById(order.getSellerId());
            if (seller != null) {
                int currentScore = seller.getCreditScore() == null ? 600 : seller.getCreditScore();
                if (rating >= 4) {
                    seller.setCreditScore(currentScore + 5);
                } else if (rating <= 2) {
                    seller.setCreditScore(currentScore - 10);
                }
                userService.updateById(seller);
            }
        }

        // Create public review
        Review review = new Review();
        review.setUserId(order.getBuyerId());
        review.setSellerId(order.getSellerId());
        review.setTextbookId(order.getTextbookId());
        review.setContent(content);
        review.setRating(rating);
        review.setCreatedAt(LocalDateTime.now());
        reviewService.save(review);
        
        return Result.success("Review submitted");
    }
}
