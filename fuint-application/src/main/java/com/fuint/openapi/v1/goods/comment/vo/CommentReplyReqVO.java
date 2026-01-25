package com.fuint.openapi.v1.goods.comment.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 商家回复评价请求VO
 */
@Data
public class CommentReplyReqVO {

    @NotNull(message = "评价ID不能为空")
    @ApiModelProperty(value = "评价ID", required = true)
    private Integer id;

    @NotBlank(message = "回复内容不能为空")
    @Size(max = 1000, message = "回复内容最多1000字")
    @ApiModelProperty(value = "回复内容", required = true)
    private String replyContent;

    @ApiModelProperty(value = "商户ID（用于权限验证）")
    private Integer merchantId;

}
