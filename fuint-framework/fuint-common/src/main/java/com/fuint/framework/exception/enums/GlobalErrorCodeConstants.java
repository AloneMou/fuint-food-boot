package com.fuint.framework.exception.enums;


import com.fuint.framework.exception.ErrorCode;

/**
 * 全局错误码枚举
 * 0-999 系统异常编码保留
 * <p>
 * 一般情况下，使用 HTTP 响应状态码 https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Status
 * 虽然说，HTTP 响应状态码作为业务使用表达能力偏弱，但是使用在系统层面还是非常不错的
 * 比较特殊的是，因为之前一直使用 0 作为成功，就不使用 200 啦。
 *
 * @author 芋道源码
 */
public interface GlobalErrorCodeConstants {

    ErrorCode SUCCESS = new ErrorCode(0, "成功");

    // ========== 客户端错误段 ==========

    ErrorCode BAD_REQUEST = new ErrorCode(400, "请求参数不正确");
    ErrorCode UNAUTHORIZED = new ErrorCode(401, "账号未登录");
    ErrorCode FORBIDDEN = new ErrorCode(403, "没有该操作权限");
    ErrorCode NOT_FOUND = new ErrorCode(404, "请求未找到");
    ErrorCode METHOD_NOT_ALLOWED = new ErrorCode(405, "请求方法不正确");
    ErrorCode LOCKED = new ErrorCode(423, "请求失败，请稍后重试"); // 并发请求，不允许
    ErrorCode TOO_MANY_REQUESTS = new ErrorCode(429, "请求过于频繁，请稍后重试");

    // ========== 服务端错误段 ==========

    ErrorCode INTERNAL_SERVER_ERROR = new ErrorCode(500, "系统异常");
    ErrorCode NOT_IMPLEMENTED = new ErrorCode(501, "功能未实现/未开启");
    ErrorCode ERROR_CONFIGURATION = new ErrorCode(502, "错误的配置项");

    // ========== 自定义错误段 ==========
    ErrorCode REPEATED_REQUESTS = new ErrorCode(900, "重复请求，请稍后重试"); // 重复请求
    ErrorCode DEMO_DENY = new ErrorCode(901, "演示模式，禁止写操作");

    ErrorCode UNKNOWN = new ErrorCode(999, "未知错误");

    //签名不正确
    ErrorCode SIGNATURE_ERROR = new ErrorCode(100_1_001, "签名不正确");
    // APPID不正确
    ErrorCode APPID_ERROR = new ErrorCode(100_1_002, "APPID不正确");

    //APPID不能为空
    ErrorCode APPID_EMPTY = new ErrorCode(100_1_003, "APPID不能为空");

    //当前IP
    ErrorCode CURRENT_IP_ERROR = new ErrorCode(100_1_004, "当前IP({})不允许访问");
    // 时间戳不能为空
    ErrorCode TIMESTAMP_EMPTY = new ErrorCode(100_1_005, "时间戳不能为空");

    //随机字符串长度不得少于
    ErrorCode RANDOM_STRING_LENGTH_ERROR = new ErrorCode(100_1_006, "随机字符串长度不得少于{}位");
    //签名不能为空
    ErrorCode SIGNATURE_EMPTY = new ErrorCode(100_1_007, "签名不能为空");
    //请求时差
    ErrorCode REQUEST_TIME_OUT = new ErrorCode(100_1_008, "请求时差过大");

}
