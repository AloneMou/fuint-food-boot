package com.fuint.openapi.v1.goods.product.vo.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Foot-Fuint-Backend-master
 *
 * @author mjw
 * @since 2026/1/17 1:30
 */
@Data
public class C_GoodsSpecVO {

    @ApiModelProperty(value = "规格ID", example = "1")
    private Integer id;

    @ApiModelProperty(value = "规格名称", example = "cupSize")
    private String name;

    @ApiModelProperty(value = "规格值", example = "大杯")
    private List<C_GoodsSpecValueVO> valueLs;

    @Data
    public static class C_GoodsSpecValueVO {

        @ApiModelProperty(value = "规格值ID", example = "1")
        private Integer id;

        @ApiModelProperty(value = "规格值名称", example = "大杯")
        private String name;
    }
}
