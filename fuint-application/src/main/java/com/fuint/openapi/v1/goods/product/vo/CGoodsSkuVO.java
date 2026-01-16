package com.fuint.openapi.v1.goods.product.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * C端商品SKU响应VO（包含动态价格）
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "C端商品SKU响应VO")
public class CGoodsSkuVO {

    @ApiModelProperty(value = "SKU ID", example = "1")
    private Integer skuId;

    @ApiModelProperty(value = "SKU编码", example = "SKU001")
    private String skuNo;

    @ApiModelProperty(value = "规格信息（JSON格式）", example = "{\"cupSize\":\"大杯\",\"temperature\":\"热\"}")
    private Map<String, String> specs;

    @ApiModelProperty(value = "动态价格（元）- 根据营销活动和用户优惠券计算", example = "15.50")
    private BigDecimal dynamicPrice;

    @ApiModelProperty(value = "划线价格/原价（元）", example = "18.00")
    private BigDecimal originalPrice;

    @ApiModelProperty(value = "库存数量", example = "100")
    private Integer stock;

    @ApiModelProperty(value = "SKU图片", example = "https://example.com/sku.jpg")
    private String logo;

    @ApiModelProperty(value = "状态：A-正常；D-删除", example = "A")
    private String status;
}
