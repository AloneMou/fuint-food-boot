package com.fuint.openapi.v1.order.vo;

import com.fuint.framework.pojo.PageParams;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 订单评价分页查询请求VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "订单评价分页查询请求VO")
public class EvaluationPageReqVO extends PageParams {

    @ApiModelProperty(value = "开始时间", example = "2024-01-01 00:00:00")
    private String startTime;

    @ApiModelProperty(value = "结束时间", example = "2024-12-31 23:59:59")
    private String endTime;

    @ApiModelProperty(value = "SKU ID列表（用于筛选包含指定SKU的订单评价）", example = "[1, 2, 3]")
    private List<Integer> skuIds;
}
