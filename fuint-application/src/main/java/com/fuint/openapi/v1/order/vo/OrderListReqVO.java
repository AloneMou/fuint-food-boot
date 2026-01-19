package com.fuint.openapi.v1.order.vo;

import com.fuint.framework.pojo.PageParams;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订单列表查询请求VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "订单列表查询请求VO")
public class OrderListReqVO extends PageParams {

    @ApiModelProperty(value = "用户ID", example = "1")
    private Integer userId;

    @ApiModelProperty(value = "商户ID", example = "1")
    private Integer merchantId;

    @ApiModelProperty(value = "店铺ID", example = "1")
    private Integer storeId;

    @ApiModelProperty(value = "商品名称（模糊查询）", example = "咖啡")
    private String goodsName;

    @ApiModelProperty(value = "订单状态", example = "A")
    private String status;

    @ApiModelProperty(value = "支付状态", example = "A")
    private String payStatus;

    @ApiModelProperty(value = "下单时间起", example = "2024-01-01 00:00:00")
    private String startTime;

    @ApiModelProperty(value = "下单时间止", example = "2024-12-31 23:59:59")
    private String endTime;
}
