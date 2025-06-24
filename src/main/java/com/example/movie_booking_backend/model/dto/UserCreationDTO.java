package com.example.movie_booking_backend.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@ApiModel(value = "用户创建请求", description = "管理员创建新用户时的请求参数")
public class UserCreationDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    @ApiModelProperty(value = "用户名", required = true, example = "newuser")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    @ApiModelProperty(value = "密码", required = true, example = "password123")
    private String password;

    @Email(message = "邮箱格式不正确")
    @ApiModelProperty(value = "邮箱", example = "newuser@example.com")
    private String email;

    @ApiModelProperty(value = "手机号", example = "13900139000")
    private String phone;

    @NotNull(message = "角色ID不能为空")
    @ApiModelProperty(value = "角色ID", required = true, example = "1")
    private Long roleId;
}
