package com.fuint.openapi.v1.goods.comment.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;
import java.util.List;

/**
 * 创建评价请求VO
 */
@Data
public class CommentCreateReqVO {

    @NotNull(message = "订单ID不能为空")
    @ApiModelProperty(value = "订单ID", required = true)
    private Integer orderId;

    @ApiModelProperty(value = "商品ID（商品评价时必填）")
    private Integer goodsId;

    @ApiModelProperty(value = "SKU ID")
    private Integer skuId;

    @NotNull(message = "用户ID不能为空")
    @ApiModelProperty(value = "用户ID", required = true)
    private Integer userId;

    @ApiModelProperty(value = "评价类型: 1-商品评价 2-订单NPS评价，默认1")
    private Integer commentType;

    @NotNull(message = "评分不能为空")
    @Min(value = 0, message = "评分最小为0")
    @Max(value = 10, message = "评分最大为10")
    @ApiModelProperty(value = "评分(1-5星或0-10分)", required = true)
    private Integer score;

    @Size(max = 1000, message = "评价内容最多1000字")
    @ApiModelProperty(value = "评价内容")
    private String content;

    @ApiModelProperty(value = "评价图片列表")
    private List<String> images;

    @ApiModelProperty(value = "是否匿名评价 Y-是 N-否，默认N")
    private String isAnonymous;

    @ApiModelProperty(value = "商户ID")
    private Integer merchantId;

    @ApiModelProperty(value = "店铺ID")
    private Integer storeId;

}
