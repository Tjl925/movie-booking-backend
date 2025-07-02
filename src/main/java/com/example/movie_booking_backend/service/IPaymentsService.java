package com.example.movie_booking_backend.service;

import com.example.movie_booking_backend.model.domain.Payments;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 支付记录表 服务类
 * </p>
 *
 * @author tjl
 * @since 2025-06-23
 */
public interface IPaymentsService extends IService<Payments> {
    
    /**
     * 根据订单ID获取最近的退款记录
     * @param orderId 订单ID
     * @return 退款记录
     */
    Payments getLatestRefundByOrderId(Long orderId);
    
    /**
     * 根据订单ID和退款请求号获取退款记录
     * @param orderId 订单ID
     * @param outRequestNo 退款请求号
     * @return 退款记录
     */
    Payments getRefundByOrderIdAndRequestNo(Long orderId, String outRequestNo);
}
