package com.example.movie_booking_backend.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QQBindVO {
    private String openId;
    private String nickname;
    private String avatar;
    private String suggestedUsername; // 生成的建议用户名
}