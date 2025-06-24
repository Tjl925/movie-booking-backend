package com.example.movie_booking_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.movie_booking_backend.model.domain.OrderItems;

import java.util.List;

/**
 * <p>
 * 订单项表 Mapper 接口
 * </p>
 *
 * @author tjl
 * @since 2025-06-23
 */
public interface OrderItemsMapper extends BaseMapper<OrderItems> {

    List<Long> findSeatIdsBySessionId(Long sessionId);

    void insertBatchSomeColumn(List<OrderItems> orderItems);
}
