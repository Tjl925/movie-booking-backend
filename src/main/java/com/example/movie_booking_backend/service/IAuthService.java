package com.example.movie_booking_backend.service;

import com.example.movie_booking_backend.common.JsonResponse;
import com.example.movie_booking_backend.model.dto.LoginDTO;
import com.example.movie_booking_backend.model.dto.RegisterDTO;
import com.example.movie_booking_backend.model.vo.LoginResponseVO;
import jakarta.validation.Valid;

public interface IAuthService {

    /**
     * 用户登录
     */
    LoginResponseVO login(LoginDTO loginDTO);

    /**
     * 用户注册
     */
    LoginResponseVO register(RegisterDTO registerDTO);

    /**
     * 用户登出
     */
    void logout(String token);

    String validate(@Valid RegisterDTO registerDTO);
}