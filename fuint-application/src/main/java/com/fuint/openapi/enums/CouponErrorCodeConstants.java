package com.fuint.openapi.enums;

import com.fuint.framework.exception.ErrorCode;

/**
 * Foot-Fuint-Backend-master
 *
 * @author mjw
 * @since 2026/1/20 22:20
 */
public interface CouponErrorCodeConstants {


    ErrorCode COUPON_NOT_FOUND = new ErrorCode(100_6_001, "优惠券不存在");

    ErrorCode COUPON_REVOKE_PROCESSING = new ErrorCode(100_6_002, "请勿频繁操作，撤销处理中");

    ErrorCode COUPON_BATCH_PROCESSING = new ErrorCode(100_6_003, "请勿频繁操作，批量发券处理中");
    ErrorCode COUPON_STOCK_NOT_ENOUGH = new ErrorCode(100_6_004, "优惠券库存不足,剩余:{}}, 需要:{}");
}
