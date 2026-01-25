package com.fuint.openapi.v1.goods.comment.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;

/**
 * 订单评价(NPS)创建请求VO
 */
@Data
public class OrderCommentCreateReqVO {

    @NotNull(message = "订单ID不能为空")
    @ApiModelProperty(value = "订单ID", required = true)
    private Integer orderId;

    @NotNull(message = "用户ID不能为空")
    @ApiModelProperty(value = "用户ID", required = true)
    private Integer userId;

    @NotNull(message = "NPS评分不能为空")
    @Min(value = 0, message = "评分最小为0分")
    @Max(value = 10, message = "评分最大为10分")
    @ApiModelProperty(value = "NPS评分(0-10分)", required = true)
    private Integer score;

    @Size(max = 1000, message = "评价内容最多1000字")
    @ApiModelProperty(value = "评价内容")
    private String content;

    @ApiModelProperty(value = "是否匿名评价 Y-是 N-否")
    private String isAnonymous;
}
