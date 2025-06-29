package com.example.movie_booking_backend.model.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 订单项表
 * </p>
 *
 * @author tjl
 * @since 2025-06-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("order_items")
@ApiModel(value="OrderItems对象", description="订单项表")
public class OrderItems implements Serializable {

    private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "订单项ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

        @ApiModelProperty(value = "订单ID")
    @TableField("order_id")
    private Long orderId;

        @ApiModelProperty(value = "价格")
    @TableField("price")
    private BigDecimal price;

        @ApiModelProperty(value = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

        @ApiModelProperty(value = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;

        @ApiModelProperty(value = "是否删除")
    @TableField("is_deleted")
    private Boolean deleted;

    @TableField("seat_id")
    private Long seatId;


}
