package com.example.movie_booking_backend.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@ApiModel(value = "场次请求", description = "创建或更新场次的请求参数")
public class SessionDTO {

    @NotNull(message = "电影ID不能为空")
    @ApiModelProperty(value = "电影ID", required = true)
    private Long movieId;

    @NotNull(message = "影厅ID不能为空")
    @ApiModelProperty(value = "影厅ID", required = true)
    private Long hallId;

    @NotNull(message = "开始时间不能为空")
    @Future(message = "开始时间必须是未来的时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "开始时间 (yyyy-MM-dd HH:mm:ss)", required = true)
    private LocalDateTime startTime;

    // 结束时间将根据电影时长自动计算，无需传入

    @NotNull(message = "票价不能为空")
    @ApiModelProperty(value = "票价", required = true)
    private Double price;
}
