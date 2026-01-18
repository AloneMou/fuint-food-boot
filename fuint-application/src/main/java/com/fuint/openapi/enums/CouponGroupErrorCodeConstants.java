package com.fuint.openapi.enums;

import com.fuint.framework.exception.ErrorCode;

/**
 * OpenAPI 优惠券分组错误码常量
 * 错误码范围: 100_5_001 ~ 100_5_999
 *
 * @author mjw
 * @since 2026/1/17
 */
public interface CouponGroupErrorCodeConstants {

    // ========== 优惠券分组相关错误 100_5_001 ~ 100_5_100 ==========
    ErrorCode COUPON_GROUP_NOT_FOUND = new ErrorCode(100_5_001, "优惠券分组不存在");
    ErrorCode COUPON_GROUP_NAME_REQUIRED = new ErrorCode(100_5_002, "分组名称不能为空");
    ErrorCode COUPON_GROUP_ID_REQUIRED = new ErrorCode(100_5_003, "分组ID不能为空");
    ErrorCode COUPON_GROUP_MERCHANT_ID_REQUIRED = new ErrorCode(100_5_004, "商户ID不能为空");
    ErrorCode COUPON_GROUP_CREATE_FAILED = new ErrorCode(100_5_005, "创建优惠券分组失败");
    ErrorCode COUPON_GROUP_UPDATE_FAILED = new ErrorCode(100_5_006, "更新优惠券分组失败");
    ErrorCode COUPON_GROUP_DELETE_FAILED = new ErrorCode(100_5_007, "删除优惠券分组失败");
    ErrorCode COUPON_GROUP_ALREADY_DELETED = new ErrorCode(100_5_008, "该分组已被删除");

}