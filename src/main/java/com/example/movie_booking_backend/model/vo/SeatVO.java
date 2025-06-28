package com.example.movie_booking_backend.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "座位信息视图", description = "场次中的座位信息视图对象")
public class SeatVO {

    @ApiModelProperty(value = "座位ID")
    private Long id;

    @ApiModelProperty(value = "行号")
    private Integer rowNumber;

    @ApiModelProperty(value = "列号")
    private Integer columnNumber;

    @ApiModelProperty(value = "状态 (AVAILABLE, RESERVED, MAINTENANCE,OCCUPIED)")
    private String status;
} 