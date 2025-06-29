package com.example.movie_booking_backend.model.vo;

import com.example.movie_booking_backend.model.domain.OrderItems;
import com.example.movie_booking_backend.model.domain.Payments;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel(value = "订单详情视图", description = "包含订单所有详细信息的视图对象")
public class OrderVO {

    @ApiModelProperty(value = "订单ID")
    private Long id;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "总金额")
    private Double totalAmount;

    @ApiModelProperty(value = "订单状态")
    private String status;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty(value = "场次信息")
    private SessionVO session;

    @ApiModelProperty(value = "订单项（电影票）")
    private List<OrderItems> orderItems;

    @ApiModelProperty(value = "支付信息")
    private Payments payment;
} 