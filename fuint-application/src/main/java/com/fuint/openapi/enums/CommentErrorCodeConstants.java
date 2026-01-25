package com.fuint.openapi.enums;

import com.fuint.framework.exception.ErrorCode;

/**
 * 评价相关错误码常量
 * <p>
 * 错误码格式: 100_8_xxx
 */
public interface CommentErrorCodeConstants {

    // 评价不存在
    ErrorCode COMMENT_NOT_FOUND = new ErrorCode(100_8_001, "评价不存在");

    // 用户无权操作该评价
    ErrorCode COMMENT_NOT_BELONG_TO_USER = new ErrorCode(100_8_002, "用户无权操作该评价");

    // 该商品已评价，不能重复评价
    ErrorCode COMMENT_ALREADY_EXISTS = new ErrorCode(100_8_003, "该商品已评价，不能重复评价");

    // 订单不存在
    ErrorCode COMMENT_ORDER_NOT_FOUND = new ErrorCode(100_8_004, "订单不存在");

    // 订单未完成，不能评价
    ErrorCode COMMENT_ORDER_NOT_COMPLETED = new ErrorCode(100_8_005, "订单未完成，不能评价");

    // 商品不存在
    ErrorCode COMMENT_GOODS_NOT_FOUND = new ErrorCode(100_8_006, "商品不存在");

    // 商品不属于该订单
    ErrorCode COMMENT_GOODS_NOT_IN_ORDER = new ErrorCode(100_8_007, "商品不属于该订单");

    // 用户不存在
    ErrorCode COMMENT_USER_NOT_FOUND = new ErrorCode(100_8_008, "用户不存在");

    // 评价图片数量超过限制
    ErrorCode COMMENT_IMAGE_EXCEED_LIMIT = new ErrorCode(100_8_009, "评价图片数量不能超过9张");

    // 评价内容包含敏感词
    ErrorCode COMMENT_CONTENT_SENSITIVE = new ErrorCode(100_8_010, "评价内容包含敏感词");

    // 商户无权操作该评价
    ErrorCode COMMENT_NOT_BELONG_TO_MERCHANT = new ErrorCode(100_8_011, "商户无权操作该评价");

    // 订单不属于该用户
    ErrorCode COMMENT_ORDER_NOT_BELONG_TO_USER = new ErrorCode(100_8_012, "订单不属于该用户");

}
