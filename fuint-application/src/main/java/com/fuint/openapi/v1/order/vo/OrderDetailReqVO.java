package com.fuint.openapi.v1.order.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 订单详情查询请求VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "订单详情查询请求VO")
public class OrderDetailReqVO {

    @ApiModelProperty(value = "订单ID", required = true, example = "1")
    @NotNull(message = "订单ID不能为空")
    private Integer orderId;

    @ApiModelProperty(value = "用户ID（用于权限验证）", example = "1")
    private Integer userId;

    @ApiModelProperty(value = "商户ID（用于权限验证）", example = "1")
    private Integer merchantId;
}
