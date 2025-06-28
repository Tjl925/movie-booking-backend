package com.example.movie_booking_backend.schedule;  // 注意包名匹配实际路径

import com.example.movie_booking_backend.service.IOrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderExpirationScheduler {

    @Autowired
    private IOrdersService ordersService;

    // 每5分钟执行一次
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void checkAndHandleExpiredOrders() {
        ordersService.handleExpiredOrders();
    }
}