package com.example.movie_booking_backend.model.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
 * 操作日志表
 * </p>
 *
 * @author tjl
 * @since 2025-06-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("operation_logs")
@ApiModel(value = "OperationLogs对象", description = "操作日志表")
public class OperationLogs implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "日志ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "操作用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "操作用户名")
    @TableField("username")
    private String username;

    @ApiModelProperty(value = "操作类型")
    @TableField("operation")
    private String operation;

    @ApiModelProperty(value = "操作资源")
    @TableField("resource")
    private String resource;

    @ApiModelProperty(value = "资源ID")
    @TableField("resource_id")
    private Long resourceId;

    @ApiModelProperty(value = "操作描述")
    @TableField("description")
    private String description;

    @ApiModelProperty(value = "IP地址")
    @TableField("ip_address")
    private String ipAddress;

    @ApiModelProperty(value = "用户代理")
    @TableField("user_agent")
    private String userAgent;

    @ApiModelProperty(value = "请求URL")
    @TableField("request_url")
    private String requestUrl;

    @ApiModelProperty(value = "请求方法")
    @TableField("request_method")
    private String requestMethod;

    @ApiModelProperty(value = "请求参数")
    @TableField("request_params")
    private String requestParams;

    @ApiModelProperty(value = "响应状态码")
    @TableField("response_status")
    private Integer responseStatus;

    @ApiModelProperty(value = "执行时间(毫秒)")
    @TableField("execution_time")
    private Long executionTime;

    @ApiModelProperty(value = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;


}
