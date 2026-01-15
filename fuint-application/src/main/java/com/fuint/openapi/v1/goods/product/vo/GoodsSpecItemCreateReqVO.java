package com.fuint.openapi.v1.goods.product.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;


@Data
public class GoodsSpecItemCreateReqVO {

    @ApiModelProperty(value = "规格名称", example = "杯型")
    private String name;

    @ApiModelProperty(value = "规格值列表")
    private List<String> child;
}
