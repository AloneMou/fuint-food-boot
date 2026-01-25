package com.fuint.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Foot-Fuint-Backend-master
 *
 * @author mjw
 * @since 2026/1/25 0:35
 */
@AllArgsConstructor
@Getter
public enum TakeStatusEnum {
    //待接单
    WAIT_CONFIRM("WAIT_CONFIRM", "待接单"),
    //接单
    CONFIRM_SUCCESS("CONFIRM_SUCCESS", "接单"),
    //制作中
    MAKING("MAKING", "制作中"),
    //制作完成
    MAKE_SUCCESS("MAKE_SUCCESS", "制作完成"),
    ;

    private final String key;
    private final String value;

    public static TakeStatusEnum getEnum(String key) {
        for (TakeStatusEnum item : values()) {
            if (item.getKey().equals(key)) {
                return item;
            }
        }
        return TakeStatusEnum.MAKE_SUCCESS;
    }
}
