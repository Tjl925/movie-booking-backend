package com.example.movie_booking_backend.service;

import com.example.movie_booking_backend.model.domain.Roles;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.movie_booking_backend.model.dto.RoleDTO;
import com.example.movie_booking_backend.model.vo.RoleVO;

import java.util.List;

/**
 * <p>
 * 角色表 服务类
 * </p>
 *
 * @author tjl
 * @since 2025-06-23
 */
public interface IRolesService extends IService<Roles> {
    RoleVO getRoleWithPermissions(Long roleId);
    Roles createRole(RoleDTO roleDTO);
    Roles updateRole(Long roleId, RoleDTO roleDTO);
    void updateRolePermissions(Long roleId, List<Long> permissionIds);
    void deleteRole(Long roleId);
}
