package com.example.movie_booking_backend.service;

import com.example.movie_booking_backend.model.dto.LoginDTO;
import com.example.movie_booking_backend.model.dto.RegisterDTO;
import com.example.movie_booking_backend.model.vo.LoginResponseVO;
import jakarta.validation.Valid;

public interface IAuthService {

    LoginResponseVO login(LoginDTO loginDTO);

    LoginResponseVO register(RegisterDTO registerDTO);

    void logout(String token);

    String validate(@Valid RegisterDTO registerDTO);
}