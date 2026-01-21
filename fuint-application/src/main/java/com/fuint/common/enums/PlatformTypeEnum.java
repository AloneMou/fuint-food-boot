package com.fuint.common.enums;

/**
 * 平台类型枚举
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public enum PlatformTypeEnum {

    /**
     * PC
     */
    PC("PC", "PC端"),

    /**
     * H5
     */
    H5("H5", "H5端"),

    /**
     * 微信小程序
     */
    MP_WEIXIN("MP-WEIXIN", "微信小程序"),

    /**
     * App客户端
     */
    APP("APP", "App客户端"),
    /**
     * 飞书
     */
    FEISHU("FEISHU", "飞书"),

    /**
     * 开放平台
     */
    OPEN_API("OPEN_API", "开放平台")
    ;

    private String code;

    private String value;

    PlatformTypeEnum(String code, String value) {
        this.code = code;
        this.value = value;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
