package com.example.movie_booking_backend.model.vo;

import com.example.movie_booking_backend.model.domain.Users;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResultVO {
    private String status; // "bound" | "unbound"
    private String token;  // 已绑定用户的JWT
    private Users user;    // 已绑定用户信息
    private QQBindVO bindInfo; // 需要绑定时的QQ信息

    public static AuthResultVO bound(Users user, String token) {
        return AuthResultVO.builder()
                .status("bound")
                .user(user)
                .token(token)
                .build();
    }

    public static AuthResultVO unbound(QQBindVO qqInfo) {
        return AuthResultVO.builder()
                .status("unbound")
                .bindInfo(qqInfo)
                .build();
    }
}