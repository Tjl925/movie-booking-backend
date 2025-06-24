package com.example.movie_booking_backend.web.controller;

import com.example.movie_booking_backend.common.JsonResponse;
import com.example.movie_booking_backend.common.annotation.RequireRole;
import com.example.movie_booking_backend.common.constants.PermissionConstants;
import com.example.movie_booking_backend.model.domain.Permissions;
import com.example.movie_booking_backend.service.IPermissionsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@Api(tags = "权限管理")
@RestController
@RequestMapping("/api/permissions")
public class PermissionsController {

    @Autowired
    private IPermissionsService permissionsService;

    @ApiOperation("获取所有可用权限列表")
    @GetMapping
    @RequireRole({PermissionConstants.ROLE_ADMIN, PermissionConstants.ROLE_SUPER_ADMIN})
    public JsonResponse<List<Permissions>> getAllPermissions() {
        return JsonResponse.success(permissionsService.list());
    }
}

