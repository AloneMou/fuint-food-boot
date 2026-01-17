package com.fuint.openapi.v1.goods.product.vo.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 商品规格项VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "商品规格项VO")
public class GoodsSpecItemVO {

    @ApiModelProperty(value = "规格名称", example = "杯型")
    private String name;

    @ApiModelProperty(value = "规格值列表")
    private List<GoodsSpecChildVO> child;
}
