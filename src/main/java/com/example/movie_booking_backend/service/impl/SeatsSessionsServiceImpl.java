package com.example.movie_booking_backend.service.impl;

import com.example.movie_booking_backend.model.domain.SeatsSessions;
import com.example.movie_booking_backend.mapper.SeatsSessionsMapper;
import com.example.movie_booking_backend.service.ISeatsSessionsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 座位场次关联表 服务实现类
 * </p>
 *
 * @author tjl
 * @since 2025-06-28
 */
@Service
public class SeatsSessionsServiceImpl extends ServiceImpl<SeatsSessionsMapper, SeatsSessions> implements ISeatsSessionsService {

}
