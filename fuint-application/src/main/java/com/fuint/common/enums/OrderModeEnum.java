package com.fuint.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Locale;

/**
 * 订单模式
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Getter
@AllArgsConstructor
public enum OrderModeEnum {
    EXPRESS("express", "配送"),
    ONESELF("oneself", "自取"),
    // 动态价格
    DYNAMIC("dynamic", "动态价格");

    private final String key;
    private final String value;

    public static OrderModeEnum getEnum(String key) {
        for (OrderModeEnum item : OrderModeEnum.values()) {
            if (item.getKey().equals(key)) {
                return item;
            }
        }
        return null;
    }

    public static OrderModeEnum getOrderMode(String key) {
        if (key != null) {
            key = key.toLowerCase(Locale.ROOT);
        }
        return getEnum(key);
    }

}
