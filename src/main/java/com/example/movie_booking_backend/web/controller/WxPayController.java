package com.example.movie_booking_backend.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.movie_booking_backend.common.JsonResponse;
import com.example.movie_booking_backend.common.utils.JwtUtils;
import com.example.movie_booking_backend.common.utils.WxPayUtils;
import com.example.movie_booking_backend.model.domain.Orders;
import com.example.movie_booking_backend.model.domain.Payments;
import com.example.movie_booking_backend.service.IOrdersService;
import com.example.movie_booking_backend.service.IPaymentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * 微信支付控制器
 */
@Controller
@RequestMapping("/api/wxpay")
public class WxPayController {
    @Autowired
    private WxPayUtils wxPayUtils;
    
    @Autowired
    private IOrdersService ordersService;
    
    @Autowired
    private IPaymentsService paymentsService;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    private Orders currentOrder = null;
    private String currentToken = "";

    /**
     * 发起微信支付
     * @param token 用户令牌
     * @param orders 订单信息
     * @return 支付链接或二维码内容
     */
    @ResponseBody
    @PostMapping("/pay")
    public JsonResponse<String> wxpay(@RequestHeader String token, @RequestBody Orders orders) {
        try {
            // 保存当前订单和令牌信息
            currentOrder = orders;
            currentToken = token;
            
            // 获取订单详情
            Orders order = ordersService.getOrders(orders.getId());
            if (order == null) {
                return JsonResponse.failure("订单不存在");
            }
            
            // 检查订单状态
            if (!"PENDING".equals(order.getStatus())) {
                return JsonResponse.failure("订单状态不正确，无法支付");
            }
            
            // 生成商户订单号
            String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
            String orderNum = time + uuid;
            
            // 获取订单金额
            float orderAmount = order.getTotalAmount().floatValue();
            
            // 调用微信支付工具发送支付请求
            String payResult = wxPayUtils.sendRequestToWxPay(orderNum, orderAmount, orders.getSeatNumbers());
            
            return JsonResponse.success(payResult, "微信支付请求已发送");
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResponse.failure("发起支付失败: " + e.getMessage());
        }
    }

    /**
     * 微信支付成功回调
     * @param outTradeNo 商户订单号
     * @return 跳转页面
     */
    @GetMapping("/toSuccess")
    public String paymentSuccess(String outTradeNo) {
        try {
            // 查询支付状态
            String queryResult = wxPayUtils.queryOrderStatus(outTradeNo);
            JSONObject resultJson = JSONObject.parseObject(queryResult);
            
            // 检查支付状态
            if ("SUCCESS".equals(resultJson.getString("trade_state"))) {
                // 支付成功，更新订单状态
                if (currentOrder != null) {
                    // 获取用户ID
                    Long userId = jwtUtils.getUserIdFromToken(currentToken);
                    currentOrder.setUserId(userId);
                    currentOrder.setStatus("PAID"); // 设置订单状态为已支付
                    currentOrder.setPaymentMethod("WECHAT"); // 设置支付方式为微信
                    currentOrder.setPaymentTime(LocalDateTime.now()); // 设置支付时间
                    
                    // 更新订单
                    boolean updateResult = ordersService.updateOrder(currentOrder);
                    
                    // 创建支付记录
                    if (updateResult) {
                        Payments payment = new Payments();
                        payment.setOrderId(currentOrder.getId());
                        payment.setUserId(userId);
                        payment.setPaymentMethod("WECHAT");
                        payment.setPaymentAmount(currentOrder.getTotalAmount());
                        payment.setTransactionId(resultJson.getString("transaction_id"));
                        payment.setPaymentStatus("PAID");
                        payment.setPaymentTime(LocalDateTime.now());
                        payment.setGatewayResponse(queryResult);
                        payment.setCreatedAt(LocalDateTime.now());
                        payment.setUpdatedAt(LocalDateTime.now());
                        
                        // 保存支付记录
                        paymentsService.save(payment);
                    }
                    
                    // 保存订单ID并清空当前订单和令牌
                    Long orderId = currentOrder.getId();
                    currentOrder = null;
                    currentToken = "";
                    
                    // 重定向到前端支付成功页面，并传递订单ID
                    return "redirect:http://localhost:5173/#/payment-success/" + orderId;
                }
                
                // 重定向到前端支付成功页面
                return "redirect:http://localhost:5173/#/payment-success";
            }
            
            // 支付失败或订单不存在
            return "redirect:http://localhost:5173/#/order/" + (currentOrder != null ? currentOrder.getId() : "");
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:http://localhost:5173/#/Home";
        }
    }

    /**
     * 微信支付异步通知
     * @param notifyData 通知数据
     * @return 处理结果
     */
    @ResponseBody
    @PostMapping("/notify")
    public String paymentNotify(@RequestBody String notifyData) {
        // 实际项目中应解析XML格式的通知数据
        // 验证签名，处理支付结果
        System.out.println("收到微信支付异步通知: " + notifyData);
        
        // 返回成功接收的响应
        return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
    }
}