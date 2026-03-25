package com.jiaocai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiaocai.entity.Order;
import com.jiaocai.entity.Textbook;
import com.jiaocai.mapper.OrderMapper;
import com.jiaocai.service.OrderService;
import com.jiaocai.service.TextbookService;
import com.jiaocai.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import com.jiaocai.entity.User;
import com.jiaocai.service.WalletTransactionService;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Override
    public OrderMapper getBaseMapper() {
        return super.getBaseMapper();
    }

    @Autowired
    private TextbookService textbookService;

    @Autowired
    private UserService userService;

    @Autowired
    private WalletTransactionService walletTransactionService;

    @Override
    @Transactional
    public Order createOrder(Order order) {
        // 1. Check textbook availability
        Textbook textbook = textbookService.getById(order.getTextbookId());
        if (textbook == null || !"available".equals(textbook.getStatus())) {
            throw new RuntimeException("Textbook not available or sold out");
        }

        // Check if buyer is the seller
        if (textbook.getSellerId().equals(order.getBuyerId())) {
            throw new RuntimeException("Cannot buy your own textbook");
        }

        // Validate rental duration and amount
        if ("rent".equals(order.getType())) {
            if (order.getRentDuration() == null || order.getRentDuration() < 1) {
                throw new RuntimeException("Rental duration must be at least 1 day");
            }
            if (textbook.getDailyRent() != null) {
                double expectedAmount = textbook.getDailyRent() * order.getRentDuration();
                if (Math.abs(order.getAmount() - expectedAmount) > 0.01) {
                     // Auto-correct or throw error? Let's throw error to be safe or just correct it. 
                     // Let's correct it to trust the server calculation.
                     order.setAmount(expectedAmount);
                }
            }
        }

        // 2. Create order
        order.setStatus("pending");
        order.setShippingStatus("pending");
        order.setCreatedAt(LocalDateTime.now());
        this.save(order);

        // 3. Update textbook status
        if ("rent".equals(order.getType())) {
            textbook.setStatus("rented");
        } else {
            textbook.setStatus("sold");
        }
        textbookService.updateById(textbook);

        return order;
    }

    @Override
    @Transactional
    public List<Order> createBatchOrders(List<Order> orders) {
        List<Order> createdOrders = new java.util.ArrayList<>();
        for (Order order : orders) {
            createdOrders.add(this.createOrder(order));
        }
        return createdOrders;
    }

    @Override
    @Transactional
    public void payBatchOrders(List<String> orderIds) {
        for (String orderId : orderIds) {
            payOrder(orderId);
        }
    }

    @Override
    @Transactional
    public void cancelOrder(String orderId) {
        Order order = this.getById(orderId);
        if (order == null) throw new RuntimeException("Order not found");
        if (!"pending".equals(order.getStatus())) throw new RuntimeException("Can only cancel pending orders");

        order.setStatus("cancelled");
        this.updateById(order);

        // Revert textbook status
        Textbook textbook = textbookService.getById(order.getTextbookId());
        if (textbook != null) {
            textbook.setStatus("available");
            textbookService.updateById(textbook);
        }
    }

    @Override
    @Transactional
    public void payOrder(String orderId) {
        Order order = this.getById(orderId);
        if (order == null) throw new RuntimeException("Order not found");
        if (!"pending".equals(order.getStatus())) throw new RuntimeException("Order not pending");

        walletTransactionService.pay(order.getBuyerId(), order.getAmount(), order.getId());

        order.setStatus("paid");
        order.setPaidAt(LocalDateTime.now());
        this.updateById(order);
    }

    @Override
    @Transactional
    public void completeOrder(String orderId) {
        Order order = this.getById(orderId);
        if (order == null) throw new RuntimeException("Order not found");
        if (!"shipped".equals(order.getStatus())) throw new RuntimeException("Order not shipped");

        // Release funds to seller
        walletTransactionService.income(order.getSellerId(), order.getAmount(), order.getId());

        // Award points to buyer (1 point per 1 RMB), but exclude "donate" type textbooks
        Textbook textbook = textbookService.getById(order.getTextbookId());
        boolean isDonate = textbook != null && "donate".equals(textbook.getType());
        
        if (!isDonate && order.getAmount() != null && order.getAmount() > 0) {
            walletTransactionService.rewardPoints(order.getBuyerId(), order.getAmount().intValue(), "购买教材奖励积分");
        }

        // Update Credit Score
        // Seller +10 for successful transaction
        User seller = userService.getById(order.getSellerId());
        if (seller != null) {
            int currentScore = seller.getCreditScore() == null ? 600 : seller.getCreditScore();
            seller.setCreditScore(currentScore + 10);
            userService.updateById(seller);
        }
        
        // Buyer +5 for successful transaction
        User buyer = userService.getById(order.getBuyerId());
        if (buyer != null) {
            int currentScore = buyer.getCreditScore() == null ? 600 : buyer.getCreditScore();
            buyer.setCreditScore(currentScore + 5);
            userService.updateById(buyer);
        }

        // For rent orders, status becomes 'renting' instead of 'completed'
        if ("rent".equals(order.getType())) {
            order.setStatus("renting");
            // Ideally we should record rentStartedAt, but we can use updatedAt or add a field
        } else {
            order.setStatus("completed");
            order.setCompletedAt(LocalDateTime.now());
        }
        
        this.updateById(order);

        // If it was a rental order, we don't make it available yet. 
        // It becomes available only after return.
        // Logic moved to confirmReturn
    }

    @Override
    @Transactional
    public void returnOrder(String orderId, String trackingNumber) {
        Order order = this.getById(orderId);
        if (order == null) throw new RuntimeException("Order not found");
        if (!"renting".equals(order.getStatus())) throw new RuntimeException("Order is not in renting status");
        
        order.setStatus("returning");
        order.setTrackingNumber(trackingNumber); // Reusing tracking number for return, or we need a returnTrackingNumber field
        // For simplicity, we can append or just overwrite if the original one isn't needed anymore
        // Or better, let's assume trackingNumber is for the current shipping leg.
        
        this.updateById(order);
    }

    @Override
    @Transactional
    public void confirmReturn(String orderId) {
        Order order = this.getById(orderId);
        if (order == null) throw new RuntimeException("Order not found");
        if (!"returning".equals(order.getStatus())) throw new RuntimeException("Order is not in returning status");
        
        order.setStatus("completed");
        order.setCompletedAt(LocalDateTime.now());
        this.updateById(order);

        // Make the textbook available again
        Textbook textbook = textbookService.getById(order.getTextbookId());
        if (textbook != null) {
            textbook.setStatus("available");
            textbook.setRentEnabled(true);
            textbookService.updateById(textbook);
        }
    }
}
