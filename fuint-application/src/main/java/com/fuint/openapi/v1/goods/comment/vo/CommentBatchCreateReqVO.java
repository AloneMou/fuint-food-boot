package com.fuint.openapi.v1.goods.comment.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.List;

/**
 * 批量评价创建请求VO
 */
@Data
public class CommentBatchCreateReqVO {

    @NotNull(message = "订单ID不能为空")
    @ApiModelProperty(value = "订单ID", required = true)
    private Integer orderId;

    @NotNull(message = "用户ID不能为空")
    @ApiModelProperty(value = "用户ID", required = true)
    private Integer userId;

    @Valid
    @NotEmpty(message = "商品评价列表不能为空")
    @ApiModelProperty(value = "商品评价列表", required = true)
    private List<GoodsItemCommentVO> goodsComments;

    @Valid
    @ApiModelProperty(value = "订单评价(NPS)")
    private OrderItemCommentVO orderComment;

//    @Valid
//    @ApiModelProperty(value = "价格评价")
//    private PriceItemCommentVO priceComment;
}
