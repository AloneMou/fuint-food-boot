package com.fuint.openapi.v1.goods.product.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品SKU VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "商品SKU VO")
public class GoodsSkuVO {

    @ApiModelProperty(value = "SKU ID", example = "1")
    private Integer id;

    @ApiModelProperty(value = "SKU编码", example = "SKU001")
    private String skuNo;

    @ApiModelProperty(value = "商品ID", example = "1")
    private Integer goodsId;

    @ApiModelProperty(value = "规格ID组合，用-分隔", example = "1-2-3")
    private String specIds;

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

    @ApiModelProperty(value = "状态：A-正常；D-删除", example = "A")
    private String status;
}
