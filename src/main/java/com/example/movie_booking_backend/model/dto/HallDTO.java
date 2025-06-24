package com.example.movie_booking_backend.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@ApiModel(value = "影厅请求", description = "创建或更新影厅的请求参数")
public class HallDTO {

    @NotBlank(message = "影厅名称不能为空")
    @ApiModelProperty(value = "影厅名称", required = true, example = "1号杜比全景声厅")
    private String name;

    @NotNull(message = "总行数不能为空")
    @Min(value = 1, message = "总行数至少为1")
    @ApiModelProperty(value = "总行数", required = true, example = "10")
    private Integer totalRows;

    @NotNull(message = "总列数不能为空")
    @Min(value = 1, message = "总列数至少为1")
    @ApiModelProperty(value = "总列数", required = true, example = "15")
    private Integer totalColumns;

    @ApiModelProperty(value = "影厅类型", example = "IMAX")
    private String type;

    @ApiModelProperty(value = "状态 (AVAILABLE, MAINTENANCE)")
    private String status;
}
