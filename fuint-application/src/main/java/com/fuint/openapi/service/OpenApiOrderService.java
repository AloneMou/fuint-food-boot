package com.fuint.openapi.service;

import com.fuint.common.dto.OrderDto;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.openapi.v1.order.vo.OrderCreateReqVO;
import com.fuint.openapi.v1.order.vo.UserOrderRespVO;
import com.fuint.repository.model.MtCart;
import com.fuint.repository.model.MtOrder;

import java.math.BigDecimal;
import java.util.Date;
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


    /**
     * 创建订单
     *
     * @param createReqVO 创建订单参数
     * @return 创建的订单信息
     */
    MtOrder saveOrder(OrderCreateReqVO createReqVO);

    /**
     * 修改订单
     *
     * @param orderDto 修改订单参数
     * @return 修改的订单信息
     */
    MtOrder updateOrder(OrderDto orderDto);

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @param remark  取消备注
     * @return
     * @throws BusinessCheckException
     */
    MtOrder cancelOrder(Integer orderId, String remark);

    /**
     * 更新订单
     *
     * @param mtOrder 订单信息
     * @return 修改的订单信息
     * @throws BusinessCheckException 业务校验异常
     */
    MtOrder updateOrder(MtOrder mtOrder);


    /**
     * 把订单置为已支付
     *
     * @param orderId 订单ID
     */
    boolean setOrderPayed(Integer orderId, BigDecimal payAmount);

    /**
     * 获取用户订单详情
     *
     * @param orderId 订单ID
     * @return 订单详情
     */
    UserOrderRespVO getUserOrderDetail(Integer orderId);

    /**
     * 批量转换订单列表
     *
     * @param orderList 订单列表
     * @return 转换后的订单列表
     */
    List<UserOrderRespVO> convertOrderList(List<MtOrder> orderList);

    /**
     * 获取待处理订单数
     *
     * @param merchantId 商户ID
     * @param storeId    店铺ID
     * @param orderTime  订单时间
     * @param orderId    订单ID
     * @return 待处理订单数
     */
    Integer getToMakeCount(Integer merchantId, Integer storeId, Date orderTime, Integer orderId);
}
