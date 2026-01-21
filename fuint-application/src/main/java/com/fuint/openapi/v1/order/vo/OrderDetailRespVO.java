package com.fuint.openapi.v1.order.vo;

import com.fuint.common.dto.UserOrderDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 订单详情响应VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "订单详情响应VO")
public class OrderDetailRespVO {

    @ApiModelProperty(value = "订单信息")
    private UserOrderDto order;

    @ApiModelProperty(value = "排队数量")
    private Integer queueCount;

    @ApiModelProperty(value = "预计等待时间（分钟）")
    private Integer estimatedWaitTime;
}
