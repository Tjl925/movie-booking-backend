
package com.example.movie_booking_backend.model.dto;

import lombok.Data;

@Data
public class BindRequestDTO {
    private String openId;     // QQ的open_id
    private String username;  // 用户名
    private String password;  // 密码（绑定现有账号时需要）
    private String avatar;    // QQ头像URL
    private String bindType;  // "existing"或"new"
}