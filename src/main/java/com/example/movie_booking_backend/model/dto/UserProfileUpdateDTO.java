package com.example.movie_booking_backend.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@ApiModel(value = "用户个人信息更新请求", description = "普通用户可修改的个人信息")
public class UserProfileUpdateDTO {

    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    @ApiModelProperty(value = "用户名", example = "newusername")
    private String username;

    @Email(message = "邮箱格式不正确")
    @ApiModelProperty(value = "邮箱", example = "newuser@example.com")
    private String email;

    @ApiModelProperty(value = "手机号", example = "13900139000")
    private String phone;

    @ApiModelProperty(value = "头像URL（前端上传后自动更新，用户不能直接修改）", example = "/avatar/user123.jpg")
    private String avatar; // 由后端更新，前端只传文件
}


