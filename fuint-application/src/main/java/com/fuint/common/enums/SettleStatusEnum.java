package com.fuint.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 订单结算状态
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Getter
@AllArgsConstructor
public enum SettleStatusEnum {
    WAIT("A", "待确认"),
    COMPLETE("B", "已完成");

    private final String key;

    private final String value;

    public static SettleStatusEnum getEnum(String key) {
        for (SettleStatusEnum item : SettleStatusEnum.values()) {
            if (item.getKey().equals(key)) {
                return item;
            }
        }
        return SettleStatusEnum.WAIT;
    }
}
