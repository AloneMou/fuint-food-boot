package com.fuint.openapi.v1.order.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 支付订单请求VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "支付订单请求VO")
public class OrderPayReqVO {

    @NotNull(message = "订单ID不能为空")
    @ApiModelProperty(value = "订单ID", required = true, example = "1")
    private Integer orderId;

    @ApiModelProperty(value = "用户ID（用于权限验证）", example = "1")
    private Integer userId;

    @NotNull(message = "支付金额不能为空")
    @ApiModelProperty(value = "支付金额", required = true, example = "100.00")
    private BigDecimal payAmount;

    @ApiModelProperty(value = "支付方式：BALANCE-余额支付；JSAPI-微信支付；ALISCAN-支付宝支付", example = "BALANCE")
    private String payType;
}
