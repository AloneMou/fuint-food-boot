package com.fuint.common.enums;

import lombok.Getter;

/**
 * 订单状态枚举
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Getter
public enum OrderStatusEnum {
    CREATED("A", "待支付"),
    PAID("B", "已支付"),
    CANCEL("C", "已取消"),
    DELIVERY("D", "待发货"),
    DELIVERED("E", "已发货"),
    RECEIVED("F", "已收货"),
    DELETED("G", "已删除"),
    REFUND("H", "已退款");

    private String key;
    private String value;

    OrderStatusEnum(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static OrderStatusEnum getEnum(String key) {
        for (OrderStatusEnum item : OrderStatusEnum.values()) {
            if (item.getKey().equals(key)) {
                return item;
            }
        }
        return OrderStatusEnum.CREATED;
    }
}
