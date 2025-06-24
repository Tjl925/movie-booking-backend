package com.example.movie_booking_backend.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "角色权限请求", description = "更新角色权限的请求参数")
public class RolePermissionDTO {

    @ApiModelProperty(value = "权限ID列表", required = true)
    private List<Long> permissionIds;
}
