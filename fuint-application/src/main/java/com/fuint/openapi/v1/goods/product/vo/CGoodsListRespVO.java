package com.fuint.openapi.v1.goods.product.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * C端商品列表响应VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "C端商品列表响应VO")
public class CGoodsListRespVO {

    @ApiModelProperty(value = "商品ID", example = "1")
    private Integer goodsId;

    @ApiModelProperty(value = "商品名称", example = "拿铁咖啡")
    private String name;

    @ApiModelProperty(value = "商品描述", example = "经典拿铁")
    private String description;

    @ApiModelProperty(value = "商品图片", example = "https://example.com/logo.jpg")
    private String imageUrl;

    @ApiModelProperty(value = "状态：A-上架；D-下架", example = "A")
    private String status;

    @ApiModelProperty(value = "商品SKU列表（包含动态价格）")
    private List<CGoodsSkuVO> skus;

    @ApiModelProperty(value = "单规格商品的动态价格（当isSingleSpec=true时使用）", example = "15.50")
    private BigDecimal dynamicPrice;

    @ApiModelProperty(value = "单规格商品的划线价格（当isSingleSpec=true时使用）", example = "18.00")
    private BigDecimal originalPrice;

    @ApiModelProperty(value = "单规格商品的库存（当isSingleSpec=true时使用）", example = "100")
    private Integer stock;
}
