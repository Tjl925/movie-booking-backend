package com.example.movie_booking_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.movie_booking_backend.common.exception.BusinessException;
import com.example.movie_booking_backend.mapper.PermissionsMapper;
import com.example.movie_booking_backend.model.domain.Permissions;
import com.example.movie_booking_backend.model.domain.Roles;
import com.example.movie_booking_backend.mapper.RolesMapper;
import com.example.movie_booking_backend.model.dto.RoleDTO;
import com.example.movie_booking_backend.model.vo.RoleVO;
import com.example.movie_booking_backend.service.IRolesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 角色表 服务实现类
 * </p>
 *
 * @author tjl
 * @since 2025-06-23
 */
@Service
public class RolesServiceImpl extends ServiceImpl<RolesMapper, Roles> implements IRolesService {

    @Autowired
    private RolesMapper rolesMapper;

    @Autowired
    private PermissionsMapper permissionsMapper;

    @Override
    public RoleVO getRoleWithPermissions(Long roleId) {
        Roles role = this.getById(roleId);
        if (role == null || role.getDeleted()) {
            throw new BusinessException("角色不存在");
        }

        RoleVO roleVO = new RoleVO();
        BeanUtils.copyProperties(role, roleVO);

        List<Permissions> permissions = rolesMapper.findPermissionsByRoleId(roleId);
        roleVO.setPermissions(permissions);

        return roleVO;
    }

    @Override
    @Transactional
    public Roles createRole(RoleDTO roleDTO) {
        if (this.getOne(new QueryWrapper<Roles>().eq("name", roleDTO.getName()).eq("is_deleted", false)) != null) {
            throw new BusinessException("角色名已存在");
        }

        Roles role = new Roles();
        BeanUtils.copyProperties(roleDTO, role);
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        role.setDeleted(false);

        this.save(role);
        return role;
    }

    @Override
    @Transactional
    public Roles updateRole(Long roleId, RoleDTO roleDTO) {
        Roles existingRole = this.getById(roleId);
        if (existingRole == null || existingRole.getDeleted()) {
            throw new BusinessException("角色不存在");
        }

        // 检查更新后的角色名是否与其它角色冲突
        Roles roleWithSameName = this.getOne(new QueryWrapper<Roles>()
                .eq("name", roleDTO.getName())
                .ne("id", roleId)
                .eq("is_deleted", false));
        if (roleWithSameName != null) {
            throw new BusinessException("角色名已存在");
        }

        BeanUtils.copyProperties(roleDTO, existingRole);
        existingRole.setId(roleId);
        existingRole.setUpdatedAt(LocalDateTime.now());

        this.updateById(existingRole);
        return existingRole;
    }

    @Override
    @Transactional
    public void updateRolePermissions(Long roleId, List<Long> permissionIds) {
        Roles role = this.getById(roleId);
        if (role == null || role.getDeleted()) {
            throw new BusinessException("角色不存在");
        }

        // 验证所有传入的permissionId都是有效的
        if (permissionIds != null && !permissionIds.isEmpty()) {
            long count = permissionsMapper.selectCount(new QueryWrapper<Permissions>().in("id", permissionIds));
            if (count != permissionIds.size()) {
                throw new BusinessException("包含无效的权限ID");
            }
        }

        // 删除旧的关联关系
        rolesMapper.deletePermissionsByRoleId(roleId);

        // 建立新的关联关系
        if (permissionIds != null && !permissionIds.isEmpty()) {
            rolesMapper.insertRolePermissions(roleId, permissionIds);
        }
    }

    @Override
    @Transactional
    public void deleteRole(Long roleId) {
        Roles role = this.getById(roleId);
        if (role == null || role.getDeleted()) {
            throw new BusinessException("角色不存在");
        }

        // 不能删除基础角色
        if (List.of("SUPER_ADMIN", "ADMIN", "USER").contains(role.getName())) {
            throw new BusinessException("不能删除系统基础角色");
        }

        // 软删除
        role.setDeleted(true);
        role.setUpdatedAt(LocalDateTime.now());
        this.updateById(role);

        // 删除关联的权限
        rolesMapper.deletePermissionsByRoleId(roleId);
    }
}
