package com.example.movie_booking_backend.service.impl;

import com.example.movie_booking_backend.model.domain.OrderItems;
import com.example.movie_booking_backend.mapper.OrderItemsMapper;
import com.example.movie_booking_backend.service.IOrderItemsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单项表 服务实现类
 * </p>
 *
 * @author tjl
 * @since 2025-06-28
 */
@Service
public class OrderItemsServiceImpl extends ServiceImpl<OrderItemsMapper, OrderItems> implements IOrderItemsService {

}
