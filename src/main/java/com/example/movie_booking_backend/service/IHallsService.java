package com.example.movie_booking_backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.movie_booking_backend.model.domain.Halls;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 影厅表 服务类
 * </p>
 *
 * @author tjl
 * @since 2025-06-28
 */
public interface IHallsService extends IService<Halls> {
    
    /**
     * 获取可用的影厅列表（状态为ACTIVE的影厅）
     * @return 可用影厅列表
     */
    List<Halls> getActiveHalls();
    
    /**
     * 更新影厅状态
     * @param id 影厅ID
     * @param status 新状态
     * @return 更新后的影厅对象
     */
    Halls updateHallStatus(Long id, String status);
    
    /**
     * 检查影厅是否可用于排片
     * @param hallId 影厅ID
     * @return 是否可用
     */
    boolean isHallAvailableForScheduling(Long hallId);
}
