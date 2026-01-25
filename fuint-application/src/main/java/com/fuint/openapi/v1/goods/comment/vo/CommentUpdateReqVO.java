package com.fuint.openapi.v1.goods.comment.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;
import java.util.List;

/**
 * 更新评价请求VO
 */
@Data
public class CommentUpdateReqVO {

    @NotNull(message = "评价ID不能为空")
    @ApiModelProperty(value = "评价ID", required = true)
    private Integer id;

    @NotNull(message = "用户ID不能为空")
    @ApiModelProperty(value = "用户ID（用于权限验证）", required = true)
    private Integer userId;

    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    @ApiModelProperty(value = "评分(1-5星)")
    private Integer score;

    @Size(max = 1000, message = "评价内容最多1000字")
    @ApiModelProperty(value = "评价内容")
    private String content;

    @ApiModelProperty(value = "评价图片列表")
    private List<String> images;

    @ApiModelProperty(value = "是否匿名评价 Y-是 N-否")
    private String isAnonymous;

}
