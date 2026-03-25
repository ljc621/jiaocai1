package com.jiaocai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jiaocai.entity.Order;
import java.util.List;

public interface OrderService extends IService<Order> {
    Order createOrder(Order order);
    List<Order> createBatchOrders(List<Order> orders);
    void payOrder(String orderId);
    void payBatchOrders(List<String> orderIds);
    void cancelOrder(String orderId);
    void completeOrder(String orderId);
    void returnOrder(String orderId, String trackingNumber);
    void confirmReturn(String orderId);
}
