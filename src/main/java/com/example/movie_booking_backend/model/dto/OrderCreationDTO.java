package com.example.movie_booking_backend.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "创建订单请求", description = "用户下单时的请求参数")
public class OrderCreationDTO {

    @NotNull(message = "场次ID不能为空")
    @ApiModelProperty(value = "场次ID", required = true)
    private Long sessionId;

    @NotEmpty(message = "必须至少选择一个座位")
    @ApiModelProperty(value = "座位ID列表", required = true)
    private List<Long> seatIds;
}
