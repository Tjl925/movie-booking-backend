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
        try {
            // 验证邮箱参数
            if (emailRequestDTO.getEmail() == null || emailRequestDTO.getEmail().isEmpty()) {
                return JsonResponse.failure("邮箱地址不能为空");
            }
            
            System.out.println("收到发送验证码请求，邮箱: " + emailRequestDTO.getEmail());
            
            // 生成验证码
            String code = verificationCodeService.generateCode(emailRequestDTO.getEmail());
            
            // 发送验证码邮件
            boolean sendResult = emailApi.sendVerificationCodeEmail(emailRequestDTO.getEmail(), code);
            
            if (sendResult) {
                System.out.println("验证码发送成功: " + emailRequestDTO.getEmail());
                return JsonResponse.success(true, "发送验证码成功");
            } else {
                System.err.println("验证码发送失败: " + emailRequestDTO.getEmail());
                return JsonResponse.failure("发送验证码失败，请稍后重试");
            }
        } catch (Exception e) {
            System.err.println("发送验证码过程中发生异常: " + e.getMessage());
            e.printStackTrace();
            return JsonResponse.failure("发送验证码时发生错误: " + e.getMessage());
        }
    }

    @PostMapping("/verify-code")
    public JsonResponse<Boolean> verifyCode(@RequestBody EmailRequestDTO emailRequestDTO) {
        try {
            // 验证参数
            if (emailRequestDTO.getEmail() == null || emailRequestDTO.getCode() == null) {
                return JsonResponse.failure("邮箱和验证码不能为空");
            }
            
            System.out.println("收到验证码验证请求，邮箱: " + emailRequestDTO.getEmail() + ", 验证码: " + emailRequestDTO.getCode());
            
            // 验证验证码
            boolean isValid = verificationCodeService.verifyCode(
                    emailRequestDTO.getEmail(),
                    emailRequestDTO.getCode()
            );
            
            if (isValid) {
                System.out.println("验证码验证成功: " + emailRequestDTO.getEmail());
                // 验证成功后移除验证码
                verificationCodeService.removeCode(emailRequestDTO.getEmail());
                return JsonResponse.success(true, "验证成功");
            } else {
                System.err.println("验证码验证失败: " + emailRequestDTO.getEmail());
                return JsonResponse.failure("验证码错误或已过期");
            }
        } catch (Exception e) {
            System.err.println("验证码验证过程中发生异常: " + e.getMessage());
            e.printStackTrace();
            return JsonResponse.failure("验证码验证时发生错误: " + e.getMessage());
        }
    }

    @PostMapping("/send-password")
    public JsonResponse<Boolean> sendPassWordCode(@RequestBody EmailRequestDTO emailRequestDTO) {
        try {
            // 验证邮箱参数
            if (emailRequestDTO.getEmail() == null || emailRequestDTO.getEmail().isEmpty()) {
                return JsonResponse.failure("邮箱地址不能为空");
            }
            
            System.out.println("收到发送密码请求，邮箱: " + emailRequestDTO.getEmail());
            
            // 获取密码
            String passWord = usersService.getPasswordByEmail(emailRequestDTO.getEmail());
            if (passWord == null || passWord.isEmpty()) {
                System.err.println("未找到该邮箱对应的密码: " + emailRequestDTO.getEmail());
                return JsonResponse.failure("未找到该邮箱对应的账户");
            }
            
            // 发送密码邮件
            boolean sendResult = emailApi.sendPassWordEmail(passWord, emailRequestDTO.getEmail());
            
            if (sendResult) {
                System.out.println("密码发送成功: " + emailRequestDTO.getEmail());
                return JsonResponse.success(true, "已将密码发送至您的邮箱，注意查收~");
            } else {
                System.err.println("密码发送失败: " + emailRequestDTO.getEmail());
                return JsonResponse.failure("发送密码失败，请稍后重试");
            }
        } catch (Exception e) {
            System.err.println("发送密码过程中发生异常: " + e.getMessage());
            e.printStackTrace();
            return JsonResponse.failure("发送密码时发生错误: " + e.getMessage());
        }
    }

}

