package com.example.movie_booking_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderTimeoutScheduler {

    @Autowired
    private IOrdersService ordersService;

    // 每分钟执行一次，处理超时未支付订单
    @Scheduled(cron = "0 * * * * ?")
    public void handleExpiredOrdersTask() {
        ordersService.handleExpiredOrders();
    }
}