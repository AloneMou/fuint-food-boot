package com.fuint.openapi.v1.goods.cate.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 商品分类分页响应VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "商品分类分页响应VO")
public class MtGoodsCatePageRespVO {

    @ApiModelProperty(value = "分类列表")
    private List<MtGoodsCateRespVO> list;

    @ApiModelProperty(value = "总记录数", example = "100")
    private Long total;

    @ApiModelProperty(value = "总页数", example = "10")
    private Integer totalPages;

    @ApiModelProperty(value = "当前页码", example = "1")
    private Integer currentPage;

    @ApiModelProperty(value = "每页数量", example = "10")
    private Integer pageSize;
}
