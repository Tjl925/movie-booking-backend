package com.example.movie_booking_backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.movie_booking_backend.model.domain.Orders;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.movie_booking_backend.model.dto.OrderCreationDTO;
import com.example.movie_booking_backend.model.vo.OrderVO;
import io.swagger.annotations.ApiOperation;

import java.util.List;

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

    Orders getOrders(Long orderId);

    OrderVO getOrderDetails(Long orderId, Long userId);

    Page<OrderVO> getUserOrders(Page<Orders> page, Long userId);
    
    // 管理员获取所有订单
    Page<OrderVO> getAllOrders(Page<Orders> page);

    void cancelOrder(Long orderId, Long userId);
    
    // 管理员删除订单
    void deleteOrder(Long orderId);

    // 处理超时的订单
    void handleExpiredOrders();
    
    // 更新订单信息（支付成功后调用）
    boolean updateOrder(Orders order);
    
    // 根据订单号查询订单
    Orders getOrderByOrderNumber(String orderNumber);

    Boolean getOrderRatedStatus(Long id);

    List<OrderVO> getAllOrdersByUserId(Long userId);
}
