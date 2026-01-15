package com.fuint.openapi.v1.order.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 订单商品项VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "订单商品项VO")
public class OrderGoodsItemVO {

    @NotNull(message = "商品ID不能为空")
    @ApiModelProperty(value = "商品ID", required = true, example = "1")
    private Integer goodsId;

    @ApiModelProperty(value = "商品SKU ID", example = "1")
    private Integer skuId;

    @NotNull(message = "购买数量不能为空")
    @ApiModelProperty(value = "购买数量", required = true, example = "2")
    private Integer quantity;

    @ApiModelProperty(value = "商品单价（元）", example = "18.00")
    private BigDecimal unitPrice;
}
