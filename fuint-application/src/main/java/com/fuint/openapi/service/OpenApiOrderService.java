package com.fuint.openapi.service;

import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.repository.model.MtCart;

import java.util.List;
import java.util.Map;

/**
 * Foot-Fuint-Backend-master
 *
 * @author mjw
 * @since 2026/1/18 15:24
 */
public interface OpenApiOrderService {

    /**
     * 计算购物车
     *
     * @param merchantId 商户ID
     * @param userId     会员ID
     * @param cartList   购物车列表
     * @param couponId   使用的卡券ID
     * @param isUsePoint 是否使用积分抵扣
     * @param platform   平台 h5
     * @param orderMode  订单模式，自取或配送
     */
    Map<String, Object> calculateCartGoods(Integer merchantId, Integer userId, List<MtCart> cartList, Integer couponId, boolean isUsePoint, String platform, String orderMode);


    /**
     * 订单预创建（实时算价）
     * 不实际创建订单，仅进行价格试算和优惠券匹配
     *
     * @param merchantId   商户ID
     * @param userId       用户ID
     * @param cartList     购物车列表
     * @param userCouponId 指定使用的用户优惠券ID（为0时自动匹配最优券）
     * @param usePoint     使用积分数量
     * @param platform     平台
     * @param orderMode    订单模式
     * @param storeId      店铺ID
     * @return 订单预创建结果（包含价格信息和可用优惠券列表）
     */
    Map<String, Object> preCreateOrder(Integer merchantId, Integer userId, List<MtCart> cartList, Integer userCouponId, Integer usePoint, String platform, String orderMode, Integer storeId);

}
