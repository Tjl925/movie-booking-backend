package com.example.movie_booking_backend.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.diagnosis.DiagnosisUtils;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.request.AlipayTradeFastpayRefundQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.domain.AlipayTradeFastpayRefundQueryModel;
import org.springframework.stereotype.Component;

@Component
public class PayUtils {
    //appid
    private final String APP_ID = "9021000149696881";
    //应用私钥
    private final String APP_PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCH8ZqkRQ1wazmJsSNtdhNPwhx+lXRgaz60BAYlLRTLO6U5U2jclG+jf1xYTBKPtglwugDPK1/agIYjTCX6hIUCuQBIV5yDe1ta5M281QaUzz3+znWrxUT3kirG+Ukz9MkhsviRkUI/6iH6lrVqwZox7CAzHXVuGtFcurfdx8s+kOhb858vN5aXD8bQ9ftVv5R//QEswN9tycYpYg0lA/nT2JTc+LlsUDOhTDcUzRgCqhqt+WyGV4CPNSzkK5cTx6b+D9mEhNonCHHQ3cIQbmai6mhdXlRu7ViKLjJQwUtbveXcTA5A7i1lfzypmD9u6JRPb6uQKet8QEMN2NYZQiVVAgMBAAECggEAJY0YcV3dq3sC5sT2i0r9ZJ+y7UhtYCDfriwaYnSEdOsh5abjpWKfP+NzZ7SDwAzBqNDIXnGVxa6Q3vku2Hd4olrl+BWzVyJhBseDCTEy4zIn5tWy7WLNfTFz/YhoryBj2posTVF5olwTTLwKmtWluTqHXnc5s9NWl63AElQZEUveXOg1k1Fbp252/nRM5JC5E02n0h9ozi9cGZI8CfZyb9ik62RH8asGG7+MurFrbwNRogB+moGKHAY4w/wU7xqBTqjfDTd4AI3IJNcWXO0Ril9diwy3NEQOCuYq8LCZmB/5AMKUaGxq+8Awyq3QqNkM5uFMFjwdMpD2oexZlXuIVQKBgQDnZKtVm2De5QPCVmX+VUg83qtK60/2J3q8iTcZBN6Pz/LzmvL98jcva9/cYVCj2LVczlHBa7NSOkTg63YsIDBbpBJRw9sVG4gXOSwSKP5aPxg2Ch0Edu3xSGUMDws4XTmcIgZkX1edk6BID5HcX9tfzsO/BxtsrmrWQamIPwr8UwKBgQCWZnejyqMhZ5r+gyReOaUYMSA60b/yrzFEIRM+gYto4Tirw65gczm6eqn2EXP3mebuPxtgyqC4yAQRvBZJbxvreJ4t990LAGrrgPrZ8ioyqgiyx2h67ibTLx3CVhTJ4zdNClw3cyIEf0/AwvD45AJydOc4CttXEk245L+b4vBitwKBgHkAZLRUl1iXy9pxUc6pDCiAjHSWDu96/6zd2EkmCbiMltCa45y2b/NFZwAYh5HWvih0373UsyY5wFEZ8Gbswjz51Habg9JK3mS+ifRJKZFJ00BXE4uFIbJ+GBExQL3NylWCWXNdhDSrwTJNqgniupYkSqp3bfjsUIQQPDY6OWVvAoGAfv/PjvORhKej7b53e0lZ62+wM3riht8AzvLO+T0Sp2FnJUN7f95MpPbnhe1+61l98zg+uiDgTqg3J3KXrzKvdMrJZy+h66RSpMKpEFi44UdLSQuxYfyvtCxZCLz/mOrQy7Ev5XLHCt0mTzNNoB2JI3UrudsRFBlUoAgo9E1Rh0kCgYEAwPnfhNq1CROj1L1JoMrLqwD6yEcwLWFIrEoDc7KVcxXcKYKoP0mAXz7YwmRlZScKIyvgeTW9jDBNPjc1yY5t7CBOJUoTceWL2+/IvNdYw6o5+hhFJK4k+JqjgzAkljcTNZQoNb+dxh8Lm2CJfYloifTiXlgXOstutMfbpjreUUA=";

    private final String CHARSET = "UTF-8";
    // 支付宝公钥
    private final String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqylm5rGf8K2f7xM3Z8w13t2I45gaKJlY0OD32DkBxqieKwgmPIftmsBFbRkyS4PHTJG5LM2RQxmyrrGVM3oToitrdyQnF2lPIokeYI7OewmYe7e7l5BdLmMW9JGJmCbMEvz1oSHUUQbziiUTixpP1pXQ6c8Xykhpibjh7JyoUtNsjcqylYWIFPrxUFAUpe2PdvAQd3bNByKevzBMQUEtzQa8YtgsvfEzUWxtEErFUbUh2wR49QPreTiE+XHWiRt1Rkl+Yv0QOQlNAFVZi+JAxI9alzKu+dTUIg26uQn1wg+M12mwoX9J3YeLsEBwN/u/Mh/3t6QRlBfSkxipKZTZvwIDAQAB";

    //这是沙箱接口路径,正式路径为https://openapi.alipay.com/gateway.do
    private final String GATEWAY_URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    private final String FORMAT = "JSON";
    //签名方式
    private final String SIGN_TYPE = "RSA2";

    //支付宝异步通知路径,付款完毕后会异步调用本项目的方法,必须为公网地址
    private final String NOTIFY_URL = "http://j68bb642.natappfree.cc/api/alipay/returns";
    //支付宝同步通知路径,也就是当付款完毕后跳转本项目的页面,可以不是公网地址
    private final String RETURN_URL = "http://localhost:8888/api/alipay/returns";
    private AlipayClient alipayClient = null;
    //支付宝官方提供的接口
    public String sendRequestToAlipay(String outTradeNo, Float totalAmount, String subject) throws AlipayApiException {
        //获得初始化的AlipayClient，设置连接超时和读取超时时间
        alipayClient = new DefaultAlipayClient(GATEWAY_URL, APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE, "30000", "30000");

        //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(RETURN_URL);
        alipayRequest.setNotifyUrl(NOTIFY_URL);

        //商品描述（可空）
        String body = "";
        alipayRequest.setBizContent("{\"out_trade_no\":\"" + outTradeNo + "\","
                + "\"total_amount\":\"" + totalAmount + "\","
                + "\"subject\":\"" + subject + "\","
                + "\"body\":\"" + body + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        //请求
        String result = alipayClient.pageExecute(alipayRequest).getBody();
        System.out.println("返回的结果是："+result );
        return result;
    }

    //    通过订单编号查询
    public String query(String id){
        // 确保alipayClient已初始化，设置连接超时和读取超时时间
        if (alipayClient == null) {
            alipayClient = new DefaultAlipayClient(GATEWAY_URL, APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE, "30000", "30000");
        }
        
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", id);
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = null;
        String body=null;
        try {
            response = alipayClient.execute(request);
            body = response.getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response != null && response.isSuccess()){
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
        }
        return body;
    }
    
    /**
     * 支付宝退款接口
     * @param outTradeNo 商户订单号
     * @param tradeNo 支付宝交易号
     * @param refundAmount 退款金额
     * @param refundReason 退款原因
     * @param outRequestNo 退款请求号，标识一次退款请求，同一笔交易多次退款需要保证唯一，如果部分退款，则此参数必传
     * @return 退款结果
     */
    public String refund(String outTradeNo, String tradeNo, String refundAmount, String refundReason, String outRequestNo) {
        // 确保alipayClient已初始化，设置连接超时和读取超时时间
        if (alipayClient == null) {
            alipayClient = new DefaultAlipayClient(GATEWAY_URL, APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE, "30000", "30000");
        }
        
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();

        if (outTradeNo != null && !outTradeNo.isEmpty()) {
            model.setOutTradeNo(outTradeNo);
        }

        if (tradeNo != null && !tradeNo.isEmpty()) {
            model.setTradeNo(tradeNo);
        }
        
        // 设置退款金额
        model.setRefundAmount(refundAmount);
        
        // 设置退款原因
        if (refundReason != null && !refundReason.isEmpty()) {
            model.setRefundReason(refundReason);
        }
        
        // 设置退款请求号，用于幂等性控制
        if (outRequestNo != null && !outRequestNo.isEmpty()) {
            model.setOutRequestNo(outRequestNo);
        } else {
            // 如果没有提供退款请求号，则使用订单号+时间戳作为退款请求号
            model.setOutRequestNo(outTradeNo + "_" + System.currentTimeMillis());
        }

        request.setBizModel(model);
        
        AlipayTradeRefundResponse response = null;
        String body = null;
        
        // 添加重试机制，最多重试3次
        int maxRetries = 3;
        int retryCount = 0;
        boolean success = false;
        
        while (retryCount < maxRetries && !success) {
            try {
                System.out.println("开始执行退款请求..." + (retryCount > 0 ? "(第" + retryCount + "次重试)" : ""));
                response = alipayClient.execute(request);
                body = response.getBody();
                System.out.println("退款响应：" + body);
                
                // 检查是否为系统异常，如果是则重试
                if (response != null && "20000".equals(response.getCode()) && 
                    "aop.ACQ.SYSTEM_ERROR".equals(response.getSubCode())) {
                    System.out.println("检测到系统异常(aop.ACQ.SYSTEM_ERROR)，准备重试...");
                    retryCount++;
                    // 等待一段时间再重试，避免频繁请求
                    try {
                        Thread.sleep(2000); // 等待2秒
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    // 其他情况不重试
                    success = true;
                }
            } catch (AlipayApiException e) {
                System.err.println("退款请求异常: " + e.getMessage());
                // 详细记录异常信息
                if (e.getCause() != null) {
                    System.err.println("异常原因: " + e.getCause().getMessage());
                    e.getCause().printStackTrace();
                }
                e.printStackTrace();
                
                // 对于网络超时等异常，也进行重试
                if (e.getCause() instanceof java.net.SocketTimeoutException) {
                    System.out.println("检测到网络超时，准备重试...");
                    retryCount++;
                    try {
                        Thread.sleep(2000); // 等待2秒
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    // 其他异常不重试
                    break;
                }
            } catch (Exception e) {
                System.err.println("退款过程中发生未知异常: " + e.getMessage());
                e.printStackTrace();
                break; // 未知异常不重试
            }
        }
        
        if (retryCount >= maxRetries) {
            System.out.println("达到最大重试次数，退款请求仍然失败");
        }
        
        if (response != null && response.isSuccess()) {
            System.out.println("退款调用成功");
        } else {
            System.out.println("退款调用失败");
            String diagnosisUrl = DiagnosisUtils.getDiagnosisUrl(response);
            System.out.println(diagnosisUrl);
        }

        return body;
    }

    /**
     * 支付宝退款查询接口
     * @param outTradeNo 商户订单号
     * @param tradeNo 支付宝交易号
     * @param outRequestNo 退款请求号
     * @return 退款查询结果
     */
    public String refundQuery(String outTradeNo, String tradeNo, String outRequestNo) {
        // 确保alipayClient已初始化，设置连接超时和读取超时时间
        if (alipayClient == null) {
            alipayClient = new DefaultAlipayClient(GATEWAY_URL, APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE, "30000", "30000");
        }

        AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
        AlipayTradeFastpayRefundQueryModel model = new AlipayTradeFastpayRefundQueryModel();

        if (outTradeNo != null && !outTradeNo.isEmpty()) {
            model.setOutTradeNo(outTradeNo);
        }

        if (tradeNo != null && !tradeNo.isEmpty()) {
            model.setTradeNo(tradeNo);
        }

        // 退款请求号是必填项
        if (outRequestNo == null || outRequestNo.isEmpty()) {
            throw new IllegalArgumentException("退款请求号不能为空");
        }
        model.setOutRequestNo(outRequestNo);

        request.setBizModel(model);

        AlipayTradeFastpayRefundQueryResponse response = null;
        String body = null;

        try {
            System.out.println("开始执行退款查询请求...");
            response = alipayClient.execute(request);
            body = response.getBody();
            System.out.println("退款查询响应：" + body);

            if (response.isSuccess()) {
                System.out.println("退款查询调用成功");
            } else {
                System.out.println("退款查询调用失败");
                String diagnosisUrl = DiagnosisUtils.getDiagnosisUrl(response);
                System.out.println(diagnosisUrl);
            }
        } catch (AlipayApiException e) {
            System.err.println("退款查询请求异常: " + e.getMessage());
            // 详细记录异常信息
            if (e.getCause() != null) {
                System.err.println("异常原因: " + e.getCause().getMessage());
                e.getCause().printStackTrace();
            }
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("退款查询过程中发生未知异常: " + e.getMessage());
            e.printStackTrace();
        }
        
        return body;
    }
}
