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
 * 影厅表
 * </p>
 *
 * @author tjl
 * @since 2025-06-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("halls")
@ApiModel(value="Halls对象", description="影厅表")
public class Halls implements Serializable {

    private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "影厅ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

        @ApiModelProperty(value = "影厅名称")
    @TableField("hall_name")
    private String hallName;

        @ApiModelProperty(value = "总座位数")
    @TableField("total_seats")
    private Integer totalSeats;

        @ApiModelProperty(value = "总行数")
    @TableField("total_rows")
    private Integer totalRows;

        @ApiModelProperty(value = "总列数")
    @TableField("total_columns")
    private Integer totalColumns;

        @ApiModelProperty(value = "影厅类型")
    @TableField("hall_type")
    private String hallType;

        @ApiModelProperty(value = "状态")
    @TableField("status")
    private String status;

        @ApiModelProperty(value = "价格倍数，根据影厅类型计算")
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
