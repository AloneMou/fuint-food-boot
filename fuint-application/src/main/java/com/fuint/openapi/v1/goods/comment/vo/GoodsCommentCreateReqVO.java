package com.fuint.openapi.v1.goods.comment.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;
import java.util.List;

/**
 * 商品评价创建请求VO
 */
@Data
public class GoodsCommentCreateReqVO {

    @NotNull(message = "订单ID不能为空")
    @ApiModelProperty(value = "订单ID", required = true)
    private Integer orderId;

    @NotNull(message = "商品ID不能为空")
    @ApiModelProperty(value = "商品ID", required = true)
    private Integer goodsId;

    @ApiModelProperty(value = "SKU ID")
    private Integer skuId;

    @NotNull(message = "用户ID不能为空")
    @ApiModelProperty(value = "用户ID", required = true)
    private Integer userId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最小为1星")
    @Max(value = 5, message = "评分最大为5星")
    @ApiModelProperty(value = "商品评分(1-5星)", required = true)
    private Integer score;

    @Size(max = 1000, message = "评价内容最多1000字")
    @ApiModelProperty(value = "评价内容")
    private String content;

    @ApiModelProperty(value = "评价图片列表")
    private List<String> images;

    @ApiModelProperty(value = "是否匿名评价 Y-是 N-否")
    private String isAnonymous;
}
