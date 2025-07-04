package com.example.movie_booking_backend.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.movie_booking_backend.common.JsonResponse;
import com.example.movie_booking_backend.model.domain.Roles;
import com.example.movie_booking_backend.service.IRolesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
    public JsonResponse<List<Roles>> getAllRoles() {
        return JsonResponse.success(rolesService.list(
                new QueryWrapper<Roles>().eq("is_deleted", false)
        ));
    }
}