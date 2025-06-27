package com.example.movie_booking_backend.service.impl;

import com.example.movie_booking_backend.model.domain.Seats;
import com.example.movie_booking_backend.mapper.SeatsMapper;
import com.example.movie_booking_backend.service.ISeatsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 座位表 服务实现类
 * </p>
 *
 * @author tjl
 * @since 2025-06-28
 */
@Service
public class SeatsServiceImpl extends ServiceImpl<SeatsMapper, Seats> implements ISeatsService {

}
