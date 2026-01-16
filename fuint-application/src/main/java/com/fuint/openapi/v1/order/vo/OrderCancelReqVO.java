package com.fuint.openapi.v1.order.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 取消订单请求VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "取消订单请求VO")
public class OrderCancelReqVO {

    @NotNull(message = "订单ID不能为空")
    @ApiModelProperty(value = "订单ID", required = true, example = "1")
    private Integer orderId;

    @ApiModelProperty(value = "取消原因", example = "不想要了")
    private String remark;
}
