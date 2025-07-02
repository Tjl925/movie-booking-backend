package com.example.movie_booking_backend.service.impl;

import com.example.movie_booking_backend.model.domain.Payments;
import com.example.movie_booking_backend.mapper.PaymentsMapper;
import com.example.movie_booking_backend.service.IPaymentsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * <p>
 * 支付记录表 服务实现类
 * </p>
 *
 * @author tjl
 * @since 2025-06-23
 */
@Service
public class PaymentsServiceImpl extends ServiceImpl<PaymentsMapper, Payments> implements IPaymentsService {

    /**
     * 根据订单ID获取最近的退款记录
     * @param orderId 订单ID
     * @return 退款记录
     */
    @Override
    public Payments getLatestRefundByOrderId(Long orderId) {
        QueryWrapper<Payments> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId)
                .eq("payment_status", "REFUNDED")
                .orderByDesc("payment_time")
                .last("LIMIT 1");
        
        return this.getOne(queryWrapper);
    }
    
    /**
     * 根据订单ID和退款请求号获取退款记录
     * @param orderId 订单ID
     * @param outRequestNo 退款请求号
     * @return 退款记录
     */
    @Override
    public Payments getRefundByOrderIdAndRequestNo(Long orderId, String outRequestNo) {
        QueryWrapper<Payments> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId)
                .eq("payment_status", "REFUNDED");
        
        // 如果提供了退款请求号，则在网关响应中查找
        if (outRequestNo != null && !outRequestNo.isEmpty()) {
            queryWrapper.like("gateway_response", outRequestNo);
        }
        
        return this.getOne(queryWrapper);
    }
}
