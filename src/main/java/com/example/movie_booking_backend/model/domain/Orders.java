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
 * 订单表
 * </p>
 *
 * @author tjl
 * @since 2025-06-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("orders")
@ApiModel(value = "Orders对象", description = "订单表")
public class Orders implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "订单ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "订单号")
    @TableField("order_number")
    private String orderNumber;

    @ApiModelProperty(value = "用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "场次ID")
    @TableField("session_id")
    private Long sessionId;

    @ApiModelProperty(value = "座位号（逗号分隔）")
    @TableField("seat_numbers")
    private String seatNumbers;

    @ApiModelProperty(value = "票数")
    @TableField("ticket_count")
    private Integer ticketCount;

    @ApiModelProperty(value = "总金额")
    @TableField("total_amount")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "订单状态")
    @TableField("status")
    private String status;

    @ApiModelProperty(value = "支付方式")
    @TableField("payment_method")
    private String paymentMethod;

    @ApiModelProperty(value = "支付时间")
    @TableField("payment_time")
    private LocalDateTime paymentTime;

    @ApiModelProperty(value = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @ApiModelProperty(value = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @ApiModelProperty(value = "是否删除")
    @TableField("is_deleted")
    private Boolean deleted;

    @ApiModelProperty(value = "是否评分")
    @TableField("is_rated")
    private Boolean isRated;
}
