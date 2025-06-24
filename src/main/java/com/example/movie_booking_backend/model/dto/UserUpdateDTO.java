package com.example.movie_booking_backend.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@ApiModel(value = "用户更新请求", description = "更新用户信息的请求参数")
public class UserUpdateDTO {

    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    @ApiModelProperty(value = "用户名", example = "newusername")
    private String username;

    @Email(message = "邮箱格式不正确")
    @ApiModelProperty(value = "邮箱", example = "newuser@example.com")
    private String email;

    @ApiModelProperty(value = "手机号", example = "13900139000")
    private String phone;

    @ApiModelProperty(value = "角色ID", example = "2")
    private Long roleId;

    @ApiModelProperty(value = "头像URL", example = "/path/to/avatar.jpg")
    private String avatar;
} 