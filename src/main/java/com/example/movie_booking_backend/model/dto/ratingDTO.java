package com.example.movie_booking_backend.model.dto;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ratingDTO {
    @ApiModelProperty(value = "订单ID", required = true)
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @ApiModelProperty(value = "电影ID", required = true)
    @NotNull(message = "电影ID不能为空")
    private Long movieId;

    @ApiModelProperty(value = "评分(1-10分)", required = true)
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低为1分")
    @Max(value = 10, message = "评分最高为10分")
    private Integer rating;

    public Long getOrderId() {
        return orderId;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
}
