package com.fuint.openapi.v1.order.vo;

import cn.hutool.core.date.DatePattern;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fuint.common.dto.OrderGoodsDto;
import com.fuint.common.dto.UserCouponDto;
import com.fuint.common.enums.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Foot-Fuint-Backend-master
 *
 * @author mjw
 * @since 2026/1/22 22:58
 */
@Data
public class UserOrderSimpleRespVO {

    @ApiModelProperty("自增ID")
    private Integer id;

    @ApiModelProperty(value = "取餐状态", allowableValues = "WAIT_CONFIRM,CONFIRM_SUCCESS,MAKING,MAKE_SUCCESS")
    private TakeStatusEnum takeStatus;

    @ApiModelProperty("商户ID")
    private Integer merchantId;

    @ApiModelProperty("订单号")
    private String orderSn;

    @ApiModelProperty(value = "订单类型", allowableValues = "GOODS,PAYMENT,RECHARGE,PRESTORE,MEMBER")
    private OrderTypeEnum type;

    @ApiModelProperty("订单类型名称")
    private String typeName;

    @ApiModelProperty(value = "支付类型", allowableValues = "CASH,JSAPI,MICROPAY,BALANCE,ALISCAN,OPEN_API")
    private PayTypeEnum payType;

    @ApiModelProperty(value = "订单模式", allowableValues = "EXPRESS,ONESELF")
    private OrderModeEnum orderMode;

    @ApiModelProperty("是否核销")
    private Boolean isVerify;

    @ApiModelProperty("卡券ID")
    private Integer couponId;

    @ApiModelProperty("会员ID")
    private Integer userId;

    @ApiModelProperty("是否游客")
    private String isVisitor;

    @ApiModelProperty("核销码")
    private String verifyCode;

    @ApiModelProperty("员工ID")
    private Integer staffId;

    @ApiModelProperty("总金额")
    private BigDecimal amount;

    @ApiModelProperty("支付金额")
    private BigDecimal payAmount;

    @ApiModelProperty("优惠金额")
    private BigDecimal discount;

    @ApiModelProperty("配送费用")
    private BigDecimal deliveryFee;

    @ApiModelProperty("使用积分")
    private Integer usePoint;

    @ApiModelProperty("积分金额")
    private BigDecimal pointAmount;

    @ApiModelProperty("订单参数")
    private String param;

    @ApiModelProperty("备注信息")
    private String remark;

    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN, timezone = "GMT+8")
    @ApiModelProperty("创建时间")
    private Date createTime;

    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN, timezone = "GMT+8")
    @ApiModelProperty("更新时间")
    private Date updateTime;

    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN, timezone = "GMT+8")
    @ApiModelProperty("支付时间")
    private Date payTime;

    @ApiModelProperty(value = "订单状态", allowableValues = "CREATED,PAID,CANCEL,DELIVERY,DELIVERED,RECEIVED,DELETED,REFUND")
    private OrderStatusEnum status;

    @ApiModelProperty(value = "支付状态", allowableValues = "WAIT,SUCCESS")
    private PayStatusEnum payStatus;

    @ApiModelProperty(value = "结算状态", allowableValues = "WAIT,COMPLETE")
    private SettleStatusEnum settleStatus;

    @ApiModelProperty("状态说明")
    private String statusText;

    @ApiModelProperty("最后操作人")
    private String operator;

    @ApiModelProperty("订单商品列表")
    private List<OrderGoodsDto> goods;

    @ApiModelProperty("下单用户信息")
    private UserOrderRespVO.OrderUserRespVO userInfo;

    @ApiModelProperty("配送地址")
    private UserOrderRespVO.AddressRespVO address;

    @ApiModelProperty("物流信息")
    private UserOrderRespVO.ExpressRespVO expressInfo;

    @ApiModelProperty("所属店铺信息")
    private UserOrderRespVO.OrderStoreRespVO storeInfo;

    @ApiModelProperty("所属桌码信息")
    private UserOrderRespVO.OrderTableRespVO tableInfo;

    @ApiModelProperty("售后订单")
    private UserOrderRespVO.OrderRefundRespVO refundInfo;

    @ApiModelProperty("使用卡券")
    private UserCouponDto couponInfo;

    @ApiModelProperty("所属员工")
    private UserOrderRespVO.OrderStaffRespVO staffInfo;
}
