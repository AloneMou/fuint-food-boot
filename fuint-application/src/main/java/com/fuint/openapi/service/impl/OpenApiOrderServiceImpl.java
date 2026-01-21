package com.fuint.openapi.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fuint.common.dto.*;
import com.fuint.common.enums.*;
import com.fuint.common.service.*;
import com.fuint.common.util.DateUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.exception.ServiceException;
import com.fuint.framework.pojo.PageResult;
import com.fuint.openapi.service.EventCallbackService;
import com.fuint.openapi.service.OpenApiOrderService;
import com.fuint.openapi.v1.order.vo.*;
import com.fuint.repository.mapper.*;
import com.fuint.repository.model.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static com.fuint.openapi.enums.OrderErrorCodeConstants.GOODS_NOT_BELONG_TO_STORE;
import static com.fuint.openapi.enums.OrderErrorCodeConstants.GOODS_NOT_EMPTY;
import static com.fuint.openapi.enums.UserErrorCodeConstants.USER_NOT_FOUND;

/**
 * Foot-Fuint-Backend-master
 *
 * @author mjw
 * @since 2026/1/18 15:24
 */
@Service
public class OpenApiOrderServiceImpl implements OpenApiOrderService {

    // 常量定义
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal TEN = new BigDecimal("10");
    private static final Integer DEFAULT_POINT = 0;
    private static final String DATE_FORMAT = "yyyy.MM.dd HH:mm";
    private static final String STATUS_AVAILABLE = "A";
    private static final String STATUS_UNAVAILABLE = "N";

    @Resource
    private MtOrderMapper mtOrderMapper;

    @Resource
    private MtGoodsMapper mtGoodsMapper;

    @Resource
    private MtOrderGoodsMapper mtOrderGoodsMapper;

    @Resource
    private MtCartMapper mtCartMapper;

    @Resource
    private MtOrderAddressMapper mtOrderAddressMapper;

    @Resource
    private MtConfirmLogMapper mtConfirmLogMapper;

    @Resource
    private MtUserCouponMapper mtUserCouponMapper;

    @Resource
    private MtGoodsSkuMapper mtGoodsSkuMapper;

    @Resource
    private MtRegionMapper mtRegionMapper;

    @Resource
    private MtUserGradeMapper mtUserGradeMapper;

    @Resource
    private MtCouponGoodsMapper mtCouponGoodsMapper;

    @Resource
    private MtUserActionMapper mtUserActionMapper;

    /**
     * 系统设置服务接口
     */
    @Resource
    private SettingService settingService;

    /**
     * 卡券服务接口
     */
    @Resource
    private CouponService couponService;

    /**
     * 会员卡券服务接口
     */
    @Resource
    private UserCouponService userCouponService;

    /**
     * 收货地址服务接口
     */
    @Resource
    private AddressService addressService;

    /**
     * 会员服务接口
     */
    @Resource
    private MemberService memberService;

    /**
     * 积分服务接口
     */
    @Resource
    private PointService pointService;

    /**
     * 购物车服务接口
     */
    @Resource
    private CartService cartService;

    /**
     * 商品服务接口
     */
    @Resource
    private GoodsService goodsService;

    /**
     * 店铺服务接口
     */
    @Resource
    private StoreService storeService;

    /**
     * 会员等级服务接口
     */
    @Resource
    private UserGradeService userGradeService;

    /**
     * 售后服务接口
     */
    @Resource
    private RefundService refundService;

    /**
     * 余额服务接口
     */
    @Resource
    private BalanceService balanceService;

    /**
     * 微信相关服务接口
     */
    @Resource
    private WeixinService weixinService;

    /**
     * 支付宝服务接口
     */
    @Resource
    private AlipayService alipayService;

    /**
     * 短信发送服务接口
     */
    @Resource
    private SendSmsService sendSmsService;

    /**
     * 开卡赠礼服务接口
     */
    @Resource
    private OpenGiftService openGiftService;

    /**
     * 商户服务接口
     */
    @Resource
    private MerchantService merchantService;

    /**
     * 店铺员工服务接口
     */
    @Resource
    private StaffService staffService;

    /**
     * 支付服务接口
     */
    @Resource
    private PaymentService paymentService;

    /**
     * 桌码服务接口
     */
    @Resource
    private TableService tableService;

    /**
     * 云打印服务接口
     */
    @Resource
    private PrinterService printerService;

    @Resource
    private OrderService orderService;

    @Resource
    private EventCallbackService eventCallbackService;

    @Override
    public Map<String, Object> calculateCartGoods(Integer merchantId, Integer userId, List<MtCart> cartList, Integer couponId, boolean isUsePoint, String platform, String orderMode) throws BusinessCheckException {
        MtUser userInfo = memberService.queryMemberById(userId);
        // 检查是否可以使用积分抵扣
        isUsePoint = checkCanUsePoint(merchantId, isUsePoint);

        // 处理购物车商品列表
        CartCalculationResult cartResult = processCartItems(cartList);

        List<ResCartDto> cartDtoList = cartResult.getCartDtoList();
        BigDecimal totalPrice = cartResult.getTotalPrice();
        BigDecimal totalCanUsePointAmount = cartResult.getTotalCanUsePointAmount();
        int totalNum = cartResult.getTotalNum();

        Map<String, Object> result = new HashMap<>();

        // 获取可用卡券列表
        List<CouponDto> couponList = getAvailableCoupons(userId, totalPrice, platform, cartList);

        // 计算使用的卡券金额
        CouponUsageResult couponResult = calculateCouponAmount(couponId, totalPrice);
        MtCoupon useCouponInfo = couponResult.getUseCouponInfo();
        BigDecimal couponAmount = couponResult.getCouponAmount();

        // 计算支付金额（减去卡券抵扣）
        BigDecimal payPrice = totalPrice.subtract(couponAmount);

        // 计算积分使用
        PointUsageResult pointResult = calculatePointUsage(userInfo, merchantId, isUsePoint, payPrice, totalCanUsePointAmount);
        int usePoint = pointResult.getUsePoint();
        BigDecimal usePointAmount = pointResult.getUsePointAmount();
        int myPoint = pointResult.getMyPoint();

        // 支付金额 = 商品总额 - 积分抵扣金额
        payPrice = payPrice.subtract(usePointAmount);
        payPrice = payPrice.max(ZERO);

        // 计算配送费用
        BigDecimal deliveryFee = calculateDeliveryFee(merchantId, orderMode);

        // 计算会员折扣（整体应用，与OrderServiceImpl.calculateCartGoods保持一致）
        BigDecimal payDiscount = calculateMemberDiscount(merchantId, userInfo);
        payPrice = payPrice.multiply(payDiscount).add(deliveryFee);

        result.put("list", cartDtoList);
        result.put("totalNum", totalNum);
        result.put("totalPrice", totalPrice);
        result.put("payPrice", payPrice);
        result.put("couponList", couponList);
        result.put("useCouponInfo", useCouponInfo);
        result.put("usePoint", usePoint);
        result.put("myPoint", myPoint);
        result.put("couponAmount", couponAmount);
        result.put("usePointAmount", usePointAmount);
        result.put("deliveryFee", deliveryFee);
        return result;
    }


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
    @Override
    public OrderPreCreateRespVO preCreateOrder(Integer merchantId, Integer userId, List<MtCart> cartList, Integer userCouponId, Integer usePoint, String platform, String orderMode, Integer storeId) throws BusinessCheckException {
        // 参数校验
        MtUser userInfo = memberService.queryMemberById(userId);
        if (userInfo == null) {
            throw new ServiceException(USER_NOT_FOUND);
        }
        if (cartList == null || cartList.isEmpty()) {
            throw new ServiceException(GOODS_NOT_EMPTY);
        }

        // 验证商品是否属于公共商品或当前门店
        validateGoodsBelongToStore(cartList, storeId);

        boolean isUsePoint = (usePoint != null && usePoint > 0);

        // 确定使用的优惠券ID（如果需要自动选择最优优惠券，先计算一次获取优惠券列表）
        Integer selectedCouponId = determineSelectedCouponId(userId, cartList, userCouponId, platform, merchantId, isUsePoint, orderMode);
        if (selectedCouponId == null && userCouponId != null && userCouponId > 0) {
            selectedCouponId = userCouponId;
        }

        // 使用确定的优惠券计算购物车（最终计算）
        Map<String, Object> cartData = calculateCartGoods(merchantId, userId, cartList, selectedCouponId != null ? selectedCouponId : 0, isUsePoint, platform, orderMode);

        // 提取计算结果（参考saveOrder的逻辑）
        BigDecimal totalPrice = getBigDecimalValue(cartData.get("totalPrice"));
        BigDecimal deliveryFee = getBigDecimalValue(cartData.get("deliveryFee"));
        BigDecimal couponAmount = getBigDecimalValue(cartData.get("couponAmount"));
        BigDecimal usePointAmount = getBigDecimalValue(cartData.get("usePointAmount"));
        List<CouponDto> couponList = (List<CouponDto>) cartData.get("couponList");
        List<ResCartDto> goodsList = (List<ResCartDto>) cartData.get("list");
        Integer myPoint = getIntegerValue(cartData.get("myPoint"));
        Integer calculatedUsePoint = getIntegerValue(cartData.get("usePoint"));

        // 构建可用优惠券列表
        List<Map<String, Object>> availableCouponsMap = buildAvailableCouponsList(couponList, selectedCouponId);
        List<AvailableCouponVO> availableCoupons = convertAvailableCoupons(availableCouponsMap);

        // 模拟saveOrder的价格计算逻辑（第450-460行，第600-610行）
        // 1. 实付金额 = 商品总额 - 积分抵扣金额 - 优惠券金额（不含会员折扣和配送费）
        BigDecimal payAmount = totalPrice.subtract(usePointAmount).subtract(couponAmount);
        if (payAmount.compareTo(ZERO) < 0) {
            payAmount = ZERO;
        }

        // 2. 按商品级别计算会员折扣（参考saveOrder第600-610行）
        BigDecimal memberDiscountAmount = calculateMemberDiscountByItems(merchantId, userInfo, goodsList);
        if (memberDiscountAmount.compareTo(ZERO) > 0) {
            // 从实付金额中减去会员折扣
            payAmount = payAmount.subtract(memberDiscountAmount);
            if (payAmount.compareTo(ZERO) < 0) {
                payAmount = ZERO;
            }
        }

        // 3. 最终应付金额 = 实付金额 + 配送费
        BigDecimal payableAmount = payAmount.add(deliveryFee);

        // 构建响应VO
        OrderPreCreateRespVO respVO = new OrderPreCreateRespVO();
        respVO.setTotalAmount(totalPrice);
        respVO.setDiscountAmount(couponAmount);
        respVO.setPointAmount(usePointAmount);
        respVO.setMemberDiscountAmount(memberDiscountAmount);
        respVO.setDeliveryFee(deliveryFee);
        respVO.setPayableAmount(payableAmount);
        respVO.setUsePoint(calculatedUsePoint);
        respVO.setAvailablePoint(myPoint);
        respVO.setSelectedCouponId(selectedCouponId);
        respVO.setCalculateTime(new Date());
        respVO.setOrderMode(orderMode);
        respVO.setStoreId(storeId);
        respVO.setAvailableCoupons(availableCoupons);

        // 转换商品列表
        List<OrderGoodsDetailVO> goodsDetailList = convertGoodsList(goodsList, userId, merchantId);
        respVO.setGoodsList(goodsDetailList);

        // 计算商品总数量
        int totalQuantity = goodsDetailList.stream()
                .mapToInt(goods -> goods.getQuantity() != null ? goods.getQuantity() : 0)
                .sum();
        respVO.setTotalQuantity(totalQuantity);

        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserOrderDto createOrder(OrderCreateReqVO reqVO) throws BusinessCheckException {
        // 1. 验证用户是否存在
        MtUser userInfo = memberService.queryMemberById(reqVO.getUserId());
        if (userInfo == null) {
            throw new ServiceException(404, "用户不存在");
        }

        // 2. 模拟预创建计算价格
        List<MtCart> cartList = new ArrayList<>();
        if (reqVO.getItems() != null && !reqVO.getItems().isEmpty()) {
            for (OrderGoodsItemVO item : reqVO.getItems()) {
                MtCart cart = new MtCart();
                cart.setGoodsId(item.getGoodsId());
                cart.setSkuId(item.getSkuId() != null ? item.getSkuId() : 0);
                cart.setNum(item.getQuantity());
                cart.setUserId(reqVO.getUserId());
                cart.setStatus(StatusEnum.ENABLED.getKey());
                cartList.add(cart);
            }
        }

        Integer merchantId = reqVO.getMerchantId() != null ? reqVO.getMerchantId() : 1;
        Integer storeId = reqVO.getStoreId() != null ? reqVO.getStoreId() : 0;
        String orderMode = StringUtils.isNotEmpty(reqVO.getOrderMode()) ? reqVO.getOrderMode() : OrderModeEnum.ONESELF.getKey();
        String platform = StringUtils.isNotEmpty(reqVO.getPlatform()) ? reqVO.getPlatform() : "MP-WEIXIN";
        Integer userCouponId = reqVO.getUserCouponId() != null ? reqVO.getUserCouponId() : 0;
        Integer usePoint = reqVO.getUsePoint() != null ? reqVO.getUsePoint() : 0;

        // 系统配置检查：检查交易功能是否关闭
        MtSetting config = settingService.querySettingByName(merchantId, storeId, SettingTypeEnum.ORDER.getKey(), OrderSettingEnum.IS_CLOSE.getKey());
        if (config != null && config.getValue().equals(YesOrNoEnum.TRUE.getKey())) {
            throw new ServiceException(403, "系统已关闭交易功能，请稍后再试！");
        }

        // 调用预创建逻辑进行校验
        OrderPreCreateRespVO preCreateResult = this.preCreateOrder(
                merchantId, reqVO.getUserId(), cartList, userCouponId, usePoint, platform, orderMode, storeId
        );

        BigDecimal calculatedPayableAmount = preCreateResult.getPayableAmount();

        // 3. 校验价格一致性
        if (reqVO.getPreTotalAmount().compareTo(calculatedPayableAmount) != 0) {
            throw new ServiceException(400, "商品价格发生变动,请重新下单");
        }
        
        // 订单起送费检查（针对配送订单）
        if (orderMode.equals(OrderModeEnum.EXPRESS.getKey())) {
            MtSetting delivery = settingService.querySettingByName(merchantId, SettingTypeEnum.ORDER.getKey(), OrderSettingEnum.DELIVERY_MIN_AMOUNT.getKey());
            if (delivery != null && StringUtils.isNotEmpty(delivery.getValue())) {
                BigDecimal deliveryMinAmount = new BigDecimal(delivery.getValue());
                BigDecimal totalAmount = preCreateResult.getTotalAmount();
                if (deliveryMinAmount.compareTo(BigDecimal.ZERO) > 0 && deliveryMinAmount.compareTo(totalAmount) > 0) {
                     throw new ServiceException(400, "订单起送金额：" + deliveryMinAmount + "元");
                }
            }
        }

        // 4. 实际创建订单
        OrderDto orderDto = new OrderDto();
        orderDto.setUserId(reqVO.getUserId());
        orderDto.setMerchantId(merchantId);
        orderDto.setStoreId(storeId);
        orderDto.setOrderMode(orderMode);
        orderDto.setPlatform(platform);
        orderDto.setCouponId(userCouponId);
        orderDto.setUsePoint(usePoint);
        orderDto.setRemark(reqVO.getRemark());
        orderDto.setTableId(reqVO.getTableId());
        orderDto.setType(reqVO.getType() != null ? reqVO.getType().getKey() : OrderTypeEnum.GOODS.getKey());
        orderDto.setPayType(reqVO.getPayType() != null ? reqVO.getPayType() : PayTypeEnum.JSAPI.getKey());
        orderDto.setIsVisitor(YesOrNoEnum.NO.getKey());

        // 这里的saveOrder逻辑比较复杂，目前OrderService.saveOrder中已经包含了创建订单、扣减库存、创建订单商品等逻辑
        // 如果要完全脱离OrderService，需要复制大量代码。考虑到"参考OrderService的实现逻辑"，建议此处复用OrderService的核心创建逻辑
        // 或者如果需要，可以在这里重新实现。鉴于风险控制，这里复用OrderService，但对外接口已统一到OpenApiOrderService
        MtOrder order = orderService.saveOrder(orderDto);
        UserOrderDto result = orderService.getOrderById(order.getId());

        // 发送订单创建回调
        eventCallbackService.sendOrderStatusChangedCallback(order, null, OrderStatusEnum.CREATED.getKey());

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelOrder(Integer orderId, String remark, Integer userId) throws BusinessCheckException {
        MtOrder order = orderService.getOrderInfo(orderId);
        if (order == null) {
            throw new ServiceException(404, "订单不存在");
        }

        // 验证订单是否属于用户
        if (userId != null && !order.getUserId().equals(userId)) {
            throw new ServiceException(403, "无权操作该订单");
        }

        String oldStatus = order.getStatus();

        // 如果已支付，执行退款
        if (order.getPayStatus().equals(PayStatusEnum.SUCCESS.getKey())) {
            AccountInfo accountInfo = new AccountInfo();
            accountInfo.setAccountName("OpenApi-System");
            refundService.doRefund(order.getId(), order.getPayAmount().toString(), "订单取消自动退款", accountInfo);
        }

        orderService.cancelOrder(orderId, remark);

        // 获取更新后的订单信息
        MtOrder updatedOrder = orderService.getOrderInfo(orderId);
        // 发送订单取消回调
        eventCallbackService.sendOrderStatusChangedCallback(updatedOrder, oldStatus, OrderStatusEnum.CANCEL.getKey());

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean payOrder(OrderPayReqVO reqVO) throws BusinessCheckException {
        MtOrder order = orderService.getOrderInfo(reqVO.getOrderId());
        if (order == null) {
            throw new ServiceException(404, "订单不存在");
        }

        // 验证订单是否属于用户
        if (reqVO.getUserId() != null && !order.getUserId().equals(reqVO.getUserId())) {
            throw new ServiceException(403, "无权操作该订单");
        }

        // 设置为已支付
        Boolean result = orderService.setOrderPayed(reqVO.getOrderId(), reqVO.getPayAmount());

        if (result) {
            // 获取更新后的订单信息
            MtOrder updatedOrder = orderService.getOrderInfo(reqVO.getOrderId());

            // 发送订单支付成功事件回调
            eventCallbackService.sendPaymentStatusChangedCallback(updatedOrder, "SUCCESS");

            // 发送订单状态变更回调
            eventCallbackService.sendOrderStatusChangedCallback(updatedOrder, order.getStatus(), OrderStatusEnum.PAID.getKey());
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean refundOrder(OrderRefundReqVO reqVO) throws BusinessCheckException {
        MtOrder order = orderService.getOrderInfo(reqVO.getOrderId());
        if (order == null) {
            throw new ServiceException(404, "订单不存在");
        }

        // 验证订单权限
        if (reqVO.getUserId() != null && !order.getUserId().equals(reqVO.getUserId())) {
            throw new ServiceException(403, "无权操作该订单");
        }

        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setAccountName("OpenApi-System");

        eventCallbackService.sendPaymentStatusChangedCallback(order, "REFUNDING");

        Boolean result = refundService.doRefund(reqVO.getOrderId(), reqVO.getAmount().toString(), reqVO.getRemark(), accountInfo);

        if (result) {
            // 获取更新后的订单信息
            MtOrder updatedOrder = orderService.getOrderInfo(reqVO.getOrderId());
            // 发送订单退款成功事件回调
            eventCallbackService.sendPaymentStatusChangedCallback(updatedOrder, "REFUNDED");
        }

        return result;
    }

    @Override
    public OrderDetailRespVO getOrderDetail(OrderDetailReqVO reqVO) throws BusinessCheckException {
        UserOrderDto orderDto = orderService.getOrderById(reqVO.getOrderId());
        if (orderDto == null) {
            throw new ServiceException(404, "订单不存在");
        }

        // 验证订单权限
        if (reqVO.getUserId() != null && !orderDto.getUserId().equals(reqVO.getUserId())) {
             throw new ServiceException(403, "无权访问该订单");
        }
        if (reqVO.getMerchantId() != null && !orderDto.getMerchantId().equals(reqVO.getMerchantId())) {
             throw new ServiceException(403, "无权访问该订单");
        }

        OrderDetailRespVO respVO = new OrderDetailRespVO();
        respVO.setOrder(orderDto);

        // 计算队列信息（优化：使用一条SQL聚合查询）
        // 假设状态为已支付（待制作/制作中）的订单在排队
        LambdaQueryWrapper<MtOrder> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(MtOrder::getStatus, OrderStatusEnum.PAID.getKey());
        queryWrapper.lt(MtOrder::getId, reqVO.getOrderId()); // 在当前订单之前的
        List<MtOrder> queueOrders = mtOrderMapper.selectList(queryWrapper); // Use mapper instead of service.list for clarity

        int coffeeCount = 0;
        if (CollUtil.isNotEmpty(queueOrders)) {
            List<Integer> queueOrderIds = queueOrders.stream()
                    .map(MtOrder::getId)
                    .collect(Collectors.toList());
            // 使用批量查询，一次性统计所有订单的商品数量
            Integer count = mtOrderGoodsMapper.countGoodsByOrderIds(queueOrderIds);
            coffeeCount = count != null ? count : 0;
        }

        respVO.setQueueCount(coffeeCount);
        respVO.setEstimatedWaitTime(coffeeCount * 5); // 假设每杯5分钟
        
        return respVO;
    }
    
    // 辅助方法：验证商品归属
    private void validateGoodsBelongToStore(List<MtCart> cartList, Integer storeId) {
        if (CollUtil.isNotEmpty(cartList)) {
            for (MtCart cart : cartList) {
                try {
                    MtGoods goodsInfo = goodsService.queryGoodsById(cart.getGoodsId());
                    if (goodsInfo != null) {
                        Integer goodsStoreId = goodsInfo.getStoreId();
                        if (goodsStoreId == null) {
                            throw new ServiceException(GOODS_NOT_BELONG_TO_STORE);
                        }
                        if (storeId == 0) {
                            if (goodsStoreId != 0) {
                                throw new ServiceException(GOODS_NOT_BELONG_TO_STORE);
                            }
                        } else {
                            if (goodsStoreId != 0 && !goodsStoreId.equals(storeId)) {
                                throw new ServiceException(GOODS_NOT_BELONG_TO_STORE);
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new ServiceException(GOODS_NOT_BELONG_TO_STORE);
                }
            }
        }
    }

    @Override
    public PageResult<UserOrderDto> getOrderList(OrderListReqVO reqVO) throws BusinessCheckException {
        // 使用 MyBatis Plus 分页查询
        PageResult<MtOrder> pageResult = mtOrderMapper.selectOrderPage(reqVO);
        
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty();
        }

        // 转换为 UserOrderDto
        List<UserOrderDto> userOrderList = new ArrayList<>();
        for (MtOrder order : pageResult.getList()) {
            try {
                UserOrderDto orderDto = orderService.getOrderById(order.getId());
                if (orderDto != null) {
                    userOrderList.add(orderDto);
                }
            } catch (Exception e) {
                // log.warn("获取订单详情失败: orderId={}, error={}", order.getId(), e.getMessage());
            }
        }

        PageResult<UserOrderDto> result = new PageResult<>();
        result.setTotal(pageResult.getTotal());
        result.setTotalPages(pageResult.getTotalPages());
        result.setCurrentPage(pageResult.getCurrentPage());
        result.setPageSize(pageResult.getPageSize());
        result.setList(userOrderList);

        return result;
    }

    @Override
    public Boolean evaluateOrder(OrderEvaluateReqVO reqVO) {
        MtUserAction action = new MtUserAction();
        action.setUserId(0); 

        MtOrder order = mtOrderMapper.selectById(reqVO.getOrderId());
        if (order != null) {
            action.setUserId(order.getUserId());
            action.setMerchantId(order.getMerchantId());
            action.setStoreId(order.getStoreId());
        }

        action.setAction("NPS_EVALUATION");
        action.setDescription(reqVO.getComment());
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", reqVO.getOrderId());
        params.put("score", reqVO.getScore());
        action.setParam(JSON.toJSONString(params));
        action.setCreateTime(new Date());
        action.setUpdateTime(new Date());
        action.setStatus(StatusEnum.ENABLED.getKey());

        mtUserActionMapper.insert(action);

        return true;
    }

    @Override
    public PageResult<MtUserAction> getEvaluations(EvaluationPageReqVO reqVO) {
        return mtUserActionMapper.selectUserActionPage(reqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean markOrderReady(OrderReadyReqVO reqVO) throws BusinessCheckException {
        MtOrder order = orderService.getOrderInfo(reqVO.getOrderId());
        if (order == null) {
            throw new ServiceException(404, "订单不存在");
        }

        // 验证商户权限
        if (reqVO.getMerchantId() != null && !order.getMerchantId().equals(reqVO.getMerchantId())) {
            throw new ServiceException(403, "无权操作该订单");
        }

        // 更新订单状态为已发货（可取餐）
        OrderDto orderDto = new OrderDto();
        orderDto.setId(reqVO.getOrderId());
        orderDto.setStatus(OrderStatusEnum.DELIVERED.getKey());
        orderService.updateOrder(orderDto);

        // 获取更新后的订单信息
        MtOrder updatedOrder = orderService.getOrderInfo(reqVO.getOrderId());

        // 获取订单商品列表
        LambdaQueryWrapper<MtOrderGoods> goodsWrapper = Wrappers.lambdaQuery();
        goodsWrapper.eq(MtOrderGoods::getOrderId, reqVO.getOrderId());
        List<MtOrderGoods> goodsList = mtOrderGoodsMapper.selectList(goodsWrapper);
        
        List<Map<String, Object>> items = new ArrayList<>();
        for (MtOrderGoods orderGoods : goodsList) {
            Map<String, Object> item = new HashMap<>();
            item.put("skuId", orderGoods.getSkuId());
            item.put("quantity", orderGoods.getNum());

            try {
                MtGoods goodsInfo = goodsService.queryGoodsById(orderGoods.getGoodsId());
                if (goodsInfo != null) {
                    item.put("goodsName", goodsInfo.getName());
                } else {
                    item.put("goodsName", "");
                }
            } catch (Exception e) {
                item.put("goodsName", "");
            }

            items.add(item);
        }

        // 发送可取餐状态通知回调
        eventCallbackService.sendOrderReadyCallback(updatedOrder, items);

        return true;
    }

    /**
     * 确定选中的优惠券ID（自动选择最优或使用指定的）
     */
    private Integer determineSelectedCouponId(Integer userId, List<MtCart> cartList, Integer userCouponId, String platform, Integer merchantId, boolean isUsePoint, String orderMode) throws BusinessCheckException {
        // 如果指定了优惠券，直接返回
        if (userCouponId != null && userCouponId > 0) {
            return userCouponId;
        }

        // 计算一次获取优惠券列表（不使用优惠券）
        Map<String, Object> cartData = calculateCartGoods(merchantId, userId, cartList, 0, isUsePoint, platform, orderMode);
        List<CouponDto> couponList = (List<CouponDto>) cartData.get("couponList");
        
        // 查找最优优惠券
        if (couponList != null && !couponList.isEmpty()) {
            Integer bestCouponId = null;
            BigDecimal maxCouponAmount = ZERO;
            
            for (CouponDto coupon : couponList) {
                if (UserCouponStatusEnum.UNUSED.getKey().equals(coupon.getStatus()) && coupon.getAmount() != null) {
                    if (coupon.getAmount().compareTo(maxCouponAmount) > 0) {
                        maxCouponAmount = coupon.getAmount();
                        bestCouponId = coupon.getUserCouponId();
                    }
                }
            }
            return bestCouponId;
        }
        
        return null;
    }

    /**
     * 构建可用优惠券列表
     */
    private List<Map<String, Object>> buildAvailableCouponsList(List<CouponDto> couponList, Integer selectedCouponId) {
        List<Map<String, Object>> availableCoupons = new ArrayList<>();
        if (couponList == null || couponList.isEmpty()) {
            return availableCoupons;
        }

        for (CouponDto coupon : couponList) {
            Map<String, Object> couponInfo = new HashMap<>();
            couponInfo.put("userCouponId", coupon.getUserCouponId());
            couponInfo.put("couponId", coupon.getId());
            couponInfo.put("couponName", coupon.getName());
            couponInfo.put("couponType", coupon.getType());
            couponInfo.put("discountAmount", coupon.getAmount());
            couponInfo.put("description", coupon.getDescription());
            couponInfo.put("effectiveDate", coupon.getEffectiveDate());

            // 判断是否可用
            String usable = UserCouponStatusEnum.UNUSED.getKey().equals(coupon.getStatus()) ? STATUS_AVAILABLE : STATUS_UNAVAILABLE;
            couponInfo.put("usable", usable);

            // 储值卡余额
            if (CouponTypeEnum.PRESTORE.getKey().equals(coupon.getType())) {
                couponInfo.put("balance", coupon.getAmount());
            }

            // 是否选中
            boolean selected = selectedCouponId != null && selectedCouponId.equals(coupon.getUserCouponId());
            couponInfo.put("selected", selected);

            availableCoupons.add(couponInfo);
        }

        return availableCoupons;
    }

    /**
     * 计算应付金额
     */
    private BigDecimal calculatePayableAmount(BigDecimal totalPrice, BigDecimal discountAmount, BigDecimal pointDiscountAmount, BigDecimal deliveryFee) {
        BigDecimal payableAmount = totalPrice.subtract(discountAmount).subtract(pointDiscountAmount).add(deliveryFee);
        return payableAmount.max(ZERO);
    }

    /**
     * 安全获取BigDecimal值
     */
    private BigDecimal getBigDecimalValue(Object value) {
        if (value == null) {
            return ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        return new BigDecimal(value.toString());
    }

    /**
     * 安全获取Integer值
     */
    private Integer getIntegerValue(Object value) {
        if (value == null) {
            return DEFAULT_POINT;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return Integer.parseInt(value.toString());
    }

    /**
     * 检查是否可以使用积分抵扣
     */
    private boolean checkCanUsePoint(Integer merchantId, boolean isUsePoint) {
        if (!isUsePoint) {
            return false;
        }
        MtSetting pointSetting = settingService.querySettingByName(merchantId, SettingTypeEnum.POINT.getKey(), PointSettingEnum.CAN_USE_AS_MONEY.getKey());
        return pointSetting != null && pointSetting.getValue().equals(YesOrNoEnum.TRUE.getKey());
    }

    /**
     * 处理购物车商品列表
     */
    private CartCalculationResult processCartItems(List<MtCart> cartList) {
        List<ResCartDto> cartDtoList = new ArrayList<>();
        String basePath = settingService.getUploadBasePath();
        int totalNum = 0;
        BigDecimal totalPrice = ZERO;
        BigDecimal totalCanUsePointAmount = ZERO;

        if (cartList == null || cartList.isEmpty()) {
            return new CartCalculationResult(cartDtoList, totalNum, totalPrice, totalCanUsePointAmount);
        }

        for (MtCart cart : cartList) {
            MtGoods mtGoodsInfo = goodsService.queryGoodsById(cart.getGoodsId());
            if (mtGoodsInfo == null || !StatusEnum.ENABLED.getKey().equals(mtGoodsInfo.getStatus())) {
                continue;
            }

            totalNum += cart.getNum();
            ResCartDto cartDto = buildCartDto(cart, mtGoodsInfo, basePath);
            cartDtoList.add(cartDto);

            // 计算总价
            BigDecimal itemPrice = cartDto.getGoodsInfo().getPrice().multiply(new BigDecimal(cart.getNum()));
            totalPrice = totalPrice.add(itemPrice);

            // 累加可用积分去抵扣的金额
            if (YesOrNoEnum.YES.getKey().equals(mtGoodsInfo.getCanUsePoint())) {
                totalCanUsePointAmount = totalCanUsePointAmount.add(itemPrice);
            }
        }

        return new CartCalculationResult(cartDtoList, totalNum, totalPrice, totalCanUsePointAmount);
    }

    /**
     * 构建购物车DTO
     */
    private ResCartDto buildCartDto(MtCart cart, MtGoods mtGoodsInfo, String basePath) {
        ResCartDto cartDto = new ResCartDto();
        cartDto.setId(cart.getId());
        cartDto.setGoodsId(cart.getGoodsId());
        cartDto.setNum(cart.getNum());
        cartDto.setSkuId(cart.getSkuId());
        cartDto.setUserId(cart.getUserId());

        // 处理SKU信息
        MtGoods goodsInfo = processGoodsInfo(cart, mtGoodsInfo, basePath);
        cartDto.setGoodsInfo(goodsInfo);

        // 设置规格列表
        if (cart.getSkuId() > 0) {
            List<GoodsSpecValueDto> specList = goodsService.getSpecListBySkuId(cart.getSkuId());
            cartDto.setSpecList(specList);
        }

        // 检查库存有效性
        boolean isEffect = checkStockAvailability(goodsInfo, cart.getNum());
        cartDto.setIsEffect(isEffect);

        return cartDto;
    }

    /**
     * 处理商品信息（包括SKU信息）
     */
    private MtGoods processGoodsInfo(MtCart cart, MtGoods mtGoodsInfo, String basePath) {
        // 处理商品图片路径
        if (StringUtils.isNotEmpty(mtGoodsInfo.getLogo()) && !mtGoodsInfo.getLogo().contains(basePath)) {
            mtGoodsInfo.setLogo(basePath + mtGoodsInfo.getLogo());
        }

        // 如果有SKU，使用SKU信息
        if (cart.getSkuId() > 0) {
            MtGoods mtGoods = new MtGoods();
            BeanUtils.copyProperties(mtGoodsInfo, mtGoods);
            MtGoodsSku mtGoodsSku = mtGoodsSkuMapper.selectById(cart.getSkuId());
            if (mtGoodsSku != null) {
                updateGoodsWithSkuInfo(mtGoods, mtGoodsSku, basePath);
            }
            return mtGoods;
        }

        return mtGoodsInfo;
    }

    /**
     * 使用SKU信息更新商品信息
     */
    private void updateGoodsWithSkuInfo(MtGoods mtGoods, MtGoodsSku mtGoodsSku, String basePath) {
        if (StringUtils.isNotEmpty(mtGoodsSku.getLogo()) && !mtGoodsSku.getLogo().contains(basePath)) {
            mtGoods.setLogo(basePath + mtGoodsSku.getLogo());
        }
        if (mtGoodsSku.getWeight().compareTo(ZERO) > 0) {
            mtGoods.setWeight(mtGoodsSku.getWeight());
        }
        mtGoods.setPrice(mtGoodsSku.getPrice());
        mtGoods.setLinePrice(mtGoodsSku.getLinePrice());
        mtGoods.setStock(mtGoodsSku.getStock());
    }

    /**
     * 检查库存是否充足
     */
    private boolean checkStockAvailability(MtGoods goodsInfo, Integer num) {
        return goodsInfo.getStock() == null || goodsInfo.getStock() >= num;
    }

    /**
     * 获取可用卡券列表
     */
    private List<CouponDto> getAvailableCoupons(Integer userId, BigDecimal totalPrice, String platform, List<MtCart> cartList) {
        List<CouponDto> couponList = new ArrayList<>();
        List<String> statusList = Collections.singletonList(UserCouponStatusEnum.UNUSED.getKey());
        List<MtUserCoupon> userCouponList = userCouponService.getUserCouponList(userId, statusList);

        if (userCouponList == null || userCouponList.isEmpty()) {
            return couponList;
        }

        for (MtUserCoupon userCoupon : userCouponList) {
            MtCoupon couponInfo = couponService.queryCouponById(userCoupon.getCouponId());
            if (couponInfo == null || !isCouponTypeUsable(couponInfo)) {
                continue;
            }

            // 检查使用场景限制
            if (!isCouponUseForValid(couponInfo, platform)) {
                continue;
            }

            CouponDto couponDto = buildCouponDto(couponInfo, userCoupon, totalPrice, cartList);
            couponList.add(couponDto);
        }

        return couponList;
    }

    /**
     * 检查卡券类型是否可用
     */
    private boolean isCouponTypeUsable(MtCoupon couponInfo) {
        return CouponTypeEnum.COUPON.getKey().equals(couponInfo.getType())
                || CouponTypeEnum.PRESTORE.getKey().equals(couponInfo.getType());
    }

    /**
     * 检查卡券使用场景是否有效
     */
    private boolean isCouponUseForValid(MtCoupon couponInfo, String platform) {
        if (StringUtils.isEmpty(couponInfo.getUseFor())) {
            return true;
        }

        // 会员专用卡券不能用于购物
        if (CouponUseForEnum.MEMBER_GRADE.getKey().equals(couponInfo.getUseFor())) {
            return false;
        }

        // 线下支付卡券只能在PC端使用
        if (CouponUseForEnum.OFF_LINE_PAYMENT.getKey().equals(couponInfo.getUseFor())) {
            return PlatformTypeEnum.PC.getCode().equals(platform);
        }

        return true;
    }

    /**
     * 构建优惠券DTO
     */
    private CouponDto buildCouponDto(MtCoupon couponInfo, MtUserCoupon userCoupon, BigDecimal totalPrice, List<MtCart> cartList) {
        CouponDto couponDto = new CouponDto();
        couponDto.setId(couponInfo.getId());
        couponDto.setUserCouponId(userCoupon.getId());
        couponDto.setName(couponInfo.getName());
        couponDto.setStatus(UserCouponStatusEnum.DISABLE.getKey());

        boolean isEffective = couponService.isCouponEffective(couponInfo, userCoupon);

        // 处理优惠券类型
        if (CouponTypeEnum.COUPON.getKey().equals(couponInfo.getType())) {
            processCouponType(couponDto, couponInfo, totalPrice, isEffective);
        } else if (CouponTypeEnum.PRESTORE.getKey().equals(couponInfo.getType())) {
            processPrestoreType(couponDto, userCoupon, isEffective);
        }

        // 检查适用商品
        checkCouponApplicableGoods(couponDto, couponInfo, cartList);

        // 设置有效期
        setCouponEffectiveDate(couponDto, couponInfo, userCoupon);

        return couponDto;
    }

    /**
     * 处理优惠券类型
     */
    private void processCouponType(CouponDto couponDto, MtCoupon couponInfo, BigDecimal totalPrice, boolean isEffective) {
        couponDto.setType(CouponTypeEnum.COUPON.getKey());
        if (StringUtils.isEmpty(couponInfo.getOutRule()) || "0".equals(couponInfo.getOutRule())) {
            couponDto.setDescription("无使用门槛");
            if (isEffective) {
                couponDto.setStatus(UserCouponStatusEnum.UNUSED.getKey());
            }
        } else {
            couponDto.setDescription("满" + couponInfo.getOutRule() + "元可用");
            BigDecimal conditionAmount = new BigDecimal(couponInfo.getOutRule());
            if (totalPrice.compareTo(conditionAmount) > 0 && isEffective) {
                couponDto.setStatus(UserCouponStatusEnum.UNUSED.getKey());
            }
        }
    }

    /**
     * 处理储值卡类型
     */
    private void processPrestoreType(CouponDto couponDto, MtUserCoupon userCoupon, boolean isEffective) {
        couponDto.setType(CouponTypeEnum.PRESTORE.getKey());
        couponDto.setDescription("无使用门槛");
        couponDto.setAmount(userCoupon.getBalance());
        if (isEffective && userCoupon.getBalance().compareTo(ZERO) > 0) {
            couponDto.setStatus(UserCouponStatusEnum.UNUSED.getKey());
        }
    }

    /**
     * 检查优惠券适用商品
     */
    private void checkCouponApplicableGoods(CouponDto couponDto, MtCoupon couponInfo, List<MtCart> cartList) {
        if (!ApplyGoodsEnum.PARK_GOODS.getKey().equals(couponInfo.getApplyGoods())
                || cartList == null || cartList.isEmpty()) {
            return;
        }

        List<MtCouponGoods> couponGoodsList = mtCouponGoodsMapper.getCouponGoods(couponInfo.getId());
        if (couponGoodsList == null || couponGoodsList.isEmpty()) {
            return;
        }

        List<Integer> applyGoodsIds = couponGoodsList.stream()
                .map(MtCouponGoods::getGoodsId)
                .collect(Collectors.toList());

        List<Integer> goodsIds = cartList.stream()
                .map(MtCart::getGoodsId)
                .collect(Collectors.toList());

        List<Integer> intersection = applyGoodsIds.stream()
                .filter(goodsIds::contains)
                .collect(Collectors.toList());

        if (intersection.isEmpty()) {
            couponDto.setStatus(UserCouponStatusEnum.DISABLE.getKey());
        }
    }

    /**
     * 设置优惠券有效期
     */
    private void setCouponEffectiveDate(CouponDto couponDto, MtCoupon couponInfo, MtUserCoupon userCoupon) {
        if (CouponExpireTypeEnum.FIX.getKey().equals(couponInfo.getExpireType())) {
            String beginTime = DateUtil.formatDate(couponInfo.getBeginTime(), DATE_FORMAT);
            String endTime = DateUtil.formatDate(couponInfo.getEndTime(), DATE_FORMAT);
            couponDto.setEffectiveDate(beginTime + "~" + endTime);
        } else if (CouponExpireTypeEnum.FLEX.getKey().equals(couponInfo.getExpireType())) {
            String createTime = DateUtil.formatDate(userCoupon.getCreateTime(), DATE_FORMAT);
            String expireTime = DateUtil.formatDate(userCoupon.getExpireTime(), DATE_FORMAT);
            couponDto.setEffectiveDate(createTime + "~" + expireTime);
        }
    }

    /**
     * 计算使用的卡券金额
     */
    private CouponUsageResult calculateCouponAmount(Integer couponId, BigDecimal totalPrice) {
        CouponUsageResult result = new CouponUsageResult(null, ZERO);

        if (couponId == null || couponId <= 0) {
            return result;
        }

        MtUserCoupon userCouponInfo = userCouponService.getUserCouponDetail(couponId);
        if (userCouponInfo == null) {
            return result;
        }

        MtCoupon useCouponInfo = couponService.queryCouponById(userCouponInfo.getCouponId());
        if (useCouponInfo == null) {
            return result;
        }

        boolean isEffective = couponService.isCouponEffective(useCouponInfo, userCouponInfo);
        if (!isEffective) {
            return result;
        }

        BigDecimal couponAmount = ZERO;
        if (CouponTypeEnum.COUPON.getKey().equals(useCouponInfo.getType())) {
            couponAmount = userCouponInfo.getAmount();
        } else if (CouponTypeEnum.PRESTORE.getKey().equals(useCouponInfo.getType())) {
            BigDecimal couponTotalAmount = userCouponInfo.getBalance();
            couponAmount = couponTotalAmount.min(totalPrice);
            userCouponInfo.setAmount(couponAmount);
        }

        result.setUseCouponInfo(useCouponInfo);
        result.setCouponAmount(couponAmount);
        return result;
    }

    /**
     * 计算积分使用
     */
    private PointUsageResult calculatePointUsage(MtUser userInfo, Integer merchantId, boolean isUsePoint,
                                                 BigDecimal payPrice, BigDecimal totalCanUsePointAmount) {
        int myPoint = userInfo.getPoint() == null ? DEFAULT_POINT : userInfo.getPoint();
        int usePoint = 0;
        BigDecimal usePointAmount = ZERO;

        if (myPoint <= 0 || !isUsePoint) {
            return new PointUsageResult(myPoint, usePoint, usePointAmount);
        }

        MtSetting setting = settingService.querySettingByName(merchantId, SettingTypeEnum.POINT.getKey(),
                PointSettingEnum.EXCHANGE_NEED_POINT.getKey());
        if (setting == null || StringUtils.isEmpty(setting.getValue()) || "0".equals(setting.getValue())) {
            return new PointUsageResult(myPoint, usePoint, usePointAmount);
        }

        BigDecimal exchangeNeedPoint = new BigDecimal(setting.getValue());
        BigDecimal maxUsePointAmount = new BigDecimal(myPoint).divide(exchangeNeedPoint, BigDecimal.ROUND_CEILING, RoundingMode.FLOOR);

        // 不能超过可用积分抵扣的金额
        usePointAmount = maxUsePointAmount.min(totalCanUsePointAmount);
        usePoint = usePointAmount.multiply(exchangeNeedPoint).intValue();

        // 不能超过支付金额
        if (usePointAmount.compareTo(payPrice) > 0) {
            usePointAmount = payPrice;
            usePoint = payPrice.multiply(exchangeNeedPoint).intValue();
        }

        return new PointUsageResult(myPoint, usePoint, usePointAmount);
    }

    /**
     * 计算配送费用
     */
    private BigDecimal calculateDeliveryFee(Integer merchantId, String orderMode) {
        if (!OrderModeEnum.EXPRESS.getKey().equals(orderMode)) {
            return ZERO;
        }

        MtSetting mtSetting = settingService.querySettingByName(merchantId, SettingTypeEnum.ORDER.getKey(),
                OrderSettingEnum.DELIVERY_FEE.getKey());
        if (mtSetting == null || StringUtils.isEmpty(mtSetting.getValue())) {
            return ZERO;
        }

        return new BigDecimal(mtSetting.getValue());
    }

    /**
     * 计算会员折扣（整体应用，与OrderServiceImpl.calculateCartGoods保持一致）
     */
    private BigDecimal calculateMemberDiscount(Integer merchantId, MtUser userInfo) {
        try {
            if (userInfo == null || userInfo.getGradeId() == null) {
                return ONE;
            }
            MtUserGrade userGrade = userGradeService.queryUserGradeById(merchantId,
                    Integer.parseInt(userInfo.getGradeId()), userInfo.getId());
            if (userGrade == null || userGrade.getDiscount() <= 0) {
                return ONE;
            }

            // 与OrderServiceImpl.calculateCartGoods第2163行保持一致
            BigDecimal discount = BigDecimal.valueOf(userGrade.getDiscount()).divide(TEN, BigDecimal.ROUND_CEILING, RoundingMode.FLOOR);
            if (discount.compareTo(ZERO) <= 0) {
                return ONE;
            }
            return discount;
        } catch (Exception e) {
            return ONE;
        }
    }

    /**
     * 按商品级别计算会员折扣金额（参考saveOrder的逻辑）
     * 只对支持会员折扣的商品（isMemberDiscount == YES）应用折扣
     *
     * @param merchantId 商户ID
     * @param userInfo   用户信息
     * @param cartDtoList 购物车商品列表
     * @return 会员折扣总金额
     */
    private BigDecimal calculateMemberDiscountByItems(Integer merchantId, MtUser userInfo, List<ResCartDto> cartDtoList) {
        if (cartDtoList == null || cartDtoList.isEmpty()) {
            return ZERO;
        }

        // 获取会员折扣率
        BigDecimal percent = getMemberDiscountPercent(merchantId, userInfo);
        if (percent.compareTo(ZERO) <= 0) {
            return ZERO;
        }

        BigDecimal totalMemberDiscount = ZERO;

        // 遍历商品列表，按商品级别计算会员折扣
        for (ResCartDto cart : cartDtoList) {
            MtGoods goodsInfo = cart.getGoodsInfo();
            if (goodsInfo == null) {
                continue;
            }

            // 检查商品是否支持会员折扣
            boolean isDiscount = YesOrNoEnum.YES.getKey().equals(goodsInfo.getIsMemberDiscount());
            if (percent.compareTo(ZERO) > 0 && isDiscount) {
                // 计算该商品的会员折扣金额
                BigDecimal price = goodsInfo.getPrice();
                if (price != null && price.compareTo(ZERO) > 0) {
                    // 折扣金额 = (原价 - 折扣后价格) * 数量
                    BigDecimal discountAmount = price.subtract(price.multiply(percent))
                            .multiply(new BigDecimal(cart.getNum()));
                    totalMemberDiscount = totalMemberDiscount.add(discountAmount);
                }
            }
        }

        return totalMemberDiscount.max(ZERO);
    }

    /**
     * 获取会员折扣率（参考saveOrder的逻辑）
     *
     * @param merchantId 商户ID
     * @param userInfo   用户信息
     * @return 会员折扣率（0-1之间，0表示无折扣，1表示不打折）
     */
    private BigDecimal getMemberDiscountPercent(Integer merchantId, MtUser userInfo) {
        try {
            if (userInfo == null || userInfo.getGradeId() == null) {
                return ZERO;
            }

            MtUserGrade userGrade = userGradeService.queryUserGradeById(merchantId,
                    Integer.parseInt(userInfo.getGradeId()), userInfo.getId());
            if (userGrade == null || userGrade.getDiscount() == null || userGrade.getDiscount() <= 0) {
                return ZERO;
            }

            // 会员折扣率 = discount / 10，例如 discount=8 表示 8折，即 0.8
            // 与saveOrder第396行保持一致
            BigDecimal percent = new BigDecimal(userGrade.getDiscount())
                    .divide(new BigDecimal("10"), BigDecimal.ROUND_CEILING, 3);
            if (percent.compareTo(ZERO) <= 0) {
                return ZERO;
            }
            return percent;
        } catch (Exception e) {
            return ZERO;
        }
    }

    /**
     * 购物车计算结果内部类
     */
    private static class CartCalculationResult {
        private final List<ResCartDto> cartDtoList;
        private final int totalNum;
        private final BigDecimal totalPrice;
        private final BigDecimal totalCanUsePointAmount;

        public CartCalculationResult(List<ResCartDto> cartDtoList, int totalNum, BigDecimal totalPrice, BigDecimal totalCanUsePointAmount) {
            this.cartDtoList = cartDtoList;
            this.totalNum = totalNum;
            this.totalPrice = totalPrice;
            this.totalCanUsePointAmount = totalCanUsePointAmount;
        }

        public List<ResCartDto> getCartDtoList() {
            return cartDtoList;
        }

        public int getTotalNum() {
            return totalNum;
        }

        public BigDecimal getTotalPrice() {
            return totalPrice;
        }

        public BigDecimal getTotalCanUsePointAmount() {
            return totalCanUsePointAmount;
        }
    }

    /**
     * 卡券使用结果内部类
     */
    private static class CouponUsageResult {
        private MtCoupon useCouponInfo;
        private BigDecimal couponAmount;

        public CouponUsageResult(MtCoupon useCouponInfo, BigDecimal couponAmount) {
            this.useCouponInfo = useCouponInfo;
            this.couponAmount = couponAmount;
        }

        public MtCoupon getUseCouponInfo() {
            return useCouponInfo;
        }

        public void setUseCouponInfo(MtCoupon useCouponInfo) {
            this.useCouponInfo = useCouponInfo;
        }

        public BigDecimal getCouponAmount() {
            return couponAmount;
        }

        public void setCouponAmount(BigDecimal couponAmount) {
            this.couponAmount = couponAmount;
        }
    }

    /**
     * 积分使用结果内部类
     */
    private static class PointUsageResult {
        private final int myPoint;
        private final int usePoint;
        private final BigDecimal usePointAmount;

        public PointUsageResult(int myPoint, int usePoint, BigDecimal usePointAmount) {
            this.myPoint = myPoint;
            this.usePoint = usePoint;
            this.usePointAmount = usePointAmount;
        }

        public int getMyPoint() {
            return myPoint;
        }

        public int getUsePoint() {
            return usePoint;
        }

        public BigDecimal getUsePointAmount() {
            return usePointAmount;
        }
    }

    // --- Helper methods moved from OpenOrderController ---

    /**
     * 转换可用优惠券列表
     */
    private List<AvailableCouponVO> convertAvailableCoupons(List<Map<String, Object>> availableCouponsMap) {
        List<AvailableCouponVO> availableCoupons = new ArrayList<>();
        if (availableCouponsMap == null || availableCouponsMap.isEmpty()) {
            return availableCoupons;
        }

        for (Map<String, Object> couponMap : availableCouponsMap) {
            AvailableCouponVO couponVO = new AvailableCouponVO();
            couponVO.setUserCouponId(getIntegerValue(couponMap.get("userCouponId")));
            couponVO.setCouponId(getIntegerValue(couponMap.get("couponId")));
            couponVO.setCouponName((String) couponMap.get("couponName"));
            couponVO.setCouponType((String) couponMap.get("couponType"));
            couponVO.setDiscountAmount(getBigDecimalValue(couponMap.get("discountAmount")));
            couponVO.setUsable((String) couponMap.get("usable"));
            couponVO.setDescription((String) couponMap.get("description"));
            couponVO.setSelected(Boolean.TRUE.equals(couponMap.get("selected")));
            
            // 设置余额（储值卡）
            if (couponMap.get("balance") != null) {
                couponVO.setBalance(getBigDecimalValue(couponMap.get("balance")));
            }

            // 解析有效期字符串为开始和结束时间
            String effectiveDate = (String) couponMap.get("effectiveDate");
            if (StringUtils.isNotEmpty(effectiveDate) && effectiveDate.contains("~")) {
                try {
                    String[] dates = effectiveDate.split("~");
                    if (dates.length == 2) {
                        Date startTime = parseDateFromString(dates[0].trim());
                        Date endTime = parseDateFromString(dates[1].trim());
                        couponVO.setEffectiveStartTime(startTime);
                        couponVO.setEffectiveEndTime(endTime);
                    }
                } catch (Exception e) {
                    // ignore
                }
            }

            // 如果优惠券不可用，设置不可用原因
            if (!"A".equals(couponVO.getUsable())) {
                couponVO.setUnusableReason(getUnusableReason(couponVO));
            }

            availableCoupons.add(couponVO);
        }

        return availableCoupons;
    }

    /**
     * 从字符串解析日期
     */
    private Date parseDateFromString(String dateStr) {
        if (StringUtils.isEmpty(dateStr)) {
            return null;
        }
        try {
            // 尝试解析 "yyyy.MM.dd HH:mm" 格式
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy.MM.dd HH:mm");
            return sdf.parse(dateStr);
        } catch (Exception e) {
            try {
                // 尝试解析 "yyyy-MM-dd HH:mm:ss" 格式
                java.text.SimpleDateFormat sdf2 = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                return sdf2.parse(dateStr);
            } catch (Exception e2) {
                return null;
            }
        }
    }

    /**
     * 获取优惠券不可用原因
     */
    private String getUnusableReason(AvailableCouponVO couponVO) {
        if (couponVO == null) {
            return "优惠券不可用";
        }
        // 根据优惠券状态判断不可用原因
        if (StringUtils.isEmpty(couponVO.getUsable()) || "N".equals(couponVO.getUsable())) {
            if (StringUtils.isNotEmpty(couponVO.getDescription()) && couponVO.getDescription().contains("满")) {
                return "订单金额不满足使用条件";
            }
            return "优惠券不可用";
        }
        return null;
    }

    /**
     * 转换商品列表
     */
    private List<OrderGoodsDetailVO> convertGoodsList(List<ResCartDto> goodsListDto, Integer userId, Integer merchantId) {
        List<OrderGoodsDetailVO> goodsList = new ArrayList<>();
        if (goodsListDto == null || goodsListDto.isEmpty()) {
            return goodsList;
        }

        String basePath = settingService.getUploadBasePath();

        for (ResCartDto cartDto : goodsListDto) {
            OrderGoodsDetailVO goodsVO = new OrderGoodsDetailVO();
            goodsVO.setGoodsId(cartDto.getGoodsId());
            goodsVO.setSkuId(cartDto.getSkuId() != null ? cartDto.getSkuId() : 0);
            goodsVO.setQuantity(cartDto.getNum() != null ? cartDto.getNum() : 0);
            goodsVO.setIsEffect(cartDto.getIsEffect() != null ? cartDto.getIsEffect() : true);
            goodsVO.setSpecList(cartDto.getSpecList());

            MtGoods goodsInfo = cartDto.getGoodsInfo();
            if (goodsInfo != null) {
                goodsVO.setGoodsName(goodsInfo.getName());
                goodsVO.setPrice(goodsInfo.getPrice() != null ? goodsInfo.getPrice() : BigDecimal.ZERO);
                goodsVO.setLinePrice(goodsInfo.getLinePrice() != null ? goodsInfo.getLinePrice() : BigDecimal.ZERO);
                
                // 处理商品图片
                String logo = goodsInfo.getLogo();
                if (StringUtils.isNotEmpty(logo) && !logo.startsWith("http")) {
                    logo = basePath + logo;
                }
                goodsVO.setGoodsImage(logo);

                // 计算小计（使用当前价格，已应用会员折扣）
                BigDecimal subtotal = goodsVO.getPrice().multiply(new BigDecimal(goodsVO.getQuantity()));
                goodsVO.setSubtotal(subtotal);

                // 计算会员折扣金额（单个商品）
                // 如果原价存在且大于当前价，说明有折扣
                if (goodsVO.getLinePrice() != null && goodsVO.getLinePrice().compareTo(goodsVO.getPrice()) > 0) {
                    BigDecimal originalSubtotal = goodsVO.getLinePrice().multiply(new BigDecimal(goodsVO.getQuantity()));
                    BigDecimal discountAmount = originalSubtotal.subtract(subtotal);
                    goodsVO.setMemberDiscount(discountAmount.max(BigDecimal.ZERO));
                } else {
                    goodsVO.setMemberDiscount(BigDecimal.ZERO);
                }
            } else {
                goodsVO.setGoodsName("");
                goodsVO.setPrice(BigDecimal.ZERO);
                goodsVO.setLinePrice(BigDecimal.ZERO);
                goodsVO.setSubtotal(BigDecimal.ZERO);
                goodsVO.setMemberDiscount(BigDecimal.ZERO);
            }

            goodsList.add(goodsVO);
        }

        return goodsList;
    }

}
