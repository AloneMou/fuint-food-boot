package com.fuint.openapi.v1.marketing.coupon.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 优惠券商品项VO
 *
 * @author mjw
 * @since 2026/1/17
 */
@Data
@ApiModel(value = "优惠券商品项VO")
public class CouponGoodsItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "商品ID不能为空")
    @ApiModelProperty(value = "商品ID", required = true, example = "1")
    private Integer goodsId;

    @ApiModelProperty(value = "商品数量", example = "1")
    private Integer quantity;

    @ApiModelProperty(value = "商品名称", example = "商品A")
    private String goodsName;
}
