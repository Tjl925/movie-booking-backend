package com.example.movie_booking_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.movie_booking_backend.model.domain.Permissions;
import com.example.movie_booking_backend.model.domain.Roles;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 角色表 Mapper 接口
 * </p>
 *
 * @author tjl
 * @since 2025-06-23
 */
public interface RolesMapper extends BaseMapper<Roles> {

    List<Permissions> findPermissionsByRoleId(@Param("roleId") Long roleId);

    void deletePermissionsByRoleId(@Param("roleId") Long roleId);

    void insertRolePermissions(@Param("roleId") Long roleId, @Param("permissionIds") List<Long> permissionIds);
}
