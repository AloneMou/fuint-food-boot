package com.fuint.openapi.enums;

import com.fuint.framework.exception.ErrorCode;

/**
 * OpenAPI 用户错误码常量
 * 错误码范围: 100_2_001 ~ 100_2_999
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface UserErrorCodeConstants {

    // ========== 会员相关错误 100_2_001 ~ 100_2_100 ==========
    ErrorCode USER_NOT_FOUND = new ErrorCode(100_2_001, "会员不存在");
    ErrorCode USER_MOBILE_REQUIRED = new ErrorCode(100_2_002, "手机号码不能为空");
    ErrorCode USER_MOBILE_INVALID = new ErrorCode(100_2_003, "手机号码格式不正确");
    ErrorCode USER_MOBILE_DUPLICATE = new ErrorCode(100_2_004, "手机号码已被其他会员使用");
    ErrorCode USER_STATUS_INVALID = new ErrorCode(100_2_005, "会员状态无效");
    ErrorCode USER_GRADE_NOT_FOUND = new ErrorCode(100_2_006, "会员等级不存在");
    ErrorCode USER_GROUP_NOT_FOUND = new ErrorCode(100_2_007, "会员分组不存在");
    ErrorCode USER_STORE_NOT_FOUND = new ErrorCode(100_2_008, "店铺不存在");
    ErrorCode USER_MERCHANT_NOT_FOUND = new ErrorCode(100_2_009, "商户不存在");
    ErrorCode USER_CREATE_FAILED = new ErrorCode(100_2_010, "创建会员失败");
    ErrorCode USER_UPDATE_FAILED = new ErrorCode(100_2_011, "更新会员失败");
    ErrorCode USER_SYNC_FAILED = new ErrorCode(100_2_012, "同步会员数据失败");
    ErrorCode USER_BATCH_SYNC_EXCEED_LIMIT = new ErrorCode(100_2_013, "批量同步数量超过限制，每次最多同步{}条");
    ErrorCode USER_BATCH_SYNC_EMPTY = new ErrorCode(100_2_014, "批量同步数据不能为空");
    
    // ========== 优惠券相关错误 100_2_101 ~ 100_2_150 ==========
    ErrorCode COUPON_STATUS_INVALID = new ErrorCode(100_2_101, "优惠券状态无效");
    
    // ========== 查询相关错误 100_2_151 ~ 100_2_200 ==========
    ErrorCode QUERY_PAGE_INVALID = new ErrorCode(100_2_151, "页码参数无效");
    ErrorCode QUERY_PAGE_SIZE_INVALID = new ErrorCode(100_2_152, "每页数量参数无效");
    ErrorCode QUERY_TIME_FORMAT_INVALID = new ErrorCode(100_2_153, "时间格式不正确");
    
}
