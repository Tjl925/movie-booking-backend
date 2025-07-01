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
import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.RequestBody;

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
    
    // 使用线程安全的Map存储订单号与订单ID的映射关系
    private static final Map<String, Long> orderNumberToIdMap = new HashMap<>();
    // 使用线程安全的Map存储订单号与用户Token的映射关系
    private static final Map<String, String> orderNumberToTokenMap = new HashMap<>();

    @ResponseBody
    @PostMapping("/pay")
    public JsonResponse<String> alipay(@RequestHeader String token, @RequestBody Orders orders) {
        try {
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
            
            // 存储订单号与订单ID、Token的映射关系
            synchronized (orderNumberToIdMap) {
                orderNumberToIdMap.put(orderNum, orders.getId());
                orderNumberToTokenMap.put(orderNum, token);
            }
            
            // 更新订单号到数据库
            order.setOrderNumber(orderNum);
            ordersService.updateById(order);
            
            // 调用支付宝接口
            String payHtml = payUtils.sendRequestToAlipay(orderNum, orderAmount, orders.getSeatNumbers());
            return JsonResponse.success(payHtml, "支付请求已发送");
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResponse.failure("发起支付失败: " + e.getMessage());
        }
    }

    // 支付成功回调
    @GetMapping("/returns")
    public String returns(String out_trade_no, String trade_no, String trade_status) {
        try {
            // 查询支付状态
            String queryResult = payUtils.query(out_trade_no);
            JSONObject jsonObject = JSONObject.parseObject(queryResult);
            JSONObject responseData = jsonObject.getJSONObject("alipay_trade_query_response");
            
            // 检查支付状态
            if (responseData != null && "TRADE_SUCCESS".equals(responseData.getString("trade_status"))) {
                // 通过订单号查询订单信息
                Orders orders = ordersService.getOrderByOrderNumber(out_trade_no);
                
                if (orders != null && "PENDING".equals(orders.getStatus())) {
                    // 设置订单状态为已支付
                    orders.setStatus("PAID");
                    orders.setPaymentMethod("ALIPAY");
                    orders.setPaymentTime(LocalDateTime.now());
                    
                    // 更新订单
                    boolean updateResult = ordersService.updateOrder(orders);
                    
                    // 创建支付记录
                    if (updateResult) {
                        Payments payment = new Payments();
                        payment.setOrderId(orders.getId());
                        payment.setUserId(orders.getUserId());
                        payment.setPaymentMethod("ALIPAY");
                        payment.setPaymentAmount(orders.getTotalAmount());
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
                    
                    // 从映射中移除订单号
                    synchronized (orderNumberToIdMap) {
                        orderNumberToIdMap.remove(out_trade_no);
                        orderNumberToTokenMap.remove(out_trade_no);
                    }
                    
                    // 重定向到前端支付成功页面，并传递订单ID
                    return "redirect:http://localhost:5173/payment-success/" + orders.getId();
                }
                
                // 支付成功但更新订单失败，重定向到订单详情页
                return "redirect:http://localhost:5173/Home";
            } else {
                // 支付失败，重定向到订单详情页
                return "redirect:http://localhost:5173/Home";
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 记录详细的错误信息到日志
            System.err.println("支付回调处理异常: " + e.getMessage());
            return "redirect:http://localhost:5173/Home";
        }
    }
    
    @GetMapping("/toSuccess")
    public String toSuccess(String out_trade_no, String trade_no, String trade_status) {
        return returns(out_trade_no, trade_no, trade_status);
    }
    
    /**
     * 退款接口
     * @param requestBody 包含orderId和refundReason的请求体
     * @return 退款结果
     */
    @ResponseBody
    @PostMapping("/refund")
    public JsonResponse<String> refund(@RequestBody Map<String, Object> requestBody) {
        Long orderId = Long.valueOf(requestBody.get("orderId").toString());
        String refundReason = requestBody.get("refundReason") != null ? requestBody.get("refundReason").toString() : null;
        try {
            // 获取订单信息
            Orders order = ordersService.getOrders(orderId);
            if (order == null) {
                return JsonResponse.failure("订单不存在");
            }
            
            // 检查订单状态，只有已支付的订单才能退款
            if (!"PAID".equals(order.getStatus())) {
                return JsonResponse.failure("订单状态不正确，无法退款");
            }
            
            // 获取订单号
            String orderNumber = order.getOrderNumber();
            if (orderNumber == null || orderNumber.isEmpty()) {
                return JsonResponse.failure("订单号不存在，无法退款");
            }
            
            // 获取订单金额
            BigDecimal orderAmount = order.getTotalAmount();
            if (orderAmount == null) {
                return JsonResponse.failure("订单金额不存在，无法退款");
            }
            
            // 调用支付宝退款接口
            String refundResult = payUtils.refund(orderNumber, null, orderAmount.toString(), refundReason);
            
            // 解析退款结果
            JSONObject jsonObject = JSONObject.parseObject(refundResult);
            JSONObject responseData = jsonObject.getJSONObject("alipay_trade_refund_response");
            
            if (responseData != null && "10000".equals(responseData.getString("code"))) {
                // 退款成功，更新订单状态
                order.setStatus("REFUNDED");
                order.setUpdatedAt(LocalDateTime.now());
                ordersService.updateById(order);
                
                // 创建退款记录
                Payments payment = new Payments();
                payment.setOrderId(order.getId());
                payment.setUserId(order.getUserId());
                payment.setPaymentMethod("ALIPAY");
                payment.setPaymentAmount(orderAmount.negate()); // 使用负数表示退款
                payment.setTransactionId(responseData.getString("trade_no"));
                payment.setPaymentStatus("REFUNDED");
                payment.setPaymentTime(LocalDateTime.now());
                payment.setGatewayResponse(refundResult);
                payment.setGatewayCode(responseData.getString("code"));
                payment.setGatewayMessage(responseData.getString("msg"));
                payment.setCreatedAt(LocalDateTime.now());
                payment.setUpdatedAt(LocalDateTime.now());
                
                // 保存退款记录
                paymentsService.save(payment);
                
                return JsonResponse.success("退款成功");
            } else {
                // 退款失败
                String errorMsg = responseData != null ? responseData.getString("sub_msg") : "未知错误";
                return JsonResponse.failure("退款失败: " + errorMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResponse.failure("退款处理异常: " + e.getMessage());
        }
    }
}