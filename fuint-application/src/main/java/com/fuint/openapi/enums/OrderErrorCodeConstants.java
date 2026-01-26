package com.fuint.openapi.enums;

import com.fuint.framework.exception.ErrorCode;

/**
 * Foot-Fuint-Backend-master
 *
 * @author mjw
 * @since 2026/1/18 15:04
 */
public interface OrderErrorCodeConstants {
    //订单不存在
    ErrorCode ORDER_NOT_FOUND = new ErrorCode(100_7_001, "订单不存在");
    //商品不能为空
    ErrorCode GOODS_NOT_EMPTY = new ErrorCode(100_7_002, "订单商品不能为空");
    //商品不属于当前门店或公共商品
    ErrorCode GOODS_NOT_BELONG_TO_STORE = new ErrorCode(100_7_003, "商品({})不属于当前门店或公共商品");


    //无权操作该订单
    ErrorCode ORDER_NOT_ALLOW_OPERATE = new ErrorCode(100_7_004, "无权操作该订单");
    //该售后订单已存在，请查询售后订单列表！
    ErrorCode REFUND_ORDER_EXIST = new ErrorCode(100_7_005, "该订单已存在售后，请查询售后订单列表！");
    //退款金额不能大于实际支付金额
    ErrorCode REFUND_AMOUNT_NOT_GREATER_THAN_PAY_AMOUNT = new ErrorCode(100_7_006, "退款金额不能大于实际支付金额");
    //取消订单异常，退款失败
    ErrorCode CANCEL_ORDER_ERROR = new ErrorCode(100_7_007, "取消订单异常，退款失败");
    //取消订单异常，请联系管理员
    ErrorCode CANCEL_ORDER_ERROR_CONTACT_ADMIN = new ErrorCode(100_7_008, "取消订单异常，请联系管理员");
    //订单取消处理中
    ErrorCode ORDER_CANCEL_PROCESSING = new ErrorCode(100_7_009, "订单取消处理中");
    //下单店铺与桌码不一致
    ErrorCode ORDER_STORE_NOT_EQUAL_TO_DESK = new ErrorCode(100_7_010, "下单店铺与桌码不一致");

    //系统已关闭交易功能，请稍后再试！
    ErrorCode SYSTEM_CLOSED_TRANSACTION = new ErrorCode(100_7_011, "系统已关闭交易功能，请稍后再试！");

    //ORDER_USE_POINT_NOT_ALLOWED
    ErrorCode ORDER_USE_POINT_NOT_ALLOWED = new ErrorCode(100_7_012, "订单使用积分功能未启用");
    //USER_COUPON_NOT_BELONG_TO_USER
    ErrorCode USER_COUPON_NOT_BELONG_TO_USER = new ErrorCode(100_7_013, "用户优惠券不属于当前用户");
    //USER_COUPON_NOT_FOUND
    ErrorCode USER_COUPON_NOT_FOUND = new ErrorCode(100_7_014, "用户优惠券不存在");
    //当前商品是多规格商品，请选择规格
    ErrorCode GOODS_IS_MULTI_SPECIFICATIONS = new ErrorCode(100_7_015, "当前商品({})是多规格商品，请选择规格");
    //订单已支付，请勿重复操作
    ErrorCode ORDER_ALREADY_PAID = new ErrorCode(100_7_016, "订单已支付，请勿重复操作");
    //订单支付金额异常
    ErrorCode ORDER_PAY_AMOUNT_ERROR = new ErrorCode(100_7_017, "订单支付金额异常");
    //当前订单不属于该用户
    ErrorCode ORDER_NOT_BELONG_TO_USER = new ErrorCode(100_7_018, "当前订单不属于该用户({})");
    //当前订单不属于该商户
    ErrorCode ORDER_NOT_BELONG_TO_MERCHANT = new ErrorCode(100_7_019, "当前订单不属于该商户({})");
    // 价格不一致，请重新下单
    ErrorCode PRICE_NOT_CONSISTENT = new ErrorCode(100_7_020, "价格不一致，请重新下单");
}
