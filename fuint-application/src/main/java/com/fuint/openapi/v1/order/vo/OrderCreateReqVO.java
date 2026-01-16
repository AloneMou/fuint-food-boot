package com.fuint.openapi.v1.order.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 订单创建请求VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "订单创建请求VO")
public class OrderCreateReqVO extends OrderPreCreateReqVO {

    @NotNull(message = "预创建订单的总订单价格不能为空")
    @ApiModelProperty(value = "预创建订单的总订单价格（用于校验价格一致性）", required = true, example = "100.00")
    private BigDecimal preTotalAmount;

    @ApiModelProperty(value = "支付方式：BALANCE-余额支付；JSAPI-微信支付；ALISCAN-支付宝支付", example = "BALANCE")
    private String payType;
}
