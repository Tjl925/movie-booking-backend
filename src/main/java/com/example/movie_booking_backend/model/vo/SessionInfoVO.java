package com.example.movie_booking_backend.model.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SessionInfoVO {
    // 场次基本信息
    private Long sessionId;
    private LocalDateTime sessionTime;
    private LocalDateTime endTime;

    // 影厅信息
    private String hallName;
    private String hallType;

    // 座位信息
    private Integer availableSeats;
    private Integer totalSeats;

    // 票价信息
    private BigDecimal basePrice;      // 电影基础票价
    private BigDecimal priceAdjustment; // 场次价格调整倍数
    private BigDecimal minPrice;       // 最低票价 = basePrice * priceAdjustment
}