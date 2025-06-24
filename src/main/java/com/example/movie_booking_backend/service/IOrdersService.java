package com.example.movie_booking_backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.movie_booking_backend.model.domain.Orders;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.movie_booking_backend.model.dto.OrderCreationDTO;
import com.example.movie_booking_backend.model.vo.OrderVO;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author tjl
 * @since 2025-06-23
 */
public interface IOrdersService extends IService<Orders> {

    OrderVO createOrder(OrderCreationDTO orderCreationDTO, Long userId);

    OrderVO getOrderDetails(Long orderId, Long userId);

    Page<OrderVO> getUserOrders(Page<Orders> page, Long userId);

    void cancelOrder(Long orderId, Long userId);

    // 支付成功的回调处理
    void processSuccessfulPayment(Long orderId);

    // 处理超时的订单
    void handleExpiredOrders();
}
