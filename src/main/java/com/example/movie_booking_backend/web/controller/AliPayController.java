package com.example.movie_booking_backend.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.movie_booking_backend.common.JsonResponse;
import com.example.movie_booking_backend.common.utils.JwtUtils;
import com.example.movie_booking_backend.common.utils.PayUtils;
import com.example.movie_booking_backend.model.domain.Orders;
import com.example.movie_booking_backend.model.domain.Payments;
import com.example.movie_booking_backend.service.IOrdersService;
import com.example.movie_booking_backend.service.IPaymentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Controller
@RequestMapping("/api/alipay")
public class AliPayController {
    @Autowired
    private PayUtils payUtils;
    
    @Autowired
    private IOrdersService ordersService;
    
    @Autowired
    private IPaymentsService paymentsService;
    
    @Autowired
    private JwtUtils jwtUtils;

    private Orders new_orders = null;
    private String tokens = "";

    @ResponseBody
    @PostMapping("/pay")
    public JsonResponse<String> alipay(@RequestHeader String token, @RequestBody Orders orders) {
        try {
            new_orders = orders;
            tokens = token;
            // 获取订单信息
            Orders order = ordersService.getOrders(orders.getId());
            if (order == null) {
                return JsonResponse.failure("订单不存在");
            }
            
            // 检查订单状态
            if (!"PENDING".equals(order.getStatus())) {
                return JsonResponse.failure("订单状态不正确，无法支付");
            }
            
            // 生成订单号
            String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String user = UUID.randomUUID().toString().replace("-", "").toUpperCase();
            String orderNum = time + user;
            
            // 获取订单金额
            float orderAmount = order.getTotalAmount().floatValue();
            
            // 调用支付宝接口
            String payHtml = payUtils.sendRequestToAlipay(orderNum, orderAmount, orders.getSeatNumbers());
            return JsonResponse.success(payHtml, "支付请求已发送");
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResponse.failure("发起支付失败: " + e.getMessage());
        }
    }

    // 支付成功回调
    @GetMapping("/toSuccess")
    public String returns(String out_trade_no) {
        try {
            // 查询支付状态
            String queryResult = payUtils.query(out_trade_no);
            JSONObject jsonObject = JSONObject.parseObject(queryResult);
            JSONObject responseData = jsonObject.getJSONObject("alipay_trade_query_response");
            
            // 检查支付状态
            if (responseData != null && "TRADE_SUCCESS".equals(responseData.getString("trade_status"))) {
                // 支付成功，更新订单状态
                if (new_orders != null) {
                    // 获取用户ID
                    Long userId = jwtUtils.getUserIdFromToken(tokens);
                    new_orders.setUserId(userId);
                    new_orders.setStatus("PAID"); // 设置订单状态为已支付
                    new_orders.setPaymentMethod("ALIPAY"); // 设置支付方式为支付宝
                    new_orders.setPaymentTime(LocalDateTime.now()); // 设置支付时间
                    
                    // 更新订单
                    boolean updateResult = ordersService.updateOrder(new_orders);
                    
                    // 创建支付记录
                    if (updateResult) {
                        Payments payment = new Payments();
                        payment.setOrderId(new_orders.getId());
                        payment.setUserId(userId);
                        payment.setPaymentMethod("ALIPAY");
                        payment.setPaymentAmount(new_orders.getTotalAmount());
                        payment.setTransactionId(responseData.getString("trade_no"));
                        payment.setPaymentStatus("PAID");
                        payment.setPaymentTime(LocalDateTime.now());
                        payment.setGatewayResponse(queryResult);
                        payment.setGatewayCode(responseData.getString("code"));
                        payment.setGatewayMessage(responseData.getString("msg"));
                        payment.setCreatedAt(LocalDateTime.now());
                        payment.setUpdatedAt(LocalDateTime.now());
                        
                        // 保存支付记录
                        paymentsService.save(payment);
                    }
                    
                    // 清空当前订单和令牌
                    Long orderId = new_orders.getId();
                    new_orders = null;
                    tokens = "";
                    
                    // 重定向到前端支付成功页面，并传递订单ID
                    return "redirect:http://localhost:5173/#/payment-success/" + orderId;
                }
                
                // 重定向到前端支付成功页面
                return "redirect:http://localhost:5173/#/payment-success";
            } else {
                // 支付失败
                return "redirect:http://localhost:5173/#/order/" + new_orders.getId();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:http://localhost:5173/#/Home";
        }
    }
}