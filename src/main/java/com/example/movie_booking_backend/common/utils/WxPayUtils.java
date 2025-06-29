package com.example.movie_booking_backend.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.example.movie_booking_backend.service.IOrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 微信支付工具类
 */
@Component
public class WxPayUtils {
    @Autowired
    private IOrdersService ordersService;
    
    // 微信支付沙箱环境配置
    private final String APP_ID = "wx_sandbox_appid"; // 微信沙箱测试APPID
    private final String MCH_ID = "wx_sandbox_mchid"; // 微信沙箱商户号
    private final String API_KEY = "wx_sandbox_apikey"; // 微信沙箱API密钥
    
    // 微信支付接口URL（沙箱环境）
    private final String PAY_URL = "https://api.mch.weixin.qq.com/sandboxnew/pay/unifiedorder";
    private final String QUERY_URL = "https://api.mch.weixin.qq.com/sandboxnew/pay/orderquery";
    
    // 支付结果通知URL
    private final String NOTIFY_URL = "http://localhost:8888/api/wxpay/notify";
    // 支付成功跳转URL
    private final String RETURN_URL = "http://localhost:8888/api/wxpay/toSuccess";
    
    /**
     * 发送微信支付请求
     * @param outTradeNo 商户订单号
     * @param totalAmount 支付金额（元）
     * @param subject 商品描述
     * @return 支付链接或二维码内容
     */
    public String sendRequestToWxPay(String outTradeNo, Float totalAmount, String subject) {
        try {
            // 构建微信支付请求参数
            Map<String, String> requestParams = new HashMap<>();
            requestParams.put("appid", APP_ID);
            requestParams.put("mch_id", MCH_ID);
            requestParams.put("nonce_str", UUID.randomUUID().toString().replaceAll("-", ""));
            requestParams.put("body", subject);
            requestParams.put("out_trade_no", outTradeNo);
            // 微信支付金额单位为分，需要将元转换为分
            int amountInCents = (int) (totalAmount * 100);
            requestParams.put("total_fee", String.valueOf(amountInCents));
            requestParams.put("spbill_create_ip", "127.0.0.1");
            requestParams.put("notify_url", NOTIFY_URL);
            requestParams.put("trade_type", "NATIVE"); // NATIVE表示扫码支付
            
            // 生成签名
            String sign = generateSign(requestParams);
            requestParams.put("sign", sign);
            
            // 将参数转换为XML格式
            String xmlParams = mapToXml(requestParams);
            
            // 发送请求到微信支付接口（沙箱环境模拟）
            // 实际项目中应该使用HttpClient发送请求
            // 这里为了简化，直接返回模拟的支付链接
            String mockPayUrl = "weixin://wxpay/bizpayurl?pr=" + UUID.randomUUID().toString().substring(0, 8);
            
            // 记录请求日志
            System.out.println("微信支付请求参数: " + xmlParams);
            System.out.println("模拟微信支付链接: " + mockPayUrl);
            
            // 返回支付链接（实际项目中应解析微信返回的XML响应）
            return mockPayUrl;
            
        } catch (Exception e) {
            e.printStackTrace();
            return "支付请求发送失败: " + e.getMessage();
        }
    }
    
    /**
     * 查询订单支付状态
     * @param outTradeNo 商户订单号
     * @return 支付状态信息
     */
    public String queryOrderStatus(String outTradeNo) {
        try {
            // 构建查询请求参数
            Map<String, String> requestParams = new HashMap<>();
            requestParams.put("appid", APP_ID);
            requestParams.put("mch_id", MCH_ID);
            requestParams.put("out_trade_no", outTradeNo);
            requestParams.put("nonce_str", UUID.randomUUID().toString().replaceAll("-", ""));
            
            // 生成签名
            String sign = generateSign(requestParams);
            requestParams.put("sign", sign);
            
            // 将参数转换为XML格式
            String xmlParams = mapToXml(requestParams);
            
            // 发送请求到微信支付查询接口（沙箱环境模拟）
            // 实际项目中应该使用HttpClient发送请求
            // 这里为了简化，直接返回模拟的查询结果
            JSONObject mockResponse = new JSONObject();
            mockResponse.put("return_code", "SUCCESS");
            mockResponse.put("result_code", "SUCCESS");
            mockResponse.put("trade_state", "SUCCESS"); // 支付状态
            mockResponse.put("out_trade_no", outTradeNo);
            mockResponse.put("transaction_id", "wx" + System.currentTimeMillis());
            
            // 记录查询日志
            System.out.println("微信支付查询参数: " + xmlParams);
            System.out.println("模拟微信支付查询结果: " + mockResponse.toJSONString());
            
            return mockResponse.toJSONString();
            
        } catch (Exception e) {
            e.printStackTrace();
            return "查询失败: " + e.getMessage();
        }
    }
    
    /**
     * 生成微信支付签名
     * @param params 请求参数
     * @return 签名字符串
     */
    private String generateSign(Map<String, String> params) {
        // 实际项目中应按微信支付签名规则生成签名
        // 这里为了简化，返回一个模拟的签名
        return "mock_sign_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * 将Map转换为XML格式字符串
     * @param params 参数Map
     * @return XML格式字符串
     */
    private String mapToXml(Map<String, String> params) {
        StringBuilder xml = new StringBuilder();
        xml.append("<xml>");
        
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            xml.append("<").append(key).append(">");
            xml.append(value);
            xml.append("</").append(key).append(">");
        }
        
        xml.append("</xml>");
        return xml.toString();
    }
}