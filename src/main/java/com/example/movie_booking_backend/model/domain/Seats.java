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
 * 座位表
 * </p>
 *
 * @author tjl
 * @since 2025-06-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("seats")
@ApiModel(value="Seats对象", description="座位表")
public class Seats implements Serializable {

    private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "座位ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

        @ApiModelProperty(value = "影厅ID")
    @TableField("hall_id")
    private Long hallId;

        @ApiModelProperty(value = "座位号")
    @TableField("seat_number")
    private String seatNumber;

        @ApiModelProperty(value = "行号")
    @TableField("seat_row")
    private Integer seatRow;

        @ApiModelProperty(value = "列号")
    @TableField("seat_column")
    private Integer seatColumn;

        @ApiModelProperty(value = "座位类型")
    @TableField("seat_type")
    private String seatType;

        @ApiModelProperty(value = "状态")
    @TableField("status")
    private String status;

        @ApiModelProperty(value = "价格倍数，根据座位类型计算")
    @TableField("price_multiplier")
    private BigDecimal priceMultiplier;

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
