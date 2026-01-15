package com.fuint.openapi.v1.goods.product.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Foot-Fuint-Backend-master
 *
 * @author mjw
 * @since 2026/1/15 23:30
 */
@Data
public class GoodsSkuCreateReqVO {

    @ApiModelProperty(value = "SKU编码", example = "SKU001")
    private String skuNo;

    @ApiModelProperty(value = "规格名称，用^分隔", example = "冰^500ml^40")
    private List<Spec> specLs;


    @ApiModelProperty(value = "SKU图片", example = "https://example.com/sku.jpg")
    private String logo;

    @ApiModelProperty(value = "价格（元）", example = "18.00")
    private BigDecimal price;

    @ApiModelProperty(value = "划线价格（元）", example = "20.00")
    private BigDecimal linePrice;

    @ApiModelProperty(value = "重量（克）", example = "500")
    private BigDecimal weight;

    @ApiModelProperty(value = "库存", example = "100")
    private Integer stock;

    @Data
    public static class Spec {

        @ApiModelProperty(value = "规格名称", example = "冰")
        private String name;

        @ApiModelProperty(value = "规格值", example = "500ml")
        private String value;
    }
}
