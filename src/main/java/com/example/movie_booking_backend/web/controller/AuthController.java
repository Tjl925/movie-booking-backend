package com.example.movie_booking_backend.web.controller;

import com.example.movie_booking_backend.common.JsonResponse;
import com.example.movie_booking_backend.model.dto.LoginDTO;
import com.example.movie_booking_backend.model.dto.RegisterDTO;
import com.example.movie_booking_backend.model.vo.LoginResponseVO;
import com.example.movie_booking_backend.service.IAuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Api(tags = "认证管理")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private IAuthService authService;

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public JsonResponse<LoginResponseVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginResponseVO response = authService.login(loginDTO);
        return JsonResponse.success(response, "登录成功");
    }

    @ApiOperation("用户注册")
    @PostMapping("/register")
    public JsonResponse<String> register(@Valid @RequestBody RegisterDTO registerDTO) {
        authService.register(registerDTO);
        return JsonResponse.successMessage("注册成功");
    }

    @ApiOperation("用户登出")
    @PostMapping("/logout")
    public JsonResponse<String> logout(@RequestHeader("Authorization") String token) {
        // 去掉Bearer前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        authService.logout(token);
        return JsonResponse.successMessage("登出成功");
    }
} 