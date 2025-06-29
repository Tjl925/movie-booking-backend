package com.example.movie_booking_backend.model.vo;

import com.example.movie_booking_backend.model.vo.SeatSessionVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "SeatsSessionsVO", description = "座位场次关联信息视图对象")
public class SeatsSessionsVO {
    @ApiModelProperty(value = "座位场次关联列表")
    private List<SeatSessionVO> seatSessions;

    @ApiModelProperty(value = "总座位数")
    private Integer totalSeats;

    @ApiModelProperty(value = "总行数")
    private Integer totalRows;

    @ApiModelProperty(value = "总列数")
    private Integer totalColumns;
}