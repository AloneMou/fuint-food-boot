package com.fuint.openapi.v1.order.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 评价列表查询请求VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "评价列表查询请求VO")
public class EvaluationListReqVO {

    @ApiModelProperty(value = "评价时间起", example = "2024-01-01 00:00:00")
    private String startTime;

    @ApiModelProperty(value = "评价时间止", example = "2024-12-31 23:59:59")
    private String endTime;

    @ApiModelProperty(value = "商品SKU ID列表")
    private List<Integer> skuIds;

    @ApiModelProperty(value = "页码", example = "1")
    private Integer page = 1;

    @ApiModelProperty(value = "每页大小", example = "10")
    private Integer pageSize = 10;
}
