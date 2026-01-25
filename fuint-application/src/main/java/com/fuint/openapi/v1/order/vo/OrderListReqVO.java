package com.fuint.openapi.v1.order.vo;

import cn.hutool.core.date.DatePattern;
import com.fuint.common.enums.*;
import com.fuint.framework.pojo.PageParams;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 订单列表查询请求VO
 * <p>
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

    @ApiModelProperty(value = "订单模式", allowableValues = "EXPRESS,ONESELF")
    private OrderModeEnum orderMode;

    @ApiModelProperty(value = "订单类型", example = "GOODS", allowableValues = "GOODS,PAYMENT,RECHARGE,PRESTORE,MEMBER")
    private OrderTypeEnum orderType;

    @ApiModelProperty(value = "订单状态", allowableValues = "CREATED,PAID,CANCEL,DELIVERY,DELIVERED,RECEIVED,DELETED,REFUND")
    private OrderStatusEnum status;

    @ApiModelProperty(value = "支付状态", example = "WAIT", allowableValues = "WAIT,SUCCESS")
    private PayStatusEnum payStatus;

    @ApiModelProperty(value = "支付方式", example = "CASH", allowableValues = "CASH,JSAPI,MICROPAY,BALANCE,ALISCAN,OPEN_API")
    private PayTypeEnum payType;

    @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    @ApiModelProperty(value = "下单时间起", example = "2024-01-01 00:00:00")
    private Date startTime;

    @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    @ApiModelProperty(value = "下单时间止", example = "2024-12-31 23:59:59")
    private String endTime;
}
