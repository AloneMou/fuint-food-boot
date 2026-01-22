package com.fuint.openapi.v1.goods.product.vo.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * Foot-Fuint-Backend-master
 *
 * @author mjw
 * @since 2026/1/23 0:25
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GoodsSkuRespVO extends GoodsSkuVO {

    @ApiModelProperty(value = "动态价格（元）", example = "18.00")
    private BigDecimal dynamicPrice;
}
