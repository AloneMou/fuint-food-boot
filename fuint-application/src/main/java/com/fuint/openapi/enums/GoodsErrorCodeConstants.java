package com.fuint.openapi.enums;

import com.fuint.framework.exception.ErrorCode;

/**
 * Foot-Fuint-Backend-master
 *
 * @author mjw
 * @since 2026/1/16 0:50
 */
public interface GoodsErrorCodeConstants {


    ErrorCode GOODS_NOT_FOUND = new ErrorCode(100_4_001, "商品不存在");

    //卡券ID等于“" + couponId + "”的虚拟卡券不存在.
    ErrorCode GOODS_COUPON_NOT_FOUND = new ErrorCode(100_4_002, "卡券ID等于“{}”的虚拟卡券不存在.");

    //获取商品详情失败
    ErrorCode GOODS_GET_DETAIL_FAILED = new ErrorCode(100_4_003, "获取商品详情失败");

    ErrorCode GOODS_COUPON_TOO_MANY = new ErrorCode(100_4_004, "单个商品优惠券不得大于{}");

    ErrorCode GOODS_SKU_NOT_EXIST = new ErrorCode(100_4_005, "商品SKU({})不存在");
    //
    ErrorCode GOODS_SKU_NOT_ENOUGH = new ErrorCode(100_4_006, "商品({})库存不足");
}
