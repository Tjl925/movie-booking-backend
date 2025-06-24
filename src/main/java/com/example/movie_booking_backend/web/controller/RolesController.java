package com.example.movie_booking_backend.web.controller;

import com.example.movie_booking_backend.common.JsonResponse;
import com.example.movie_booking_backend.common.annotation.RequireRole;
import com.example.movie_booking_backend.common.constants.PermissionConstants;
import com.example.movie_booking_backend.model.domain.Roles;
import com.example.movie_booking_backend.model.dto.RoleDTO;
import com.example.movie_booking_backend.model.dto.RolePermissionDTO;
import com.example.movie_booking_backend.model.vo.RoleVO;
import com.example.movie_booking_backend.service.IRolesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 *
 *  前端控制器
 *
 *
 * @author tjl
 * @since 2025-06-23
 * @version v1.0
 */

 @Api(tags = "角色管理")
 @RestController
 @RequestMapping("/api/roles")
 public class RolesController {
     @Autowired
     private IRolesService rolesService;
 
     @ApiOperation("获取所有角色列表")
     @GetMapping
     @RequireRole({PermissionConstants.ROLE_ADMIN, PermissionConstants.ROLE_SUPER_ADMIN})
     public JsonResponse<List<Roles>> getAllRoles() {
         return JsonResponse.success(rolesService.list(
                 new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Roles>().eq("is_deleted", false)
         ));
     }
 
     @ApiOperation("根据ID获取角色及其权限")
     @GetMapping("/{id}")
     @RequireRole({PermissionConstants.ROLE_ADMIN, PermissionConstants.ROLE_SUPER_ADMIN})
     public JsonResponse<RoleVO> getRoleById(@PathVariable Long id) {
         RoleVO roleVO = rolesService.getRoleWithPermissions(id);
         return JsonResponse.success(roleVO);
     }
 
     @ApiOperation("创建新角色")
     @PostMapping
     @RequireRole({PermissionConstants.ROLE_SUPER_ADMIN})
     public JsonResponse<Roles> createRole(@RequestBody RoleDTO roleDTO) {
         Roles newRole = rolesService.createRole(roleDTO);
         return JsonResponse.success(newRole, "角色创建成功");
     }
 
     @ApiOperation("更新角色信息")
     @PutMapping("/{id}")
     @RequireRole({PermissionConstants.ROLE_SUPER_ADMIN})
     public JsonResponse<Roles> updateRole(@PathVariable Long id, @Valid @RequestBody RoleDTO roleDTO) {
         Roles updatedRole = rolesService.updateRole(id, roleDTO);
         return JsonResponse.success(updatedRole, "角色更新成功");
     }
 
     @ApiOperation("删除角色")
     @DeleteMapping("/{id}")
     @RequireRole({PermissionConstants.ROLE_SUPER_ADMIN})
     public JsonResponse<String> deleteRole(@PathVariable Long id) {
         rolesService.deleteRole(id);
         return JsonResponse.successMessage("角色删除成功");
     }
 
     @ApiOperation("更新角色的权限")
     @PutMapping("/{id}/permissions")
     @RequireRole({PermissionConstants.ROLE_SUPER_ADMIN})
     public JsonResponse<String> updateRolePermissions(@PathVariable Long id, @RequestBody RolePermissionDTO rolePermissionDTO) {
         rolesService.updateRolePermissions(id, rolePermissionDTO.getPermissionIds());
         return JsonResponse.successMessage("角色权限更新成功");
     }
 }