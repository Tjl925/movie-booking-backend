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
 * 支付记录表
 * </p>
 *
 * @author tjl
 * @since 2025-06-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("payments")
@ApiModel(value = "Payments对象", description = "支付记录表")
public class Payments implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "支付ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "订单ID")
    @TableField("order_id")
    private Long orderId;

    @ApiModelProperty(value = "用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "支付方式")
    @TableField("payment_method")
    private String paymentMethod;

    @ApiModelProperty(value = "支付金额")
    @TableField("payment_amount")
    private BigDecimal paymentAmount;

    @ApiModelProperty(value = "交易ID")
    @TableField("transaction_id")
    private String transactionId;

    @ApiModelProperty(value = "支付状态")
    @TableField("payment_status")
    private String paymentStatus;

    @ApiModelProperty(value = "支付时间")
    @TableField("payment_time")
    private LocalDateTime paymentTime;

    @ApiModelProperty(value = "退款金额")
    @TableField("refund_amount")
    private BigDecimal refundAmount;

    @ApiModelProperty(value = "退款时间")
    @TableField("refund_time")
    private LocalDateTime refundTime;

    @ApiModelProperty(value = "退款原因")
    @TableField("refund_reason")
    private String refundReason;

    @ApiModelProperty(value = "网关响应")
    @TableField("gateway_response")
    private String gatewayResponse;

    @ApiModelProperty(value = "网关代码")
    @TableField("gateway_code")
    private String gatewayCode;

    @ApiModelProperty(value = "网关消息")
    @TableField("gateway_message")
    private String gatewayMessage;

    @ApiModelProperty(value = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @ApiModelProperty(value = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @ApiModelProperty(value = "是否删除")
    @TableField("is_deleted")
    private Boolean deleted;


}
