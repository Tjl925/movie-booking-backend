package com.example.movie_booking_backend.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel(value = "SeatSessionVO", description = "单个座位场次关联信息视图对象")
public class SeatSessionVO {
    @ApiModelProperty(value = "关联ID")
    private Long id;

    @ApiModelProperty(value = "座位ID")
    private Long seatId;

    @ApiModelProperty(value = "场次ID")
    private Long sessionId;

    @ApiModelProperty(value = "行号")
    private Integer rowNumber; // 确保与前端使用的 rowNumber 一致

    @ApiModelProperty(value = "列号")
    private Integer columnNumber; // 确保与前端使用的 columnNumber 一致

    @ApiModelProperty(value = "状态 (AVAILABLE, RESERVED, MAINTENANCE, OCCUPIED)")
    private String status;

    @ApiModelProperty(value = "座位类型")
    private String seatType;

    @ApiModelProperty(value = "价格倍数，根据座位类型计算")
    private BigDecimal priceMultiplier;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updatedAt;

    @ApiModelProperty(value = "是否删除")
    private Boolean deleted;
}