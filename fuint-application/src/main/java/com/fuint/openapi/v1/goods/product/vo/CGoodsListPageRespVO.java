package com.fuint.openapi.v1.goods.product.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * C端商品列表分页响应VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "C端商品列表分页响应VO")
public class CGoodsListPageRespVO {

    @ApiModelProperty(value = "当前页码", example = "1")
    private Integer pageNo;

    @ApiModelProperty(value = "每页数量", example = "20")
    private Integer pageSize;

    @ApiModelProperty(value = "总记录数", example = "100")
    private Long totalCount;

    @ApiModelProperty(value = "总页数", example = "5")
    private Integer totalPages;

    @ApiModelProperty(value = "商品列表")
    private List<CGoodsListRespVO> items;
}
