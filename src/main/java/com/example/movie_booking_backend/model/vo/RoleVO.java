package com.example.movie_booking_backend.model.vo;

import com.example.movie_booking_backend.model.domain.Permissions;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel(value = "角色信息", description = "包含权限列表的角色视图对象")
public class RoleVO {

    @ApiModelProperty(value = "角色ID")
    private Long id;

    @ApiModelProperty(value = "角色名")
    private String name;

    @ApiModelProperty(value = "角色显示名称")
    private String displayName;

    @ApiModelProperty(value = "角色描述")
    private String description;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty(value = "权限列表")
    private List<Permissions> permissions;
} 