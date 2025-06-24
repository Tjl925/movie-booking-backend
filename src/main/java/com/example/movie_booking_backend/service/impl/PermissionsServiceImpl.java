package com.example.movie_booking_backend.service.impl;

import com.example.movie_booking_backend.model.domain.Permissions;
import com.example.movie_booking_backend.mapper.PermissionsMapper;
import com.example.movie_booking_backend.service.IPermissionsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 权限表 服务实现类
 * </p>
 *
 * @author tjl
 * @since 2025-06-23
 */
@Service
public class PermissionsServiceImpl extends ServiceImpl<PermissionsMapper, Permissions> implements IPermissionsService {

}
