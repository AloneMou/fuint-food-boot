package com.fuint.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fuint.common.dto.UserOrderDto;
import com.fuint.common.dto.OrderDto;
import com.fuint.common.param.OrderListParam;
import com.fuint.common.param.SettlementParam;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.repository.model.MtCart;
import com.fuint.repository.model.MtOrder;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 订单业务接口
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface OrderService extends IService<MtOrder> {

    /**
     * 获取用户的订单
     *
     * @param orderListParam
     * @return
     * @throws BusinessCheckException
     */
    PaginationResponse getUserOrderList(OrderListParam orderListParam) throws BusinessCheckException;

    /**
     * 创建订单
     *
     * @param reqDto
     * @return
     * @throws BusinessCheckException
     */
    MtOrder saveOrder(OrderDto reqDto) throws BusinessCheckException;

    /**
     * 订单提交结算
     *
     * @param request         请求参数
     * @param settlementParam 结算参数
     * @return
     * @throws BusinessCheckException
     */
    Map<String, Object> doSettle(HttpServletRequest request, SettlementParam settlementParam) throws BusinessCheckException;

    /**
     * 获取订单详情
     *
     * @param id 订单ID
     * @return
     * @throws BusinessCheckException
     */
    MtOrder getOrderInfo(Integer id) throws BusinessCheckException;

    /**
     * 根据ID获取订单
     *
     * @param id 订单ID
     * @return
     * @throws BusinessCheckException
     */
    UserOrderDto getOrderById(Integer id) throws BusinessCheckException;

    /**
     * 根据ID获取订单
     *
     * @param id 订单ID
     * @return
     * @throws BusinessCheckException
     */
    UserOrderDto getMyOrderById(Integer id) throws BusinessCheckException;

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @param remark  取消备注
     * @return
     * @throws BusinessCheckException
     */
    MtOrder cancelOrder(Integer orderId, String remark) throws BusinessCheckException;

    /**
     * 根据订单ID删除
     *
     * @param orderId  订单ID
     * @param operator 操作人
     * @return
     * @throws BusinessCheckException
     */
    void deleteOrder(Integer orderId, String operator) throws BusinessCheckException;

    /**
     * 根据订单号获取订单详情
     *
     * @param orderSn
     * @return
     * @throws BusinessCheckException
     */
    UserOrderDto getOrderByOrderSn(String orderSn) throws BusinessCheckException;

    /**
     * 根据订单号获取订单详情
     *
     * @param orderSn 订单号
     * @return
     */
    MtOrder getOrderInfoByOrderSn(String orderSn);

    /**
     * 更新订单
     *
     * @param reqDto
     * @return
     * @throws BusinessCheckException
     */
    MtOrder updateOrder(OrderDto reqDto) throws BusinessCheckException;

    /**
     * 更新订单
     *
     * @param mtOrder
     * @return
     * @throws BusinessCheckException
     */
    MtOrder updateOrder(MtOrder mtOrder) throws BusinessCheckException;

    /**
     * 把订单置为已支付
     *
     * @param orderId
     * @param payAmount
     * @return
     * @throws BusinessCheckException
     */
    Boolean setOrderPayed(Integer orderId, BigDecimal payAmount) throws BusinessCheckException;

    /**
     * 根据条件搜索订单
     *
     * @param params 查询参数
     * @return
     * @throws BusinessCheckException
     */
    List<MtOrder> getOrderListByParams(Map<String, Object> params) throws BusinessCheckException;

    /**
     * 获取订单总数
     *
     * @param merchantId 商户ID
     * @param storeId    店铺ID
     * @return
     * @throws BusinessCheckException
     */
    BigDecimal getOrderCount(Integer merchantId, Integer storeId) throws BusinessCheckException;

    /**
     * 获取订单数量
     *
     * @param merchantId 商户ID
     * @param storeId    店铺ID
     * @param beginTime  开始时间
     * @param endTime    结束时间
     * @return
     * @throws BusinessCheckException
     */
    BigDecimal getOrderCount(Integer merchantId, Integer storeId, Date beginTime, Date endTime) throws BusinessCheckException;

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
     * @return
     * @throws BusinessCheckException
     */
    Map<String, Object> calculateCartGoods(Integer merchantId, Integer userId, List<MtCart> cartList, Integer couponId, boolean isUsePoint, String platform, String orderMode) throws BusinessCheckException;

    /**
     * 获取支付金额
     *
     * @param merchantId 商户ID
     * @param storeId    店铺ID
     * @param beginTime  开始时间
     * @param endTime    结束时间
     * @return
     * @throws BusinessCheckException
     */
    BigDecimal getPayMoney(Integer merchantId, Integer storeId, Date beginTime, Date endTime) throws BusinessCheckException;

    /**
     * 获取支付人数
     *
     * @param merchantId 商户ID
     * @param storeId    店铺ID
     * @return
     * @throws BusinessCheckException
     */
    Integer getPayUserCount(Integer merchantId, Integer storeId) throws BusinessCheckException;

    /**
     * 获取支付金额
     *
     * @param merchantId 商户ID
     * @param storeId    店铺ID
     * @return
     * @throws BusinessCheckException
     */
    BigDecimal getPayMoney(Integer merchantId, Integer storeId) throws BusinessCheckException;

    /**
     * 获取会员支付金额
     *
     * @param userId 会员ID
     * @return
     * @throws BusinessCheckException
     */
    BigDecimal getUserPayMoney(Integer userId) throws BusinessCheckException;

    /**
     * 获取会员订单数
     *
     * @param userId 会员ID
     * @return
     * @throws BusinessCheckException
     */
    Integer getUserPayOrderCount(Integer userId) throws BusinessCheckException;

    /**
     * 获取等待分佣的订单列表
     *
     * @param dateTime 时间
     * @return
     * @throws BusinessCheckException
     */
    List<MtOrder> getTobeCommissionOrderList(String dateTime) throws BusinessCheckException;

    /**
     * 发送取餐提醒
     *
     * @param orderDto 订购DTO
     * @return boolean
     */
    boolean sendTakeFoodRemind(OrderDto orderDto) throws BusinessCheckException;

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
     * @throws BusinessCheckException
     */
    Map<String, Object> preCreateOrder(Integer merchantId, Integer userId, List<MtCart> cartList, Integer userCouponId, Integer usePoint, String platform, String orderMode, Integer storeId) throws BusinessCheckException;


    /**
     * 批量确认订单
     *
     * @param orderIds 订单ID列表
     */
    void batchConfirmed(List<Integer> orderIds);

    /**
     * 根据核销码查询订单
     *
     * @param verifyCode 核销码
     * @return 订单
     */
    MtOrder getByVerifyCode(String verifyCode,Integer merchantId);
}
