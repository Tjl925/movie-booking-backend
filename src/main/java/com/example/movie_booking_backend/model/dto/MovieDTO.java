package com.example.movie_booking_backend.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@ApiModel(value = "电影请求", description = "创建或更新电影的请求参数")
public class MovieDTO {

    @NotBlank(message = "电影标题不能为空")
    @ApiModelProperty(value = "标题", required = true)
    private String title;

    @ApiModelProperty(value = "描述")
    private String description;

    @NotNull(message = "时长（分钟）不能为空")
    @ApiModelProperty(value = "时长（分钟）", required = true)
    private Integer durationMinutes;

    @ApiModelProperty(value = "语言")
    private String language;

    @NotNull(message = "上映日期不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "上映日期", required = true)
    private LocalDate releaseDate;

    @NotNull(message = "下映日期不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "下映日期", required = true)
    private LocalDate endDate;

    @ApiModelProperty(value = "国家/地区")
    private String country;

    @ApiModelProperty(value = "类型")
    private String genre;

    @ApiModelProperty(value = "导演")
    private String director;

    @ApiModelProperty(value = "主演")
    private String actors;

    @ApiModelProperty(value = "海报图片URL")
    private String posterUrl;

    @ApiModelProperty(value = "预告片URL")
    private String trailerUrl;
}
