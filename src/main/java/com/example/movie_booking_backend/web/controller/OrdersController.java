package com.example.movie_booking_backend.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.movie_booking_backend.common.JsonResponse;
import com.example.movie_booking_backend.model.dto.OrderCreationDTO;
import com.example.movie_booking_backend.model.vo.OrderVO;
import com.example.movie_booking_backend.service.IOrdersService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public JsonResponse<OrderVO> createOrder(@Valid @RequestBody OrderCreationDTO orderCreationDTO, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        OrderVO orderVO = ordersService.createOrder(orderCreationDTO, userId);
        return JsonResponse.success(orderVO, "下单成功，请尽快支付");
    }

    @ApiOperation("用户支付订单（模拟支付成功）")
    @PostMapping("/{id}/pay")
    public JsonResponse<String> payOrder(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        OrderVO order = ordersService.getOrderDetails(id, userId);
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            return JsonResponse.failure("订单状态异常，无法支付");
        }
        ordersService.processSuccessfulPayment(id);
        return JsonResponse.successMessage("支付成功");
    }

    @ApiOperation("用户取消订单")
    @PostMapping("/{id}/cancel")
    public JsonResponse<String> cancelOrder(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        ordersService.cancelOrder(id, userId);
        return JsonResponse.successMessage("订单已取消");
    }

    @ApiOperation("用户查询订单详情")
    @GetMapping("/{id}")
    public JsonResponse<OrderVO> getOrderDetails(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        OrderVO orderVO = ordersService.getOrderDetails(id, userId);
        return JsonResponse.success(orderVO);
    }

    @ApiOperation("用户查询自己的订单列表")
    @GetMapping
    public JsonResponse<Page<OrderVO>> getUserOrders(@RequestParam(defaultValue = "1") Integer current,
                                                     @RequestParam(defaultValue = "10") Integer size,
                                                     HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Page<OrderVO> page = ordersService.getUserOrders(new Page<>(current, size), userId);
        return JsonResponse.success(page);
    }
}

