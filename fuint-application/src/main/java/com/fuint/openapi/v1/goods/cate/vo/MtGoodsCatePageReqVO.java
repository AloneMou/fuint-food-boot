package com.fuint.openapi.v1.goods.cate.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;

/**
 * 商品分类分页查询请求VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "商品分类分页查询请求VO")
public class MtGoodsCatePageReqVO {

    @ApiModelProperty(value = "当前页码", example = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @ApiModelProperty(value = "每页数量", example = "10")
    @Min(value = 1, message = "每页数量最小为1")
    private Integer pageSize = 10;

    @ApiModelProperty(value = "分类名称（模糊搜索）", example = "饮品")
    private String name;

    @ApiModelProperty(value = "状态：A-正常；D-删除", example = "A")
    private String status;

    @ApiModelProperty(value = "店铺ID", example = "1")
    private Integer storeId;

    @ApiModelProperty(value = "商户ID", example = "1")
    private Integer merchantId;
}
