package com.example.movie_booking_backend.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "登录响应", description = "用户登录响应信息")
public class LoginResponseVO {

    @ApiModelProperty(value = "访问令牌")
    private String token;

    @ApiModelProperty(value = "用户信息")
    private UserVO userInfo;

    @ApiModelProperty(value = "过期时间")
    private Long expiresIn;
} 