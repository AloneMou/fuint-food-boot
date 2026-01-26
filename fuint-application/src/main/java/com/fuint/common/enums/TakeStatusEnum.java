package com.fuint.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 取餐状态枚举
 * <p>
 * 对应咖啡系统事件回调文档中的 Order Take Status
 *
 * @author mjw
 * @since 2026/1/25
 */
@AllArgsConstructor
@Getter
public enum TakeStatusEnum {

    /**
     * 待确认
     */
    PENDING("PENDING", "待确认"),

    /**
     * 已确认
     */
    CONFIRMED("CONFIRMED", "已确认"),

    /**
     * 制作中
     */
    PROCESSING("PROCESSING", "制作中"),

    /**
     * 可取餐
     */
    READY("READY", "可取餐"),

    /**
     * 已完成
     */
    COMPLETED("COMPLETED", "已完成"),

    /**
     * 已取消
     */
    CANCELLED("CANCELLED", "已取消");

    private final String key;
    private final String value;

    /**
     * 根据 key 获取枚举
     *
     * @param key 状态值
     * @return 枚举对象，未找到返回 null
     */
    public static TakeStatusEnum getEnum(String key) {
        for (TakeStatusEnum item : values()) {
            if (item.getKey().equals(key)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 检查状态流转是否合法
     *
     * @param nextStatus 下一个状态
     * @return 是否合法
     */
    public boolean canTransitionTo(TakeStatusEnum nextStatus) {
        if (nextStatus == null) {
            return false;
        }
        
        // 自身状态流转（可能是重试）认为是合法的
        if (this == nextStatus) {
            return true;
        }

        switch (this) {
            case PENDING:
                // 待确认 -> 已确认 | 已取消
                return nextStatus == CONFIRMED || nextStatus == CANCELLED;
            case CONFIRMED:
                // 已确认 -> 制作中 | 已取消
                return nextStatus == PROCESSING || nextStatus == CANCELLED;
            case PROCESSING:
                // 制作中 -> 可取餐 | 已取消
                return nextStatus == READY || nextStatus == CANCELLED;
            case READY:
                // 可取餐 -> 已完成 | 已取消
                return nextStatus == COMPLETED || nextStatus == CANCELLED;
            case COMPLETED:
            case CANCELLED:
                // 终态不可流转
                return false;
            default:
                return false;
        }
    }
}
