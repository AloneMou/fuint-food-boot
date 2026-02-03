package com.fuint.openapi.v1.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fuint.common.dto.UserCouponDto;
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

//    @ApiModelProperty(value = "订单总金额（元）", example = "36.00")
//    private BigDecimal totalAmount;

    @ApiModelProperty(value = "订单金额（与创建订单字段一致）（元）", example = "36.00")
    private BigDecimal amount;

//    @ApiModelProperty(value = "优惠金额（元）", example = "5.00")
//    private BigDecimal discountAmount;

    @ApiModelProperty(value = "优惠金额（与创建订单字段一致）（元）", example = "5.00")
    private BigDecimal discount;

    @ApiModelProperty(value = "积分抵扣金额（元）", example = "1.00")
    private BigDecimal pointAmount;

    @ApiModelProperty(value = "配送费（元）", example = "0.00")
    private BigDecimal deliveryFee;

/*    @ApiModelProperty(value = "应付金额（元）", example = "30.00")
    private BigDecimal payableAmount;*/

    @ApiModelProperty(value = "实付金额（与创建订单字段一致）（元）", example = "30.00")
    private BigDecimal payAmount;

    @ApiModelProperty(value = "使用积分数量", example = "100")
    private Integer usePoint;

    @ApiModelProperty(value = "当前用户可用积分", example = "500")
    private Integer availablePoint;

    @ApiModelProperty(value = "适用的用户优惠券列表")
    private List<AvailableCouponVO> availableCoupons;

    @ApiModelProperty(value = "当前选中的优惠券ID", example = "1")
    private Integer selectedCouponId;

    @ApiModelProperty(value = "使用的优惠券ID（与创建订单字段一致）", example = "1")
    private Integer couponId;



//    @ApiModelProperty(value = "订单商品列表")
//    private List<OrderGoodsDetailVO> goodsList;
    @ApiModelProperty("订单商品列表")
    private List<UserOrderRespVO.OrderGoodsRespVO> goods;

    @ApiModelProperty(value = "计算时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date calculateTime;

//    @ApiModelProperty(value = "会员折扣金额（元）", example = "2.00")
//    private BigDecimal memberDiscountAmount;

    @ApiModelProperty(value = "商品总数量", example = "5")
    private Integer totalQuantity;

    @ApiModelProperty(value = "订单模式：express-配送；oneself-自取", example = "oneself")
    private String orderMode;

    @ApiModelProperty(value = "店铺ID", example = "1")
    private Integer storeId;

    @ApiModelProperty(value = "配送起送金额（元）", example = "10.00")
    private BigDecimal deliveryMinAmount;

    @ApiModelProperty("前面还有多少杯")
    private Integer queueCount;

    @ApiModelProperty("预计取餐时间/分钟")
    private Integer estimatedWaitTime;


    @ApiModelProperty("下单用户信息")
    private UserOrderRespVO.OrderUserRespVO userInfo;

    @ApiModelProperty("所属店铺信息")
    private UserOrderRespVO.OrderStoreRespVO storeInfo;

    @ApiModelProperty("使用卡券")
    private UserCouponDto couponInfo;
}
