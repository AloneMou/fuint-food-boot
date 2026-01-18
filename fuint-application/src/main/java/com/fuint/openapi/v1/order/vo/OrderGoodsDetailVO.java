package com.fuint.openapi.v1.order.vo;

import com.fuint.common.dto.GoodsSpecValueDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单商品明细VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "订单商品明细VO")
public class OrderGoodsDetailVO {

    @ApiModelProperty(value = "商品ID", example = "1")
    private Integer goodsId;

    @ApiModelProperty(value = "商品名称", example = "拿铁咖啡")
    private String goodsName;

    @ApiModelProperty(value = "商品SKU ID", example = "1")
    private Integer skuId;

//    @ApiModelProperty(value = "商品规格", example = "大杯")
//    private String skuName;

    @ApiModelProperty(value = "商品图片", example = "https://example.com/image.jpg")
    private String goodsImage;

    @ApiModelProperty(value = "购买数量", example = "2")
    private Integer quantity;

    @ApiModelProperty(value = "商品单价（元）", example = "18.00")
    private BigDecimal price;

    @ApiModelProperty(value = "商品原价（元）", example = "20.00")
    private BigDecimal linePrice;

    @ApiModelProperty(value = "小计金额（元）", example = "36.00")
    private BigDecimal subtotal;

    @ApiModelProperty(value = "会员折扣金额（元）", example = "2.00")
    private BigDecimal memberDiscount;

    @ApiModelProperty("商品规格")
    private List<GoodsSpecValueDto> specList;

    @ApiModelProperty("是否有效")
    private Boolean isEffect;

}
