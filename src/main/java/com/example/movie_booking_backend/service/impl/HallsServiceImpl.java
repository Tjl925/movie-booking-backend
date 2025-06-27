package com.example.movie_booking_backend.service.impl;

import com.example.movie_booking_backend.model.domain.Halls;
import com.example.movie_booking_backend.mapper.HallsMapper;
import com.example.movie_booking_backend.service.IHallsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 影厅表 服务实现类
 * </p>
 *
 * @author tjl
 * @since 2025-06-28
 */
@Service
public class HallsServiceImpl extends ServiceImpl<HallsMapper, Halls> implements IHallsService {

}
