package com.fuint.openapi.v1.goods.comment.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 评价统计信息VO
 */
@Data
public class CommentStatisticsVO {

    @ApiModelProperty(value = "商品ID")
    private Integer goodsId;

    @ApiModelProperty(value = "评价总数")
    private Integer totalCount;

    @ApiModelProperty(value = "平均评分")
    private BigDecimal avgScore;

    @ApiModelProperty(value = "好评数（4-5星）")
    private Integer goodCount;

    @ApiModelProperty(value = "中评数（3星）")
    private Integer normalCount;

    @ApiModelProperty(value = "差评数（1-2星）")
    private Integer badCount;

    @ApiModelProperty(value = "好评率（百分比）")
    private BigDecimal goodRate;

    @ApiModelProperty(value = "5星数量")
    private Integer star5Count;

    @ApiModelProperty(value = "4星数量")
    private Integer star4Count;

    @ApiModelProperty(value = "3星数量")
    private Integer star3Count;

    @ApiModelProperty(value = "2星数量")
    private Integer star2Count;

    @ApiModelProperty(value = "1星数量")
    private Integer star1Count;

    @ApiModelProperty(value = "订单NPS总评价数")
    private Integer npsTotalCount;

    @ApiModelProperty(value = "订单NPS平均评分")
    private BigDecimal npsAvgScore;

    @ApiModelProperty(value = "订单NPS推荐者数量(9-10分)")
    private Integer npsPromoterCount;

    @ApiModelProperty(value = "订单NPS被动者数量(7-8分)")
    private Integer npsPassiveCount;

    @ApiModelProperty(value = "订单NPS贬损者数量(0-6分)")
    private Integer npsDetractorCount;

    @ApiModelProperty(value = "NPS净推荐值")
    private BigDecimal npsScore;

}
