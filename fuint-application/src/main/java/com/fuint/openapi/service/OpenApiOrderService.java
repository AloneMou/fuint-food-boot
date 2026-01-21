package com.fuint.openapi.service;

import com.fuint.common.dto.UserOrderDto;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pojo.PageResult;
import com.fuint.openapi.v1.order.vo.*;
import com.fuint.repository.model.MtCart;
import com.fuint.repository.model.MtUserAction;

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
    Map<String, Object> calculateCartGoods(Integer merchantId, Integer userId, List<MtCart> cartList, Integer couponId, boolean isUsePoint, String platform, String orderMode) throws BusinessCheckException;


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
    OrderPreCreateRespVO preCreateOrder(Integer merchantId, Integer userId, List<MtCart> cartList, Integer userCouponId, Integer usePoint, String platform, String orderMode, Integer storeId) throws BusinessCheckException;

    /**
     * 创建订单
     *
     * @param reqVO 订单创建请求参数
     * @return 订单信息
     */
    UserOrderDto createOrder(OrderCreateReqVO reqVO) throws BusinessCheckException;

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @param remark  取消原因
     * @param userId  用户ID
     * @return 是否成功
     */
    Boolean cancelOrder(Integer orderId, String remark, Integer userId) throws BusinessCheckException;

    /**
     * 支付订单
     *
     * @param reqVO 支付请求参数
     * @return 是否成功
     */
    Boolean payOrder(OrderPayReqVO reqVO) throws BusinessCheckException;

    /**
     * 订单退款
     *
     * @param reqVO 退款请求参数
     * @return 是否成功
     */
    Boolean refundOrder(OrderRefundReqVO reqVO) throws BusinessCheckException;

    /**
     * 获取订单详情
     *
     * @param reqVO 订单详情请求参数
     * @return 订单详情
     */
    OrderDetailRespVO getOrderDetail(OrderDetailReqVO reqVO) throws BusinessCheckException;

    /**
     * 订单列表
     *
     * @param reqVO 查询参数
     * @return 订单列表
     */
    PageResult<UserOrderDto> getOrderList(OrderListReqVO reqVO) throws BusinessCheckException;

    /**
     * 订单评价
     *
     * @param reqVO 评价请求参数
     * @return 是否成功
     */
    Boolean evaluateOrder(OrderEvaluateReqVO reqVO);

    /**
     * 订单评价拉取
     *
     * @param reqVO 查询参数
     * @return 评价列表
     */
    PageResult<MtUserAction> getEvaluations(EvaluationPageReqVO reqVO);

    /**
     * 标记订单可取餐
     *
     * @param reqVO 标记订单可取餐请求参数
     * @return 是否成功
     */
    Boolean markOrderReady(OrderReadyReqVO reqVO) throws BusinessCheckException;

}
