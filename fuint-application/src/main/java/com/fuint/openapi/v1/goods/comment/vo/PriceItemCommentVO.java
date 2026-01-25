package com.fuint.openapi.v1.goods.comment.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;

/**
 * 价格评价项请求VO
 */
@Data
public class PriceItemCommentVO {

    @NotNull(message = "价格评分不能为空")
    @Min(value = 1, message = "评分最小为1星")
    @Max(value = 5, message = "评分最大为5星")
    @ApiModelProperty(value = "价格评分(1-5星)", required = true)
    private Integer score;

    @Size(max = 1000, message = "评价内容最多1000字")
    @ApiModelProperty(value = "评价内容")
    private String content;

    @ApiModelProperty(value = "是否匿名评价 Y-是 N-否")
    private String isAnonymous;
}
