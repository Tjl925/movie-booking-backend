package com.example.movie_booking_backend.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.movie_booking_backend.common.JsonResponse;
import com.example.movie_booking_backend.model.domain.Orders;
import com.example.movie_booking_backend.model.dto.OrderCreationDTO;
import com.example.movie_booking_backend.model.vo.OrderVO;
import com.example.movie_booking_backend.service.IOrdersService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 *
 *  前端控制器
 *
 *
 * @author tjl
 * @since 2025-06-23
 * @version v1.0
 */
@Api(tags = "订单与支付管理")
@RestController
@RequestMapping("/api/orders")
public class OrdersController {

    @Autowired
    private IOrdersService ordersService;

    @ApiOperation("用户下单（锁定座位，生成待支付订单）")
    @PostMapping
    public JsonResponse<OrderVO> createOrder(@Valid @RequestBody OrderCreationDTO orderCreationDTO, Long uid) {
        System.out.println("前端传递的座位列表:"+orderCreationDTO.getSeatIds());
        OrderVO orderVO = ordersService.createOrder(orderCreationDTO, uid);
        return JsonResponse.success(orderVO, "下单成功，请尽快支付");
    }

    @ApiOperation("用户取消订单")
    @PostMapping("/{id}/cancel")
    public JsonResponse<String> cancelOrder(@PathVariable Long id, Long uid) {
        System.out.println("取消订单请求，订单ID: " + id + ", 用户ID: " + uid);
        ordersService.cancelOrder(id, uid);
        return JsonResponse.successMessage("订单已取消");
    }

    @ApiOperation("用户查询自己的订单列表")
    @GetMapping
    public JsonResponse<Page<OrderVO>> getUserOrders(@RequestParam(defaultValue = "1") Integer current,
                                                     @RequestParam(defaultValue = "10") Integer size,
                                                     Long uid) {
        Page<OrderVO> page = ordersService.getUserOrders(new Page<>(current, size), uid);
        return JsonResponse.success(page);
    }

    @PostMapping("/detail")
    public JsonResponse<OrderVO> getOrderDetail(Long orderId, Long userId) {
        OrderVO orderVO = ordersService.getOrderDetails(orderId, userId);
        return JsonResponse.success(orderVO);
    }
    
    /**
     * 查询订单状态
     * @param id 订单ID
     * @return 订单状态信息
     */
    @ApiOperation("查询订单状态")
    @GetMapping("/{id}/status")
    public JsonResponse<Map<String, String>> checkOrderStatus(@PathVariable Long id) {
        Orders order = ordersService.getOrders(id);
        if (order == null) {
            return JsonResponse.failure("订单不存在");
        }
        
        Map<String, String> statusInfo = new HashMap<>();
        statusInfo.put("status", order.getStatus());
        return JsonResponse.success(statusInfo);
    }

}

