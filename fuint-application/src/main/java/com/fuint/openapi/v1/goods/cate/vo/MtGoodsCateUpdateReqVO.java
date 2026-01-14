package com.fuint.openapi.v1.goods.cate.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 商品分类更新请求VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "商品分类更新请求VO")
public class MtGoodsCateUpdateReqVO {

    @NotNull(message = "分类ID不能为空")
    @ApiModelProperty(value = "分类ID", required = true, example = "1")
    private Integer id;

    @ApiModelProperty(value = "分类名称", example = "饮品")
    private String name;

    @ApiModelProperty(value = "LOGO地址", example = "/uploads/logo.jpg")
    private String logo;

    @ApiModelProperty(value = "分类描述", example = "各类饮品")
    private String description;

    @ApiModelProperty(value = "排序", example = "100")
    private Integer sort;

    @ApiModelProperty(value = "状态：A-正常；D-删除", example = "A")
    private String status;
}
