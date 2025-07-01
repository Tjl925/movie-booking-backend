package com.example.movie_booking_backend.model.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 电影表
 * </p>
 *
 * @author tjl
 * @since 2025-06-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("movies")
@ApiModel(value = "Movies对象", description = "电影表")
public class Movies implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "电影ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "电影标题")
    @TableField("title")
    private String title;

    @ApiModelProperty(value = "电影描述")
    @TableField("description")
    private String description;

    @ApiModelProperty(value = "导演")
    @TableField("director")
    private String director;

    @ApiModelProperty(value = "演员")
    @TableField("actors")
    private String actors;

    @ApiModelProperty(value = "类型")
    @TableField("genre")
    private String genre;

    @ApiModelProperty(value = "时长（分钟）")
    @TableField("duration_minutes")
    private Integer durationMinutes;

    @ApiModelProperty(value = "上映日期")
    @TableField("release_date")
    private LocalDate releaseDate;

    @ApiModelProperty(value = "下映日期")
    @TableField("end_date")
    private LocalDate endDate;

    @ApiModelProperty(value = "海报URL")
    @TableField("poster_url")
    private String posterUrl;

    @ApiModelProperty(value = "预告片URL")
    @TableField("trailer_url")
    private String trailerUrl;

    @ApiModelProperty(value = "基础票价")
    @TableField("base_price")
    private BigDecimal basePrice;

    @ApiModelProperty(value = "状态")
    @TableField("status")
    private String status;

    @ApiModelProperty(value = "评分")
    @TableField("rating")
    private BigDecimal rating;

    @ApiModelProperty(value = "评分人数")
    @TableField("rating_count")
    private Integer ratingCount;

    @ApiModelProperty(value = "观看次数")
    @TableField("view_count")
    private Integer viewCount;

    @ApiModelProperty(value = "语言")
    @TableField("language")
    private String language;

    @ApiModelProperty(value = "国家/地区")
    @TableField("country")
    private String country;

    @ApiModelProperty(value = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @ApiModelProperty(value = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @ApiModelProperty(value = "是否删除")
    @TableField("is_deleted")
    private Boolean deleted;

    @ApiModelProperty(value = "票房数")
    @TableField("box_office")
    private Long boxOffice;

}
