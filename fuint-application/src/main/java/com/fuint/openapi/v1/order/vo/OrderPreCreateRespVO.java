package com.fuint.openapi.v1.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 订单预创建响应VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "订单预创建响应VO")
public class OrderPreCreateRespVO {

    @ApiModelProperty(value = "订单总金额（元）", example = "36.00")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "优惠金额（元）", example = "5.00")
    private BigDecimal discountAmount;

    @ApiModelProperty(value = "积分抵扣金额（元）", example = "1.00")
    private BigDecimal pointAmount;

    @ApiModelProperty(value = "配送费（元）", example = "0.00")
    private BigDecimal deliveryFee;

    @ApiModelProperty(value = "应付金额（元）", example = "30.00")
    private BigDecimal payableAmount;

    @ApiModelProperty(value = "使用积分数量", example = "100")
    private Integer usePoint;

    @ApiModelProperty(value = "当前用户可用积分", example = "500")
    private Integer availablePoint;

    @ApiModelProperty(value = "适用的用户优惠券列表")
    private List<AvailableCouponVO> availableCoupons;

    @ApiModelProperty(value = "当前选中的优惠券ID", example = "1")
    private Integer selectedCouponId;

    @ApiModelProperty(value = "订单商品列表")
    private List<OrderGoodsDetailVO> goodsList;

    @ApiModelProperty(value = "计算时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date calculateTime;
}
