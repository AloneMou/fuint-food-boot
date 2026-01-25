package com.fuint.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 支付状态
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Getter
@AllArgsConstructor
public enum PayStatusEnum {
    WAIT("A", "待支付"),
    SUCCESS("B", "已支付");

    private final String key;

    private final String value;

    public static PayStatusEnum getEnum(String key) {
        for (PayStatusEnum item : PayStatusEnum.values()) {
            if (item.getKey().equals(key)) {
                return item;
            }
        }
        return PayStatusEnum.WAIT;
    }
}
