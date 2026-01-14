package com.fuint.openapi.v1.goods.product.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 商品规格子项VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "商品规格子项VO")
public class GoodsSpecChildVO {

    @ApiModelProperty(value = "规格值ID", example = "1")
    private Integer id;

    @ApiModelProperty(value = "规格值名称", example = "大杯")
    private String name;

    @ApiModelProperty(value = "是否选中", example = "true")
    private Boolean checked;
}
