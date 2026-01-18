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
}
