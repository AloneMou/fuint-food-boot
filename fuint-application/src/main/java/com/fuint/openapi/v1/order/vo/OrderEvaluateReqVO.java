package com.fuint.openapi.v1.order.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 订单评价请求VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "订单评价请求VO")
public class OrderEvaluateReqVO {

    @NotNull(message = "订单ID不能为空")
    @ApiModelProperty(value = "订单ID", required = true, example = "1")
    private Integer orderId;

    @NotNull(message = "评分不能为空")
    @Min(value = 0, message = "评分不能小于0")
    @Max(value = 10, message = "评分不能大于10")
    @ApiModelProperty(value = "NPS评分（0-10分）", required = true, example = "10")
    private Integer score;

    @ApiModelProperty(value = "评价内容", example = "非常好喝")
    private String comment;
}
