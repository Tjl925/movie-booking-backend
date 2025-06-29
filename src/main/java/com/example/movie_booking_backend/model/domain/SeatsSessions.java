package com.example.movie_booking_backend.model.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 座位场次关联表
 * </p>
 *
 * @author tjl
 * @since 2025-06-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("seats_sessions")
@ApiModel(value="SeatsSessions对象", description="座位场次关联表")
public class SeatsSessions implements Serializable {

    private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

        @ApiModelProperty(value = "座位ID")
    @TableField("seat_id")
    private Long seatId;

        @ApiModelProperty(value = "场次ID")
    @TableField("session_id")
    private Long sessionId;

        @ApiModelProperty(value = "状态")
    @TableField("status")
    private String status;

        @ApiModelProperty(value = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

        @ApiModelProperty(value = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;

        @ApiModelProperty(value = "是否删除")
    @TableField("is_deleted")
        @TableLogic
    private Boolean deleted;


}
