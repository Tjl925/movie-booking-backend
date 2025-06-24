package com.example.movie_booking_backend.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@ApiModel(value = "角色请求", description = "创建或更新角色的请求参数")
public class RoleDTO {

    @NotBlank(message = "角色名不能为空")
    @ApiModelProperty(value = "角色名", required = true, example = "OPERATOR")
    private String name;

    @NotBlank(message = "角色显示名称不能为空")
    @ApiModelProperty(value = "角色显示名称", required = true, example = "运营人员")
    private String displayName;

    @ApiModelProperty(value = "角色描述", example = "负责日常运营工作的员工")
    private String description;
}
