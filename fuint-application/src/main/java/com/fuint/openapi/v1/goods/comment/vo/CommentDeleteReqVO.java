package com.fuint.openapi.v1.goods.comment.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 删除评价请求VO
 */
@Data
public class CommentDeleteReqVO {

    @NotNull(message = "评价ID不能为空")
    @ApiModelProperty(value = "评价ID", required = true)
    private Integer id;

    @NotNull(message = "用户ID不能为空")
    @ApiModelProperty(value = "用户ID（用于权限验证）", required = true)
    private Integer userId;

}
