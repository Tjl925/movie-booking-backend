package com.example.movie_booking_backend.model.vo;

import com.example.movie_booking_backend.model.domain.Halls;
import com.example.movie_booking_backend.model.domain.Movies;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(value = "场次信息视图", description = "包含电影和影厅详细信息的场次视图对象")
public class SessionVO {

    @ApiModelProperty(value = "场次ID")
    private Long id;

    @ApiModelProperty(value = "电影信息")
    private Movies movie;

    @ApiModelProperty(value = "影厅信息")
    private Halls hall;

    @ApiModelProperty(value = "开始时间")
    private LocalDateTime sessionTime;

    @ApiModelProperty(value = "结束时间")
    private LocalDateTime endTime;
} 