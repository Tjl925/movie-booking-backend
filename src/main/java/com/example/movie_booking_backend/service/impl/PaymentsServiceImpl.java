package com.example.movie_booking_backend.service.impl;

import com.example.movie_booking_backend.model.domain.Payments;
import com.example.movie_booking_backend.mapper.PaymentsMapper;
import com.example.movie_booking_backend.service.IPaymentsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}
