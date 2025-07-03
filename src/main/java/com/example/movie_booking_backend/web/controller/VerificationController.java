package com.example.movie_booking_backend.web.controller;

import com.example.movie_booking_backend.common.JsonResponse;
import com.example.movie_booking_backend.common.utils.EmailApi;
import com.example.movie_booking_backend.model.dto.EmailRequestDTO;
import com.example.movie_booking_backend.service.IUsersService;
import com.example.movie_booking_backend.service.VerificationCodeService ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
public class VerificationController {
    private final EmailApi emailApi;
    private final VerificationCodeService verificationCodeService;
    @Autowired
    private IUsersService usersService;
    public VerificationController(EmailApi emailApi, VerificationCodeService verificationCodeService) {
        this.emailApi = emailApi;
        this.verificationCodeService = verificationCodeService;
    }

    @PostMapping("/send-code")
    public JsonResponse<Boolean> sendVerificationCode(@RequestBody EmailRequestDTO emailRequestDTO) {
        if (emailRequestDTO.getEmail() == null || emailRequestDTO.getEmail().isEmpty()) {
            return JsonResponse.failure("邮箱地址不能为空");
        }

        String code = verificationCodeService.generateCode(emailRequestDTO.getEmail());
        return JsonResponse.success(emailApi.sendVerificationCodeEmail(emailRequestDTO.getEmail(), code),"发送验证码成功");
    }

    @PostMapping("/verify-code")
    public JsonResponse<Boolean> verifyCode(@RequestBody EmailRequestDTO emailRequestDTO) {
        if (emailRequestDTO.getEmail() == null || emailRequestDTO.getCode() == null) {
            return JsonResponse.failure("邮箱和验证码不能为空");
        }

        boolean isValid = verificationCodeService.verifyCode(
                emailRequestDTO.getEmail(),
                emailRequestDTO.getCode()
        );

        if (isValid) {
            verificationCodeService.removeCode(emailRequestDTO.getEmail());
            return JsonResponse.success(true, "验证成功");
        } else {
            return JsonResponse.failure("验证码错误或已过期");
        }
    }

    @PostMapping("/send-password")
    public JsonResponse<Boolean> sendPassWordCode(@RequestBody EmailRequestDTO emailRequestDTO) {
        if (emailRequestDTO.getEmail() == null || emailRequestDTO.getEmail().isEmpty()) {
            return JsonResponse.failure("邮箱地址不能为空");
        }
        String passWord=usersService.getPasswordByEmail(emailRequestDTO.getEmail());
        return JsonResponse.success(emailApi.sendPassWordEmail(passWord,emailRequestDTO.getEmail()),"已将密码发送至您的邮箱，注意查收~");
    }

}

