package com.example.movie_booking_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.movie_booking_backend.common.exception.BusinessException;
import com.example.movie_booking_backend.model.domain.Halls;
import com.example.movie_booking_backend.mapper.HallsMapper;
import com.example.movie_booking_backend.service.IHallsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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

    @Override
    public List<Halls> getActiveHalls() {
        QueryWrapper<Halls> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "ACTIVE")
                .eq("is_deleted", false);
        return this.list(queryWrapper);
    }

    @Override
    public Halls updateHallStatus(Long id, String status) {
        Halls hall = this.getById(id);
        if (hall == null || hall.getDeleted()) {
            throw new BusinessException("影厅不存在");
        }
        
        // 验证状态值是否合法
        if (!status.equals("ACTIVE") && !status.equals("INACTIVE") && !status.equals("MAINTENANCE")) {
            throw new BusinessException("无效的状态值");
        }
        
        hall.setStatus(status);
        hall.setUpdatedAt(LocalDateTime.now());
        this.updateById(hall);
        
        return hall;
    }

    @Override
    public boolean isHallAvailableForScheduling(Long hallId) {
        Halls hall = this.getById(hallId);
        if (hall == null || hall.getDeleted()) {
            return false;
        }
        
        // 只有状态为ACTIVE的影厅才可用于排片
        return "ACTIVE".equals(hall.getStatus());
    }
}
