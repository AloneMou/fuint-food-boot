package com.fuint.openapi.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fuint.common.dto.*;
import com.fuint.common.enums.*;
import com.fuint.common.service.*;
import com.fuint.common.util.CommonUtil;
import com.fuint.common.util.DateUtil;
import com.fuint.framework.annoation.OperationServiceLog;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.exception.ServiceException;
import com.fuint.framework.util.SeqUtil;
import com.fuint.framework.util.spring.SpringUtils;
import com.fuint.openapi.service.OpenApiOrderService;
import com.fuint.openapi.v1.order.vo.OrderCreateReqVO;
import com.fuint.openapi.v1.order.vo.OrderGoodsItemVO;
import com.fuint.openapi.v1.order.vo.UserOrderRespVO;
import com.fuint.repository.mapper.*;
import com.fuint.repository.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import weixin.popular.util.JsonUtil;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.fuint.framework.exception.util.ServiceExceptionUtil.exception;
import static com.fuint.framework.util.string.StrUtils.isHttp;
import static com.fuint.openapi.enums.GoodsErrorCodeConstants.GOODS_SKU_NOT_ENOUGH;
import static com.fuint.openapi.enums.GoodsErrorCodeConstants.GOODS_SKU_NOT_EXIST;
import static com.fuint.openapi.enums.OrderErrorCodeConstants.*;
import static com.fuint.openapi.enums.UserErrorCodeConstants.USER_NOT_FOUND;

/**
 * Foot-Fuint-Backend-master
 *
 * @author mjw
 * @since 2026/1/18 15:24
 */
@Slf4j
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
    private MtUserCouponMapper mtUserCouponMapper;

    @Resource
    private MtGoodsSkuMapper mtGoodsSkuMapper;

    @Resource
    private MtCouponGoodsMapper mtCouponGoodsMapper;

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
    private MtConfirmLogMapper mtConfirmLogMapper;

    @Resource
    private MtUserGradeMapper mtUserGradeMapper;

    @Resource
    private OrderService orderService;

    @Resource
    private MtRegionMapper mtRegionMapper;

    @Resource
    private MtStoreMapper mtStoreMapper;

    @Resource
    private MtUserMapper mtUserMapper;

    @Resource
    private MtTableMapper mtTableMapper;

    @Resource
    private MtStaffMapper mtStaffMapper;

    @Resource
    private MtGoodsSpecMapper mtGoodsSpecMapper;


    @Override
    public Map<String, Object> calculateCartGoods(Integer merchantId, Integer userId, List<MtCart> cartList, Integer couponId, boolean isUsePoint, String platform, String orderMode) throws BusinessCheckException {
        MtUser userInfo = memberService.queryMemberById(userId);
        // 检查是否可以使用积分抵扣
        isUsePoint = checkCanUsePoint(merchantId, isUsePoint);
        // 处理购物车商品列表
        CartCalculationResult cartResult = processCartItems(cartList, orderMode);
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
    public Map<String, Object> preCreateOrder(Integer merchantId, Integer userId, List<MtCart> cartList, Integer userCouponId, Integer usePoint, String platform, String orderMode, Integer storeId) throws BusinessCheckException {
        // 参数校验
        MtUser userInfo = memberService.queryMemberById(userId);
        if (userInfo == null) {
            throw new ServiceException(USER_NOT_FOUND);
        }
        if (cartList == null || cartList.isEmpty()) {
            throw new ServiceException(GOODS_NOT_EMPTY);
        }

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
        List<Map<String, Object>> availableCoupons = buildAvailableCouponsList(couponList, selectedCouponId);

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

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("totalAmount", totalPrice);
        result.put("discountAmount", couponAmount);
        result.put("pointAmount", usePointAmount);
        result.put("memberDiscountAmount", memberDiscountAmount);
        result.put("deliveryFee", deliveryFee);
        result.put("payableAmount", payableAmount);
        result.put("usePoint", calculatedUsePoint);
        result.put("availablePoint", myPoint);
        result.put("availableCoupons", availableCoupons);
        result.put("selectedCouponId", selectedCouponId);
        result.put("goodsList", goodsList);
        result.put("calculateTime", new Date());
        return result;
    }

    /**
     * 保存订单信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "提交订单信息")
    public MtOrder saveOrder(OrderCreateReqVO createReqVO) {
        Integer merchantId = createReqVO.getMerchantId();
        MtUser userInfo = memberService.queryMemberById(createReqVO.getUserId());
        if (userInfo == null) {
            throw exception(USER_NOT_FOUND);
        }
        Integer storeId = createReqVO.getStoreId();
        if (ObjectUtil.isNotNull(createReqVO.getTableId())) {
            MtTable mtTable = tableService.queryTableById(createReqVO.getTableId());
            if (mtTable != null && mtTable.getStoreId() > 0) {
                storeId = mtTable.getStoreId();
            }
        }
        if (createReqVO.getStoreId() != null && ObjectUtil.notEqual(createReqVO.getStoreId(), storeId)) {
            throw exception(ORDER_STORE_NOT_EQUAL_TO_DESK);
        }
        MtSetting config = settingService.querySettingByName(merchantId, storeId, SettingTypeEnum.ORDER.getKey(), OrderSettingEnum.IS_CLOSE.getKey());
        if (config != null && config.getValue().equals(YesOrNoEnum.TRUE.getKey())) {
            throw exception(SYSTEM_CLOSED_TRANSACTION);
        }
        MtOrder mtOrder = new MtOrder();
        // 检查店铺是否已被禁用
        if (createReqVO.getStoreId() != null && createReqVO.getStoreId() > 0) {
            MtStore storeInfo = storeService.queryStoreById(createReqVO.getStoreId());
            if (storeInfo != null) {
                if (!storeInfo.getStatus().equals(StatusEnum.ENABLED.getKey())) {
                    createReqVO.setStoreId(0);
                }
            }
        }
        MtSetting pointSetting = settingService.querySettingByName(merchantId, SettingTypeEnum.POINT.getKey(), PointSettingEnum.CAN_USE_AS_MONEY.getKey());
        // 使用积分数量
        if (pointSetting != null && pointSetting.getValue().equals(YesOrNoEnum.TRUE.getKey())) {
            mtOrder.setUsePoint(createReqVO.getUsePoint());
            if (createReqVO.getUsePoint() == null || createReqVO.getUsePoint() <= 0) {
                mtOrder.setUsePoint(0);
            }
        } else {
            if (createReqVO.getUsePoint() != null && createReqVO.getUsePoint() > 0) {
                throw exception(ORDER_USE_POINT_NOT_ALLOWED);
            }
            mtOrder.setUsePoint(0);
        }


        String orderSn = CommonUtil.createOrderSN(createReqVO.getUserId() + "");
        mtOrder.setOrderSn(orderSn);
        mtOrder.setUserId(createReqVO.getUserId());
        mtOrder.setMerchantId(createReqVO.getMerchantId());
        mtOrder.setStoreId(createReqVO.getStoreId());
        mtOrder.setTableId(createReqVO.getTableId());
        mtOrder.setCouponId(createReqVO.getUserCouponId());
        mtOrder.setRemark(createReqVO.getRemark());
        mtOrder.setStatus(OrderStatusEnum.CREATED.getKey());
        mtOrder.setType(createReqVO.getType().getKey());
        mtOrder.setAmount(BigDecimal.ZERO);
        mtOrder.setPayAmount(BigDecimal.ZERO);
        mtOrder.setDiscount(BigDecimal.ZERO);
        mtOrder.setPayStatus(PayStatusEnum.WAIT.getKey());
        mtOrder.setPlatform(createReqVO.getPlatform());
        mtOrder.setPointAmount(BigDecimal.ZERO);
        mtOrder.setOrderMode(createReqVO.getOrderMode());
        mtOrder.setPayType(createReqVO.getPayType());
        mtOrder.setOperator("OPEN_API");
        mtOrder.setStaffId(null);
        mtOrder.setIsVisitor(YesOrNoEnum.NO.getKey());
        mtOrder.setUpdateTime(new Date());
        mtOrder.setDeliveryFee(BigDecimal.ZERO);
        mtOrder.setSettleStatus(SettleStatusEnum.WAIT.getKey());
        mtOrder.setTakeStatus(TakeStatusEnum.PENDING.getKey());
        if (mtOrder.getId() == null || mtOrder.getId() <= 0) {
            mtOrder.setCreateTime(new Date());
        }
        if (mtOrder.getVerifyCode() == null && !createReqVO.getPlatform().equals(PlatformTypeEnum.PC.getCode())) {
            mtOrder.setVerifyCode(SeqUtil.getRandomNumber(6));
        }
        // 首先生成订单
        mtOrderMapper.insert(mtOrder);
        MtOrder orderInfo = mtOrderMapper.selectById(mtOrder.getId());
        mtOrder.setId(orderInfo.getId());

        MtUserGrade userGrade = userGradeService.queryUserGradeById(createReqVO.getMerchantId(), userInfo.getGradeId() != null ? Integer.parseInt(userInfo.getGradeId()) : 1, createReqVO.getUserId());
        BigDecimal percent = new BigDecimal("0");
        if (userGrade != null && userGrade.getDiscount() != null && userGrade.getDiscount() > 0) {
            // 会员折扣
            percent = BigDecimal.valueOf(userGrade.getDiscount()).divide(new BigDecimal("10"), BigDecimal.ROUND_CEILING, RoundingMode.FLOOR);
            if (percent.compareTo(new BigDecimal("0")) <= 0) {
                percent = new BigDecimal("1");
            }
        }

        // 如果没有指定店铺，则读取默认的店铺
        if (createReqVO.getStoreId() == null || createReqVO.getStoreId() <= 0) {
            Map<String, Object> params = new HashMap<>();
            params.put("status", StatusEnum.ENABLED.getKey());
            params.put("is_default", YesOrNoEnum.YES.getKey());
            List<MtStore> storeList = storeService.queryStoresByParams(params);
            if (!storeList.isEmpty()) {
                mtOrder.setStoreId(storeList.get(0).getId());
            } else {
                mtOrder.setStoreId(0);
            }
        }

        mtOrder.setUpdateTime(new Date());
        if (mtOrder.getCreateTime() == null) {
            mtOrder.setCreateTime(new Date());
        }
        List<MtCart> cartList = new ArrayList<>();
        if (createReqVO.getType().equals(OrderTypeEnum.GOODS)) {
            for (OrderGoodsItemVO item : createReqVO.getItems()) {
                MtCart mtCart = new MtCart();
                mtCart.setGoodsId(item.getGoodsId());
                mtCart.setSkuId(item.getSkuId());
                mtCart.setNum(item.getQuantity());
                mtCart.setId(0);
                mtCart.setUserId(createReqVO.getUserId());
                mtCart.setStatus(StatusEnum.ENABLED.getKey());
                cartList.add(mtCart);
            }
        }

        boolean isUsePoint = mtOrder.getUsePoint() > 0;
        Map<String, Object> cartData = calculateCartGoods(orderInfo.getMerchantId(), mtOrder.getUserId(), cartList, mtOrder.getCouponId(), isUsePoint, mtOrder.getPlatform(), orderInfo.getOrderMode());
        mtOrder.setAmount(new BigDecimal(cartData.get("totalPrice").toString()));
        mtOrder.setUsePoint(Integer.parseInt(cartData.get("usePoint").toString()));
        mtOrder.setDiscount(new BigDecimal(cartData.get("couponAmount").toString()));

        // 实付金额
        BigDecimal payAmount = mtOrder.getAmount().subtract(mtOrder.getPointAmount()).subtract(mtOrder.getDiscount());
        if (payAmount.compareTo(new BigDecimal("0")) > 0) {
            mtOrder.setPayAmount(payAmount);
        } else {
            mtOrder.setPayAmount(new BigDecimal("0"));
        }
        // 购物使用了卡券
        if (ObjectUtil.isNotNull(mtOrder.getCouponId())) {
            // 查询是否适用商品
            MtUserCoupon userCoupon = mtUserCouponMapper.selectById(mtOrder.getCouponId());
            if (userCoupon == null) {
                throw exception(USER_COUPON_NOT_FOUND);
            }
            if (!userCoupon.getUserId().equals(createReqVO.getUserId())) {
                throw exception(USER_COUPON_NOT_BELONG_TO_USER);
            }
            MtCoupon couponInfo = couponService.queryCouponById(userCoupon.getCouponId());
            if (couponInfo.getApplyGoods() != null && couponInfo.getApplyGoods().equals(ApplyGoodsEnum.PARK_GOODS.getKey())) {
                List<MtCouponGoods> couponGoodsList = mtCouponGoodsMapper.getCouponGoods(couponInfo.getId());
                if (couponGoodsList != null && !couponGoodsList.isEmpty() && !cartList.isEmpty()) {
                    List<Integer> applyGoodsIds = new ArrayList<>();
                    List<Integer> goodsIds = new ArrayList<>();
                    for (MtCouponGoods mtCouponGoods : couponGoodsList) {
                        applyGoodsIds.add(mtCouponGoods.getGoodsId());
                    }
                    for (MtCart mtCart : cartList) {
                        goodsIds.add(mtCart.getGoodsId());
                    }
                    List<Integer> intersection = applyGoodsIds.stream()
                            .filter(goodsIds::contains)
                            .collect(Collectors.toList());
                    if (intersection.isEmpty()) {
                        throw new BusinessCheckException("该卡券不适用于购买的商品列表");
                    }
                }
            }
            updateOrder(mtOrder);
            String useCode = couponService.useCoupon(mtOrder.getCouponId(), mtOrder.getUserId(), mtOrder.getStoreId(), mtOrder.getId(), mtOrder.getDiscount(), "购物使用卡券");
            // 卡券使用失败
            if (StringUtils.isEmpty(useCode)) {
                mtOrder.setDiscount(new BigDecimal("0"));
                mtOrder.setCouponId(0);
            }
        }

        // 会员付款类订单
        if (createReqVO.getType().equals(OrderTypeEnum.PAYMENT)) {
            if (userInfo.getGradeId() != null && mtOrder.getIsVisitor().equals(YesOrNoEnum.NO.getKey())) {
                if (percent.compareTo(new BigDecimal("0")) > 0) {
                    // 会员折扣
                    BigDecimal payAmountDiscount = mtOrder.getPayAmount().multiply(percent);
                    if (payAmountDiscount.compareTo(new BigDecimal("0")) > 0) {
                        mtOrder.setDiscount(mtOrder.getDiscount().add(mtOrder.getPayAmount().subtract(payAmountDiscount)));
                        mtOrder.setPayAmount(payAmountDiscount);
                    } else {
                        mtOrder.setPayAmount(new BigDecimal("0"));
                    }
                }
            }
        }

        // 再次更新订单
        try {
            orderInfo = updateOrder(mtOrder);
        } catch (
                Exception e) {
            log.error("OrderService 生成订单失败...");
            throw new BusinessCheckException("生成订单失败，请稍后重试");
        }

        // 扣减积分
        if (mtOrder.getUsePoint() > 0) {
            try {
                MtPoint reqPointDto = new MtPoint();
                reqPointDto.setUserId(mtOrder.getUserId());
                reqPointDto.setAmount(-mtOrder.getUsePoint());
                reqPointDto.setOrderSn(orderSn);
                reqPointDto.setDescription("支付扣除" + mtOrder.getUsePoint() + "积分");
                reqPointDto.setOperator("");
                pointService.addPoint(reqPointDto);
            } catch (BusinessCheckException e) {
                log.error("OrderService 扣减积分失败...{}", e.getMessage());
                throw new BusinessCheckException("扣减积分失败，请稍后重试");
            }
        }

        // 如果是商品订单，生成订单商品
        if (mtOrder.getType().equals(OrderTypeEnum.GOODS.getKey()) && !cartList.isEmpty()) {
            Object listObject = cartData.get("list");
            List<ResCartDto> lists = (ArrayList<ResCartDto>) listObject;
            BigDecimal memberDiscount = new BigDecimal("0");
            for (ResCartDto cart : lists) {
                MtOrderGoods orderGoods = new MtOrderGoods();
                orderGoods.setOrderId(orderInfo.getId());
                orderGoods.setGoodsId(cart.getGoodsId());
                orderGoods.setSkuId(cart.getSkuId());
                orderGoods.setNum(cart.getNum());
                // 计算会员折扣
                BigDecimal price = cart.getGoodsInfo().getPrice();
                boolean isDiscount = cart.getGoodsInfo().getIsMemberDiscount().equals(YesOrNoEnum.YES.getKey());
                if (percent.compareTo(new BigDecimal("0")) > 0 && isDiscount) {
                    orderGoods.setPrice(price.multiply(percent));
                    BigDecimal discount = price.subtract(price.multiply(percent)).multiply(new BigDecimal(cart.getNum()));
                    orderGoods.setDiscount(discount);
                    memberDiscount = memberDiscount.add(discount);
                } else {
                    orderGoods.setPrice(price);
                    orderGoods.setDiscount(new BigDecimal("0"));
                }
                orderGoods.setStatus(StatusEnum.ENABLED.getKey());
                orderGoods.setCreateTime(new Date());
                orderGoods.setUpdateTime(new Date());
                mtOrderGoodsMapper.insert(orderGoods);
                // 扣减库存
                MtGoods goodsInfo = mtGoodsMapper.selectById(cart.getGoodsId());
                if (goodsInfo.getIsSingleSpec().equals(YesOrNoEnum.YES.getKey())) {
                    // 单规格减去库存
                    int stock = goodsInfo.getStock() - cart.getNum();
                    if (stock < 0) {
                        throw new BusinessCheckException("商品“" + goodsInfo.getName() + "”库存不足，订单提交失败");
                    }
                    goodsInfo.setStock(stock);
                    mtGoodsMapper.updateById(goodsInfo);
                } else {
                    // 多规格减去库存
                    MtGoodsSku mtGoodsSku = mtGoodsSkuMapper.selectById(cart.getSkuId());
                    if (mtGoodsSku != null) {
                        int stock = mtGoodsSku.getStock() - cart.getNum();
                        if (stock < 0) {
                            throw new BusinessCheckException("商品sku编码“" + mtGoodsSku.getSkuNo() + "”库存不足，订单提交失败");
                        }
                        mtGoodsSku.setStock(stock);
                        mtGoodsSkuMapper.updateById(mtGoodsSku);
                        if (goodsInfo.getStock() != null && goodsInfo.getStock() > 0) {
                            int goodsStock = goodsInfo.getStock() - cart.getNum();
                            if (goodsStock >= 0) {
                                goodsInfo.setStock(goodsStock);
                                mtGoodsMapper.updateById(goodsInfo);
                            }
                        }
                    }
                }
                if (cart.getId() > 0) {
                    mtCartMapper.deleteById(cart.getId());
                }
            }

            // 会员折扣
            if (memberDiscount.compareTo(new BigDecimal("0")) > 0) {
                orderInfo.setDiscount(orderInfo.getDiscount().add(memberDiscount));
                if (orderInfo.getPayAmount().subtract(memberDiscount).compareTo(new BigDecimal("0")) > 0) {
                    orderInfo.setPayAmount(orderInfo.getPayAmount().subtract(memberDiscount));
                } else {
                    orderInfo.setPayAmount(new BigDecimal("0"));
                }
                orderInfo.setUpdateTime(new Date());
                orderInfo = updateOrder(orderInfo);
            }

            // 需要配送的订单，生成配送地址
            if (mtOrder.getOrderMode().equals(OrderModeEnum.EXPRESS.getKey())) {
                MtAddress mtAddress = null;
                if (ObjectUtil.isNotNull(createReqVO.getAddressId())) {
                    mtAddress = addressService.getById(createReqVO.getAddressId());
                    if (mtAddress == null) {
                        throw new BusinessCheckException("配送地址出错了，请重新选择配送地址");
                    }
                    if (ObjectUtil.equals(mtAddress.getUserId(), mtOrder.getUserId())) {
                        throw new BusinessCheckException("配送地址出错了，请重新选择配送地址");
                    }
                }
                if (ObjectUtil.isNull(mtAddress)) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("userId", mtOrder.getUserId().toString());
                    params.put("isDefault", YesOrNoEnum.YES.getKey());
                    List<MtAddress> addressList = addressService.queryListByParams(params);
                    if (!addressList.isEmpty()) {
                        mtAddress = addressList.get(0);
                    } else {
                        throw new BusinessCheckException("配送地址出错了，请重新选择配送地址");
                    }
                }
                MtOrderAddress orderAddress = new MtOrderAddress();
                orderAddress.setOrderId(orderInfo.getId());
                orderAddress.setUserId(mtOrder.getUserId());
                orderAddress.setName(mtAddress.getName());
                orderAddress.setMobile(mtAddress.getMobile());
                orderAddress.setCityId(mtAddress.getCityId());
                orderAddress.setProvinceId(mtAddress.getProvinceId());
                orderAddress.setRegionId(mtAddress.getRegionId());
                orderAddress.setDetail(mtAddress.getDetail());
                orderAddress.setCreateTime(new Date());
                mtOrderAddressMapper.insert(orderAddress);
            }
        }
        if (createReqVO.getPreTotalAmount().compareTo(orderInfo.getPayAmount()) != 0) {
            // 价格不一致
            throw exception(PRICE_NOT_CONSISTENT);
        }
        return orderInfo;
    }


    /**
     * 更新订单
     *
     * @param orderDto 订单参数
     * @return
     * @throws BusinessCheckException
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "更新订单信息")
    public MtOrder updateOrder(OrderDto orderDto) throws BusinessCheckException {
        log.info("orderService.updateOrder orderDto = {}", JsonUtil.toJSONString(orderDto));
        MtOrder mtOrder = mtOrderMapper.selectById(orderDto.getId());
        if (null == mtOrder || OrderStatusEnum.DELETED.getKey().equals(mtOrder.getStatus())) {
            throw new BusinessCheckException("该订单状态异常");
        }

        mtOrder.setId(orderDto.getId());
        mtOrder.setUpdateTime(new Date());

        if (null != orderDto.getOperator()) {
            mtOrder.setOperator(orderDto.getOperator());
        }

        if (null != orderDto.getStatus()) {
            if (orderDto.getStatus().equals(OrderStatusEnum.CANCEL.getKey()) || orderDto.getStatus().equals(OrderStatusEnum.CREATED.getKey())) {
                orderDto.setPayStatus(PayStatusEnum.WAIT.getKey());
            }
            if (orderDto.getStatus().equals(OrderStatusEnum.CANCEL.getKey())) {
                cancelOrder(orderDto.getId(), "取消订单");
            } else {
                mtOrder.setStatus(orderDto.getStatus());
            }
            if (orderDto.getStatus().equals(OrderStatusEnum.PAID.getKey())) {
                mtOrder.setPayStatus(PayStatusEnum.SUCCESS.getKey());
                mtOrder.setPayTime(new Date());
            }
        }

        if (null != orderDto.getPayAmount()) {
            mtOrder.setPayAmount(orderDto.getPayAmount());
        }

        if (null != orderDto.getAmount()) {
            mtOrder.setAmount(orderDto.getAmount());
        }

        if (null != orderDto.getVerifyCode() && StringUtils.isNotEmpty(orderDto.getVerifyCode())) {
            if (orderDto.getVerifyCode().equals(mtOrder.getVerifyCode())) {
                mtOrder.setStatus(OrderStatusEnum.DELIVERED.getKey());
                mtOrder.setVerifyCode("");
            } else {
                throw new BusinessCheckException("核销码错误，请确认！");
            }
        }

        if (null != orderDto.getDiscount()) {
            mtOrder.setDiscount(orderDto.getDiscount());
        }

        if (null != orderDto.getPayTime()) {
            mtOrder.setPayTime(orderDto.getPayTime());
        }

        if (null != orderDto.getPayType()) {
            mtOrder.setPayType(orderDto.getPayType());
        }

        if (null != orderDto.getPayStatus()) {
            mtOrder.setPayStatus(orderDto.getPayStatus());
        }

        if (null != orderDto.getExpressInfo()) {
            mtOrder.setExpressInfo(JSONObject.toJSONString(orderDto.getExpressInfo()));
        }

        if (null != orderDto.getOrderMode()) {
            mtOrder.setOrderMode(orderDto.getOrderMode());
        }

        if (null != orderDto.getRemark()) {
            mtOrder.setRemark(orderDto.getRemark());
        }

        mtOrderMapper.updateById(mtOrder);
        return mtOrder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "取消订单")
    public MtOrder cancelOrder(Integer orderId, String remark) {
        MtOrder mtOrder = mtOrderMapper.selectById(orderId);
        if (mtOrder != null && mtOrder.getStatus().equals(OrderStatusEnum.CREATED.getKey()) && mtOrder.getPayStatus().equals(PayStatusEnum.WAIT.getKey())) {
            if (StringUtils.isNotEmpty(remark)) {
                mtOrder.setRemark(remark);
            }

            mtOrder.setStatus(OrderStatusEnum.CANCEL.getKey());
            mtOrderMapper.updateById(mtOrder);

            // 返还积分
            if (mtOrder.getPointAmount() != null && mtOrder.getUsePoint() > 0) {
                MtPoint reqPointDto = new MtPoint();
                reqPointDto.setUserId(mtOrder.getUserId());
                reqPointDto.setAmount(mtOrder.getUsePoint());
                reqPointDto.setDescription("订单取消" + mtOrder.getOrderSn() + "退回" + mtOrder.getUsePoint() + "积分");
                reqPointDto.setOrderSn(mtOrder.getOrderSn());
                reqPointDto.setOperator("");
                pointService.addPoint(reqPointDto);
            }

            // 返还卡券
            List<MtConfirmLog> confirmLogList = mtConfirmLogMapper.getOrderConfirmLogList(mtOrder.getId());
            if (confirmLogList.size() > 0) {
                for (MtConfirmLog log : confirmLogList) {
                    MtCoupon couponInfo = couponService.queryCouponById(log.getCouponId());
                    MtUserCoupon userCouponInfo = mtUserCouponMapper.selectById(log.getUserCouponId());

                    if (userCouponInfo != null) {
                        // 优惠券直接置为未使用
                        if (couponInfo.getType().equals(CouponTypeEnum.COUPON.getKey())) {
                            userCouponInfo.setStatus(UserCouponStatusEnum.UNUSED.getKey());
                            mtUserCouponMapper.updateById(userCouponInfo);
                        }

                        // 储值卡把余额加回去
                        if (couponInfo.getType().equals(CouponTypeEnum.PRESTORE.getKey())) {
                            BigDecimal balance = userCouponInfo.getBalance();
                            BigDecimal newBalance = balance.add(log.getAmount());
                            if (newBalance.compareTo(userCouponInfo.getAmount()) <= 0) {
                                userCouponInfo.setBalance(newBalance);
                                userCouponInfo.setStatus(UserCouponStatusEnum.UNUSED.getKey());
                            }
                            mtUserCouponMapper.updateById(userCouponInfo);
                        }

                        // 撤销核销记录
                        log.setStatus(StatusEnum.DISABLE.getKey());
                        mtConfirmLogMapper.updateById(log);
                    }
                }
            }

            // 返还库存
            Map<String, Object> params = new HashMap<>();
            params.put("ORDER_ID", mtOrder.getId());
            List<MtOrderGoods> orderGoodsList = mtOrderGoodsMapper.selectByMap(params);
            if (orderGoodsList != null && orderGoodsList.size() > 0) {
                for (MtOrderGoods mtOrderGoods : orderGoodsList) {
                    MtGoods mtGoods = mtGoodsMapper.selectById(mtOrderGoods.getGoodsId());
                    // 商品已不存在
                    if (mtGoods == null) {
                        continue;
                    }
                    mtGoods.setStock(mtOrderGoods.getNum() + mtGoods.getStock());
                    mtGoodsMapper.updateById(mtGoods);
                    if (mtOrderGoods.getSkuId() != null && mtOrderGoods.getSkuId() > 0) {
                        MtGoodsSku mtGoodsSku = mtGoodsSkuMapper.selectById(mtOrderGoods.getSkuId());
                        if (mtGoodsSku != null && mtGoodsSku.getStock() != null && mtOrderGoods.getNum() != null) {
                            mtGoodsSku.setStock(mtGoodsSku.getStock() + mtOrderGoods.getNum());
                            mtGoodsSkuMapper.updateById(mtGoodsSku);
                        }
                    }
                }
            }
        }

        return mtOrder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MtOrder updateOrder(MtOrder mtOrder) {
        mtOrder.setUpdateTime(new Date());
        int id = mtOrderMapper.updateById(mtOrder);
        if (id > 0) {
            mtOrder = mtOrderMapper.selectById(mtOrder.getId());
        }
        return mtOrder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "修改订单为已支付")
    public boolean setOrderPayed(Integer orderId, BigDecimal payAmount) throws BusinessCheckException {
        MtOrder mtOrder = mtOrderMapper.selectById(orderId);
        if (mtOrder == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        if (mtOrder.getPayStatus().equals(PayStatusEnum.SUCCESS.getKey())) {
            throw exception(ORDER_ALREADY_PAID);
        }
        if (mtOrder.getPayAmount().compareTo(payAmount) != 0) {
            throw exception(ORDER_PAY_AMOUNT_ERROR);
        }
        OrderDto reqDto = new OrderDto();
        reqDto.setId(orderId);
        reqDto.setStatus(OrderStatusEnum.PAID.getKey());
        reqDto.setPayStatus(PayStatusEnum.SUCCESS.getKey());
        reqDto.setTakeStatus(TakeStatusEnum.PENDING.getKey());
        if (payAmount != null) {
            reqDto.setPayAmount(payAmount);
        }
        reqDto.setPayTime(new Date());
        reqDto.setUpdateTime(new Date());
        updateOrder(reqDto);
        // 处理会员升级订单
        if (mtOrder.getType().equals(OrderTypeEnum.MEMBER.getKey())) {
            openGiftService.openGift(mtOrder.getUserId(), Integer.parseInt(mtOrder.getParam()), false);
        }

        // 处理购物订单
        UserOrderDto orderInfo = orderService.getOrderByOrderSn(mtOrder.getOrderSn());
        if (orderInfo.getType().equals(OrderTypeEnum.GOODS.getKey())) {
            try {
                List<OrderGoodsDto> goodsList = orderInfo.getGoods();
                if (goodsList != null && !goodsList.isEmpty()) {
                    for (OrderGoodsDto goodsDto : goodsList) {
                        MtGoods mtGoods = goodsService.queryGoodsById(goodsDto.getGoodsId());
                        if (mtGoods != null) {
                            // 购买虚拟卡券商品发放处理
                            if (mtGoods.getType().equals(GoodsTypeEnum.COUPON.getKey()) && mtGoods.getCouponIds() != null && StringUtils.isNotEmpty(mtGoods.getCouponIds())) {
                                String[] couponIds = mtGoods.getCouponIds().split(",");
                                for (String couponId : couponIds) {
                                    userCouponService.buyCouponItem(orderInfo.getId(), Integer.parseInt(couponId), orderInfo.getUserId(), orderInfo.getUserInfo().getMobile(), goodsDto.getNum());
                                }
                            }
                            // 将已销售数量+1
                            goodsService.updateInitSale(mtGoods.getId());
                        }
                    }
                }
            } catch (BusinessCheckException e) {
                log.error("会员购买的卡券发送给会员失败......{}", e.getMessage());
            }
        }

        // 处理消费返积分，查询返1积分所需消费金额
        MtSetting setting = settingService.querySettingByName(mtOrder.getMerchantId(), SettingTypeEnum.POINT.getKey(), PointSettingEnum.POINT_NEED_CONSUME.getKey());
        if (setting != null && !orderInfo.getPayType().equals(PayTypeEnum.BALANCE.getKey()) && orderInfo.getIsVisitor().equals(YesOrNoEnum.NO.getKey())) {
            String needPayAmount = setting.getValue();
            int needPayAmountInt = Math.round(Integer.parseInt(needPayAmount));
            double pointNum = 0d;
            if (needPayAmountInt > 0 && orderInfo.getPayAmount().compareTo(new BigDecimal(needPayAmountInt)) >= 0) {
                BigDecimal point = orderInfo.getPayAmount().divide(new BigDecimal(needPayAmountInt), BigDecimal.ROUND_CEILING, RoundingMode.FLOOR);
                pointNum = Math.ceil(point.doubleValue());
            }
            log.info("PaymentService paymentCallback Point orderSn = {} , pointNum ={}", orderInfo.getOrderSn(), pointNum);
            if (pointNum > 0) {
                MtUser userInfo = memberService.queryMemberById(orderInfo.getUserId());
                MtUserGrade userGrade = userGradeService.queryUserGradeById(orderInfo.getMerchantId(), Integer.parseInt(userInfo.getGradeId()), orderInfo.getUserId());
                // 是否会员积分加倍
                if (userGrade != null && userGrade.getSpeedPoint() > 1) {
                    pointNum = pointNum * userGrade.getSpeedPoint();
                }
                MtPoint reqPointDto = new MtPoint();
                reqPointDto.setAmount((int) pointNum);
                reqPointDto.setUserId(orderInfo.getUserId());
                reqPointDto.setOrderSn(orderInfo.getOrderSn());
                reqPointDto.setDescription("支付￥" + orderInfo.getPayAmount() + "返" + pointNum + "积分");
                reqPointDto.setOperator("系统");
                pointService.addPoint(reqPointDto);
            }
        }

        // 计算是否要升级（购物订单、付款订单、充值订单）
        if (orderInfo.getIsVisitor().equals(YesOrNoEnum.NO.getKey()) && orderInfo.getType().equals(OrderTypeEnum.GOODS.getKey()) || orderInfo.getType().equals(OrderTypeEnum.PAYMENT.getKey()) || orderInfo.getType().equals(OrderTypeEnum.RECHARGE.getKey())) {
            try {
                if (orderInfo.getIsVisitor().equals(YesOrNoEnum.NO.getKey())) {
                    Map<String, Object> param = new HashMap<>();
                    param.put("MERCHANT_ID", mtOrder.getMerchantId());
                    param.put("STATUS", StatusEnum.ENABLED.getKey());
                    MtUser mtUser = memberService.queryMemberById(orderInfo.getUserId());
                    MtUserGrade mtUserGrade = mtUserGradeMapper.selectById(mtUser.getGradeId());
                    if (mtUserGrade == null) {
                        mtUserGrade = userGradeService.getInitUserGrade(orderInfo.getMerchantId());
                    }
                    List<MtUserGrade> userGradeList = mtUserGradeMapper.selectByMap(param);
                    if (mtUserGrade != null && userGradeList != null && userGradeList.size() > 0) {
                        // 会员已支付金额
                        BigDecimal payMoney = orderService.getUserPayMoney(orderInfo.getUserId());
                        // 会员支付订单笔数
                        Integer payOrderCount = orderService.getUserPayOrderCount(orderInfo.getUserId());
                        BigDecimal payOrderCountValue = new BigDecimal(payOrderCount);
                        for (MtUserGrade grade : userGradeList) {
                            if (grade.getCatchValue() != null && grade.getCatchType() != null) {
                                // 累计消费金额已达到
                                if (grade.getCatchType().equals(UserGradeCatchTypeEnum.AMOUNT.getKey())) {
                                    if (grade.getGrade().compareTo(mtUserGrade.getGrade()) > 0 && payMoney.compareTo(grade.getCatchValue()) >= 0) {
                                        openGiftService.openGift(mtOrder.getUserId(), grade.getId(), false);
                                    }
                                }
                                // 累计消费次数已达到
                                if (grade.getCatchType().equals(UserGradeCatchTypeEnum.FREQUENCY.getKey()) && payOrderCountValue.compareTo(grade.getCatchValue()) >= 0) {
                                    if (grade.getGrade().compareTo(mtUserGrade.getGrade()) > 0) {
                                        openGiftService.openGift(mtOrder.getUserId(), grade.getId(), false);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                log.error("会员升级出错啦，userId = {}，message = {}", orderInfo.getUserId(), ex.getMessage());
            }
        }

        try {
            // 打印订单
            printerService.printOrder(orderInfo);

            // 给商家发送通知短信
            MtStore mtStore = storeService.queryStoreById(mtOrder.getStoreId());
            if (mtStore != null && orderInfo.getIsVisitor().equals(YesOrNoEnum.NO.getKey())) {
                Map<String, String> params = new HashMap<>();
                params.put("orderSn", mtOrder.getOrderSn());
                List<String> mobileList = new ArrayList<>();
                mobileList.add(mtStore.getPhone());
                sendSmsService.sendSms(mtOrder.getMerchantId(), "new-order", mobileList, params);
            }
        } catch (Exception e) {
            log.error("给商家发送短信出错啦，message = {}", e.getMessage());
        }

        return true;
    }


    @Override
    public List<UserOrderRespVO> convertOrderList(List<MtOrder> orderList) {
        if (orderList == null || orderList.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. 收集ID
        Set<Integer> orderIds = orderList.stream().map(MtOrder::getId).collect(Collectors.toSet());
        Set<Integer> storeIds = orderList.stream().map(MtOrder::getStoreId).collect(Collectors.toSet());
        Set<Integer> userIds = orderList.stream().map(MtOrder::getUserId).collect(Collectors.toSet());
        Set<Integer> tableIds = orderList.stream().map(MtOrder::getTableId).filter(id -> id != null && id > 0).collect(Collectors.toSet());
        Set<Integer> staffIds = orderList.stream().map(MtOrder::getStaffId).filter(id -> id != null && id > 0).collect(Collectors.toSet());
        Set<Integer> couponIds = orderList.stream().map(MtOrder::getCouponId).filter(id -> id != null && id > 0).collect(Collectors.toSet());

        // 2. 批量查询基础信息
        Map<Integer, MtStore> storeMap = new HashMap<>();
        if (!storeIds.isEmpty()) {
            List<MtStore> stores = mtStoreMapper.selectBatchIds(storeIds);
            storeMap = stores.stream().collect(Collectors.toMap(MtStore::getId, Function.identity()));
        }

        Map<Integer, MtUser> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<MtUser> users = mtUserMapper.selectBatchIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(MtUser::getId, Function.identity()));
        }

        Map<Integer, MtTable> tableMap = new HashMap<>();
        if (!tableIds.isEmpty()) {
            List<MtTable> tables = mtTableMapper.selectBatchIds(tableIds);
            tableMap = tables.stream().collect(Collectors.toMap(MtTable::getId, Function.identity()));
        }

        Map<Integer, MtStaff> staffMap = new HashMap<>();
        if (!staffIds.isEmpty()) {
            List<MtStaff> staffs = mtStaffMapper.selectBatchIds(staffIds);
            staffMap = staffs.stream().collect(Collectors.toMap(MtStaff::getId, Function.identity()));
        }

        // 3. 批量查询订单商品
        Map<Integer, List<MtOrderGoods>> orderGoodsMap = new HashMap<>();
        Map<Integer, MtGoods> goodsMap = new HashMap<>();
        Map<Integer, List<GoodsSpecValueDto>> skuSpecMap = new HashMap<>();

        if (!orderIds.isEmpty()) {
            QueryWrapper<MtOrderGoods> goodsQuery = new QueryWrapper<>();
            goodsQuery.in("order_id", orderIds);
            List<MtOrderGoods> allOrderGoods = mtOrderGoodsMapper.selectList(goodsQuery);
            orderGoodsMap = allOrderGoods.stream().collect(Collectors.groupingBy(MtOrderGoods::getOrderId));

            Set<Integer> goodsIds = allOrderGoods.stream().map(MtOrderGoods::getGoodsId).collect(Collectors.toSet());
            Set<Integer> skuIds = allOrderGoods.stream().map(MtOrderGoods::getSkuId).filter(id -> id != null && id > 0).collect(Collectors.toSet());

            if (!goodsIds.isEmpty()) {
                List<MtGoods> goodsList = mtGoodsMapper.selectBatchIds(goodsIds);
                goodsMap = goodsList.stream().collect(Collectors.toMap(MtGoods::getId, Function.identity()));
            }

            if (!skuIds.isEmpty()) {
                List<MtGoodsSku> skuList = mtGoodsSkuMapper.selectBatchIds(skuIds);

                Set<Integer> allSpecIds = new HashSet<>();
                for (MtGoodsSku sku : skuList) {
                    if (StrUtil.isNotEmpty(sku.getSpecIds())) {
                        String[] ids = sku.getSpecIds().split("-");
                        for (String id : ids) {
                            allSpecIds.add(Integer.parseInt(id));
                        }
                    }
                }

                if (!allSpecIds.isEmpty()) {
                    List<MtGoodsSpec> specs = mtGoodsSpecMapper.selectBatchIds(allSpecIds);
                    Map<Integer, MtGoodsSpec> specMap = specs.stream().collect(Collectors.toMap(MtGoodsSpec::getId, Function.identity()));

                    for (MtGoodsSku sku : skuList) {
                        List<GoodsSpecValueDto> specDtos = new ArrayList<>();
                        if (StrUtil.isNotEmpty(sku.getSpecIds())) {
                            String[] ids = sku.getSpecIds().split("-");
                            for (String id : ids) {
                                MtGoodsSpec spec = specMap.get(Integer.parseInt(id));
                                if (spec != null) {
                                    GoodsSpecValueDto dto = new GoodsSpecValueDto();
                                    dto.setSpecValueId(spec.getId());
                                    dto.setSpecName(spec.getName());
                                    dto.setSpecValue(spec.getValue());
                                    specDtos.add(dto);
                                }
                            }
                        }
                        skuSpecMap.put(sku.getId(), specDtos);
                    }
                }
            }
        }

        // 4. 批量查询优惠券
        Map<Integer, MtUserCoupon> userCouponMap = new HashMap<>();
        Map<Integer, MtCoupon> couponInfoMap = new HashMap<>();
        if (!couponIds.isEmpty()) {
            List<MtUserCoupon> userCoupons = mtUserCouponMapper.selectBatchIds(couponIds);
            userCouponMap = userCoupons.stream().collect(Collectors.toMap(MtUserCoupon::getId, Function.identity()));

            Set<Integer> realCouponIds = userCoupons.stream().map(MtUserCoupon::getCouponId).collect(Collectors.toSet());
            if (!realCouponIds.isEmpty()) {
                List<MtCoupon> coupons = couponService.queryCouponListByIds(new ArrayList<>(realCouponIds));
                couponInfoMap = coupons.stream().collect(Collectors.toMap(MtCoupon::getId, Function.identity()));
            }
        }

        // 5. 批量查询订单地址
        Map<Integer, MtOrderAddress> addressMap = new HashMap<>();
        Map<Integer, String> regionNameMap = new HashMap<>();
        List<Integer> expressOrderIds = orderList.stream()
                .filter(o -> OrderModeEnum.EXPRESS.getKey().equals(o.getOrderMode()))
                .map(MtOrder::getId)
                .collect(Collectors.toList());

        if (!expressOrderIds.isEmpty()) {
            QueryWrapper<MtOrderAddress> addressQuery = new QueryWrapper<>();
            addressQuery.in("order_id", expressOrderIds);
            List<MtOrderAddress> addresses = mtOrderAddressMapper.selectList(addressQuery);
            addressMap = addresses.stream().collect(Collectors.toMap(MtOrderAddress::getOrderId, Function.identity(), (k1, k2) -> k1));

            Set<Integer> regionIds = new HashSet<>();
            addresses.forEach(a -> {
                if (a.getProvinceId() != null) regionIds.add(a.getProvinceId());
                if (a.getCityId() != null) regionIds.add(a.getCityId());
                if (a.getRegionId() != null) regionIds.add(a.getRegionId());
            });

            if (!regionIds.isEmpty()) {
                List<MtRegion> regions = mtRegionMapper.selectBatchIds(regionIds);
                regionNameMap = regions.stream().collect(Collectors.toMap(MtRegion::getId, MtRegion::getName));
            }
        }

        // 6. 组装结果
        List<UserOrderRespVO> resultList = new ArrayList<>();
        String basePath = settingService.getUploadBasePath();

        for (MtOrder mtOrder : orderList) {
            UserOrderRespVO vo = new UserOrderRespVO();
            BeanUtils.copyProperties(mtOrder, vo);

            vo.setType(OrderTypeEnum.getEnum(mtOrder.getType()));
            vo.setPayType(PayTypeEnum.getEnum(mtOrder.getPayType()));
            vo.setOrderMode(OrderModeEnum.getEnum(mtOrder.getOrderMode()));
            vo.setStatus(OrderStatusEnum.getEnum(mtOrder.getStatus()));
            vo.setPayStatus(PayStatusEnum.getEnum(mtOrder.getPayStatus()));
            vo.setTakeStatus(TakeStatusEnum.getEnum(mtOrder.getTakeStatus()));
            vo.setSettleStatus(SettleStatusEnum.getEnum(mtOrder.getSettleStatus()));

            vo.setPayAmount(ObjectUtil.defaultIfNull(mtOrder.getPayAmount(), BigDecimal.ZERO));
            vo.setDiscount(ObjectUtil.defaultIfNull(mtOrder.getDiscount(), BigDecimal.ZERO));
            vo.setPointAmount(ObjectUtil.defaultIfNull(mtOrder.getPointAmount(), BigDecimal.ZERO));
            vo.setUsePoint(ObjectUtil.defaultIfNull(mtOrder.getUsePoint(), 0));
            vo.setTypeName(vo.getType().getValue());
            vo.setStatusText(vo.getStatus().getValue());
            vo.setIsVerify(StrUtil.isEmpty(mtOrder.getVerifyCode()));

            if (storeMap.containsKey(mtOrder.getStoreId())) {
                MtStore store = storeMap.get(mtOrder.getStoreId());
                UserOrderRespVO.OrderStoreRespVO storeVO = new UserOrderRespVO.OrderStoreRespVO();
                BeanUtils.copyProperties(store, storeVO);
                vo.setStoreInfo(storeVO);
            }

            if (userMap.containsKey(mtOrder.getUserId())) {
                MtUser user = userMap.get(mtOrder.getUserId());
                UserOrderRespVO.OrderUserRespVO userVO = new UserOrderRespVO.OrderUserRespVO();
                userVO.setId(user.getId());
                userVO.setName(user.getName());
                userVO.setMobile(user.getMobile());
                userVO.setNo(user.getUserNo());
                vo.setUserInfo(userVO);
            }

            if (tableMap.containsKey(mtOrder.getTableId())) {
                MtTable table = tableMap.get(mtOrder.getTableId());
                UserOrderRespVO.OrderTableRespVO tableVO = new UserOrderRespVO.OrderTableRespVO();
                BeanUtils.copyProperties(table, tableVO);
                vo.setTableInfo(tableVO);
            }

            if (staffMap.containsKey(mtOrder.getStaffId())) {
                MtStaff staff = staffMap.get(mtOrder.getStaffId());
                UserOrderRespVO.OrderStaffRespVO staffVO = new UserOrderRespVO.OrderStaffRespVO();
                BeanUtils.copyProperties(staff, staffVO);
                vo.setStaffInfo(staffVO);
            }

            List<MtOrderGoods> orderGoodsList = orderGoodsMap.getOrDefault(mtOrder.getId(), new ArrayList<>());
            List<OrderGoodsDto> goodsDtos = new ArrayList<>();

            if (mtOrder.getType().equals(OrderTypeEnum.PRESTORE.getKey())) {
                if (mtOrder.getCouponId() != null && userCouponMap.containsKey(mtOrder.getCouponId())) {
                    MtUserCoupon uc = userCouponMap.get(mtOrder.getCouponId());
                    if (couponInfoMap.containsKey(uc.getCouponId())) {
                        MtCoupon coupon = couponInfoMap.get(uc.getCouponId());
                        if (StrUtil.isNotEmpty(mtOrder.getParam())) {
                            String[] paramArr = mtOrder.getParam().split(",");
                            for (String s : paramArr) {
                                String[] item = s.split("_");
                                if (Integer.parseInt(item[2]) > 0) {
                                    OrderGoodsDto goodsDto = new OrderGoodsDto();
                                    goodsDto.setId(coupon.getId());
                                    goodsDto.setType(OrderTypeEnum.PRESTORE.getKey());
                                    goodsDto.setName("预存￥" + item[0] + "到账￥" + item[1]);
                                    goodsDto.setNum(Integer.parseInt(item[2]));
                                    goodsDto.setPrice(item[0]);
                                    goodsDto.setDiscount("0");
                                    if (!coupon.getImage().contains(basePath)) {
                                        goodsDto.setImage(basePath + coupon.getImage());
                                    }
                                    goodsDtos.add(goodsDto);
                                }
                            }
                        }
                    }
                }
            } else if (mtOrder.getType().equals(OrderTypeEnum.GOODS.getKey())) {
                for (MtOrderGoods orderGoods : orderGoodsList) {
                    MtGoods goodsInfo = goodsMap.get(orderGoods.getGoodsId());
                    if (goodsInfo != null) {
                        OrderGoodsDto orderGoodsDto = new OrderGoodsDto();
                        orderGoodsDto.setId(orderGoods.getId());
                        orderGoodsDto.setName(goodsInfo.getName());
                        if (StrUtil.isNotEmpty(goodsInfo.getLogo()) && !goodsInfo.getLogo().contains(basePath)) {
                            orderGoodsDto.setImage(basePath + goodsInfo.getLogo());
                        } else {
                            orderGoodsDto.setImage(goodsInfo.getLogo());
                        }
                        orderGoodsDto.setType(OrderTypeEnum.GOODS.getKey());
                        orderGoodsDto.setNum(orderGoods.getNum());
                        orderGoodsDto.setSkuId(orderGoods.getSkuId());
                        orderGoodsDto.setPrice(orderGoods.getPrice().toString());
                        orderGoodsDto.setDiscount(orderGoods.getDiscount().toString());
                        orderGoodsDto.setGoodsId(orderGoods.getGoodsId());

                        if (orderGoods.getSkuId() > 0 && skuSpecMap.containsKey(orderGoods.getSkuId())) {
                            orderGoodsDto.setSpecList(skuSpecMap.get(orderGoods.getSkuId()));
                        }
                        goodsDtos.add(orderGoodsDto);
                    }
                }
            }
            vo.setGoods(goodsDtos);

            if (mtOrder.getCouponId() != null && mtOrder.getCouponId() > 0 && userCouponMap.containsKey(mtOrder.getCouponId())) {
                MtUserCoupon uc = userCouponMap.get(mtOrder.getCouponId());
                if (couponInfoMap.containsKey(uc.getCouponId())) {
                    MtCoupon c = couponInfoMap.get(uc.getCouponId());
                    UserCouponDto couponDto = new UserCouponDto();
                    couponDto.setId(uc.getId());
                    couponDto.setCouponId(c.getId());
                    couponDto.setName(c.getName());
                    couponDto.setAmount(uc.getAmount());
                    couponDto.setBalance(uc.getBalance());
                    couponDto.setStatus(uc.getStatus());
                    couponDto.setType(c.getType());
                    vo.setCouponInfo(couponDto);
                }
            }

            if (addressMap.containsKey(mtOrder.getId())) {
                MtOrderAddress addr = addressMap.get(mtOrder.getId());
                UserOrderRespVO.AddressRespVO addressVO = new UserOrderRespVO.AddressRespVO();
                addressVO.setId(addr.getId());
                addressVO.setName(addr.getName());
                addressVO.setMobile(addr.getMobile());
                addressVO.setDetail(addr.getDetail());
                addressVO.setProvinceId(addr.getProvinceId());
                addressVO.setCityId(addr.getCityId());
                addressVO.setRegionId(addr.getRegionId());

                if (addr.getProvinceId() != null) addressVO.setProvinceName(regionNameMap.get(addr.getProvinceId()));
                if (addr.getCityId() != null) addressVO.setCityName(regionNameMap.get(addr.getCityId()));
                if (addr.getRegionId() != null) addressVO.setRegionName(regionNameMap.get(addr.getRegionId()));

                vo.setAddress(addressVO);
            }

            if (StrUtil.isNotEmpty(mtOrder.getExpressInfo())) {
                try {
                    JSONObject express = JSONObject.parseObject(mtOrder.getExpressInfo());
                    UserOrderRespVO.ExpressRespVO expressInfo = new UserOrderRespVO.ExpressRespVO();
                    expressInfo.setExpressNo(express.getString("expressNo"));
                    expressInfo.setExpressCompany(express.getString("expressCompany"));
                    expressInfo.setExpressTime(express.getDate("expressTime"));
                    vo.setExpressInfo(expressInfo);
                } catch (Exception e) {
                    // ignore
                }
            }

            resultList.add(vo);
        }

        return resultList;
    }

    @Override
    public UserOrderRespVO getUserOrderDetail(Integer orderId) {
        if (orderId == null || orderId <= 0) {
            return null;
        }
        MtOrder mtOrder = mtOrderMapper.selectById(orderId);
        UserOrderRespVO order = getOrderDetail(mtOrder, true, true);
        if (!order.getTakeStatus().equals(TakeStatusEnum.COMPLETED)) {
            Integer makeCount = getToMakeCount(mtOrder.getMerchantId(), mtOrder.getStoreId(), mtOrder.getPayTime(), mtOrder.getId());
            order.setQueueCount(makeCount);
            order.setEstimatedWaitTime(makeCount * 5);
        }
        return order;
    }

    @Override
    public Integer getToMakeCount(Integer merchantId, Integer storeId, Date orderTime, Integer orderId) {
        return mtOrderMapper.selectToMakeCount(merchantId, storeId, orderTime, orderId);
    }

    @Override
    public MtOrder getOrderById(Integer orderId) {
        MtOrder order = mtOrderMapper.selectById(orderId);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        return order;
    }

    /**
     * 处理订单详情
     *
     * @param orderInfo    订单信息
     * @param needAddress  是否获取订单地址
     * @param getPayStatus 是否获取支付状态
     * @return UserOrderDto
     */
    private UserOrderRespVO getOrderDetail(MtOrder orderInfo, boolean needAddress, boolean getPayStatus) throws BusinessCheckException {
        UserOrderRespVO order = new UserOrderRespVO();
        BeanUtils.copyProperties(orderInfo, order);
        OrderTypeEnum type = OrderTypeEnum.getEnum(orderInfo.getType());
        OrderStatusEnum orderStatus = OrderStatusEnum.getEnum(orderInfo.getStatus());
        SettleStatusEnum settleStatus = SettleStatusEnum.getEnum(orderInfo.getSettleStatus());
        TakeStatusEnum takeStatus = TakeStatusEnum.getEnum(orderInfo.getTakeStatus());
        // 订单信息
        order.setTakeStatus(takeStatus);
        order.setSettleStatus(settleStatus);
        order.setId(orderInfo.getId());
        order.setMerchantId(orderInfo.getMerchantId());
        order.setUserId(orderInfo.getUserId());
        order.setCouponId(orderInfo.getCouponId());
        order.setOrderSn(orderInfo.getOrderSn());
        order.setRemark(orderInfo.getRemark());
        order.setType(type);
        order.setPayType(PayTypeEnum.getEnum(orderInfo.getPayType()));
        order.setOrderMode(OrderModeEnum.getEnum(orderInfo.getOrderMode()));
        order.setAmount(orderInfo.getAmount());
        order.setIsVisitor(orderInfo.getIsVisitor());
        order.setStaffId(orderInfo.getStaffId());
        order.setDeliveryFee(orderInfo.getDeliveryFee());
        // 核销码为空，说明已经核销
        order.setIsVerify(orderInfo.getVerifyCode() == null || StringUtils.isEmpty(orderInfo.getVerifyCode()));
        if (orderInfo.getPayAmount() != null) {
            order.setPayAmount(orderInfo.getPayAmount());
        } else {
            order.setPayAmount(new BigDecimal("0"));
        }
        if (orderInfo.getDiscount() != null) {
            order.setDiscount(orderInfo.getDiscount());
        } else {
            order.setDiscount(new BigDecimal("0"));
        }
        if (orderInfo.getPointAmount() != null) {
            order.setPointAmount(orderInfo.getPointAmount());
        } else {
            order.setPointAmount(new BigDecimal("0"));
        }

        order.setStatus(orderStatus);
        order.setParam(orderInfo.getParam());
        order.setPayStatus(PayStatusEnum.getEnum(orderInfo.getPayStatus()));

        if (orderInfo.getUsePoint() != null) {
            order.setUsePoint(orderInfo.getUsePoint());
        } else {
            order.setUsePoint(0);
        }
        order.setTypeName(type.getValue());
        order.setStatusText(orderStatus.getValue());

        // 订单所属店铺
        MtStore store = storeService.queryStoreById(orderInfo.getStoreId());
        UserOrderRespVO.OrderStoreRespVO storeInfo = new UserOrderRespVO.OrderStoreRespVO();
        BeanUtils.copyProperties(store, storeInfo);
        order.setStoreInfo(storeInfo);
        // 订单所属桌码
        if (orderInfo.getTableId() != null && orderInfo.getTableId() > 0) {
            MtTable tableInfo = tableService.queryTableById(orderInfo.getTableId());
            if (tableInfo != null) {
                UserOrderRespVO.OrderTableRespVO tableRespVO = new UserOrderRespVO.OrderTableRespVO();
                BeanUtils.copyProperties(tableInfo, tableRespVO);
                order.setTableInfo(tableRespVO);
            }
        }

        // 所属员工
        if (orderInfo.getStaffId() != null && orderInfo.getStaffId() > 0) {
            MtStaff staffInfo = staffService.queryStaffById(orderInfo.getStaffId());
            if (staffInfo != null) {
                UserOrderRespVO.OrderStaffRespVO staffRespVO = new UserOrderRespVO.OrderStaffRespVO();
                BeanUtils.copyProperties(staffInfo, staffRespVO);
                order.setStaffInfo(staffRespVO);
            }
        }

        // 下单用户信息直接取会员个人信息
        UserOrderRespVO.OrderUserRespVO userInfo = new UserOrderRespVO.OrderUserRespVO();
        MtUser user = memberService.queryMemberById(orderInfo.getUserId());
        if (user != null) {
            userInfo.setId(user.getId());
            userInfo.setName(user.getName());
            userInfo.setMobile(user.getMobile());
            userInfo.setNo(user.getUserNo());
            order.setUserInfo(userInfo);
        }
        List<OrderGoodsDto> goodsList = new ArrayList<>();
        String baseImage = settingService.getUploadBasePath();
        // 储值卡的订单
        if (orderInfo.getType().equals(OrderTypeEnum.PRESTORE.getKey())) {
            MtCoupon coupon = couponService.queryCouponById(orderInfo.getCouponId());
            String[] paramArr = orderInfo.getParam().split(",");
            for (String s : paramArr) {
                String[] item = s.split("_");
                if (Integer.parseInt(item[2]) > 0) {
                    OrderGoodsDto goodsDto = new OrderGoodsDto();
                    goodsDto.setId(coupon.getId());
                    goodsDto.setType(OrderTypeEnum.PRESTORE.getKey());
                    goodsDto.setName("预存￥" + item[0] + "到账￥" + item[1]);
                    goodsDto.setNum(Integer.parseInt(item[2]));
                    goodsDto.setPrice(item[0]);
                    goodsDto.setDiscount("0");
                    goodsDto.setImage(isHttp(coupon.getImage(), baseImage));
                    goodsList.add(goodsDto);
                }
            }
        }

        // 商品订单
        if (orderInfo.getType().equals(OrderTypeEnum.GOODS.getKey())) {
            Map<String, Object> params = new HashMap<>();
            params.put("ORDER_ID", orderInfo.getId());
            List<MtOrderGoods> orderGoodsList = mtOrderGoodsMapper.selectByMap(params);
            for (MtOrderGoods orderGoods : orderGoodsList) {
                MtGoods goodsInfo = mtGoodsMapper.selectById(orderGoods.getGoodsId());
                if (goodsInfo != null) {
                    OrderGoodsDto orderGoodsDto = new OrderGoodsDto();
                    orderGoodsDto.setId(orderGoods.getId());
                    orderGoodsDto.setName(goodsInfo.getName());
                    if (!goodsInfo.getLogo().contains(baseImage)) {
                        orderGoodsDto.setImage(baseImage + goodsInfo.getLogo());
                    }
                    orderGoodsDto.setType(OrderTypeEnum.GOODS.getKey());
                    orderGoodsDto.setNum(orderGoods.getNum());
                    orderGoodsDto.setSkuId(orderGoods.getSkuId());
                    orderGoodsDto.setPrice(orderGoods.getPrice().toString());
                    orderGoodsDto.setDiscount(orderGoods.getDiscount().toString());
                    orderGoodsDto.setGoodsId(orderGoods.getGoodsId());
                    if (orderGoods.getSkuId() > 0) {
                        List<GoodsSpecValueDto> specList = goodsService.getSpecListBySkuId(orderGoods.getSkuId());
                        orderGoodsDto.setSpecList(specList);
                    }
                    goodsList.add(orderGoodsDto);
                }
            }
        }

        // 配送地址
        if (orderInfo.getOrderMode().equals(OrderModeEnum.EXPRESS.getKey()) && needAddress) {
            List<MtOrderAddress> orderAddressList = mtOrderAddressMapper.getOrderAddress(orderInfo.getId());
            MtOrderAddress orderAddress = null;
            if (!orderAddressList.isEmpty()) {
                orderAddress = orderAddressList.get(0);
            }
            if (orderAddress != null) {
                UserOrderRespVO.AddressRespVO address = new UserOrderRespVO.AddressRespVO();
                address.setId(orderAddress.getId());
                address.setName(orderAddress.getName());
                address.setMobile(orderAddress.getMobile());
                address.setDetail(orderAddress.getDetail());
                address.setProvinceId(orderAddress.getProvinceId());
                address.setCityId(orderAddress.getCityId());
                address.setRegionId(orderAddress.getRegionId());

                if (orderAddress.getProvinceId() > 0) {
                    MtRegion mtProvince = mtRegionMapper.selectById(orderAddress.getProvinceId());
                    if (mtProvince != null) {
                        address.setProvinceName(mtProvince.getName());
                    }
                }
                if (orderAddress.getCityId() > 0) {
                    MtRegion mtCity = mtRegionMapper.selectById(orderAddress.getCityId());
                    if (mtCity != null) {
                        address.setCityName(mtCity.getName());
                    }
                }
                if (orderAddress.getRegionId() > 0) {
                    MtRegion mtRegion = mtRegionMapper.selectById(orderAddress.getRegionId());
                    if (mtRegion != null) {
                        address.setRegionName(mtRegion.getName());
                    }
                }
                order.setAddress(address);
            }
        }

        // 物流信息
        if (StringUtils.isNotEmpty(orderInfo.getExpressInfo())) {
            JSONObject express = JSONObject.parseObject(orderInfo.getExpressInfo());
            UserOrderRespVO.ExpressRespVO expressInfo = new UserOrderRespVO.ExpressRespVO();
            expressInfo.setExpressNo(express.get("expressNo").toString());
            expressInfo.setExpressCompany(express.get("expressCompany").toString());
            expressInfo.setExpressTime(express.getDate("expressTime"));
            order.setExpressInfo(expressInfo);
        }

        // 使用的卡券
        if (order.getCouponId() != null && order.getCouponId() > 0) {
            MtUserCoupon mtUserCoupon = userCouponService.getUserCouponDetail(order.getCouponId());
            if (mtUserCoupon != null) {
                MtCoupon mtCoupon = couponService.queryCouponById(mtUserCoupon.getCouponId());
                if (mtCoupon != null) {
                    UserCouponDto couponInfo = new UserCouponDto();
                    couponInfo.setId(mtUserCoupon.getId());
                    couponInfo.setCouponId(mtCoupon.getId());
                    couponInfo.setName(mtCoupon.getName());
                    couponInfo.setAmount(mtUserCoupon.getAmount());
                    couponInfo.setBalance(mtUserCoupon.getBalance());
                    couponInfo.setStatus(mtUserCoupon.getStatus());
                    couponInfo.setType(mtCoupon.getType());
                    order.setCouponInfo(couponInfo);
                }
            }
        }

        // 查询支付状态
        if (getPayStatus && !orderInfo.getPayStatus().equals(PayStatusEnum.SUCCESS.getKey())) {
            // 微信支付
            if (orderInfo.getPayType().equals(PayTypeEnum.MICROPAY.getKey()) || orderInfo.getPayType().equals(PayTypeEnum.JSAPI.getKey())) {
                try {
                    Map<String, String> payResult = weixinService.queryPaidOrder(orderInfo.getStoreId(), "", orderInfo.getOrderSn());
                    if (payResult != null && payResult.get("trade_state").equals("SUCCESS")) {
                        BigDecimal payAmount = new BigDecimal(payResult.get("total_fee")).divide(new BigDecimal("100"));
                        SpringUtils.getBean(OpenApiOrderService.class).setOrderPayed(orderInfo.getId(), payAmount);
                        order.setPayStatus(PayStatusEnum.SUCCESS);
                    }
                } catch (Exception e) {
                    // empty
                }
            }
            // 支付宝支付
            if (orderInfo.getPayType().equals(PayTypeEnum.ALISCAN.getKey())) {
                try {
                    Map<String, String> payResult = alipayService.queryPaidOrder(orderInfo.getStoreId(), "", orderInfo.getOrderSn());
                    if (payResult != null) {
                        BigDecimal payAmount = new BigDecimal(payResult.get("payAmount"));
                        SpringUtils.getBean(OpenApiOrderService.class).setOrderPayed(orderInfo.getId(), payAmount);
                        order.setPayStatus(PayStatusEnum.SUCCESS);
                    }
                } catch (Exception e) {
                    // empty
                }
            }
        }
        order.setGoods(goodsList);
        return order;
    }

    /**
     * 确定选中的优惠券ID（自动选择最优或使用指定的）
     */
    private Integer determineSelectedCouponId(Integer userId, List<MtCart> cartList, Integer userCouponId, String platform, Integer merchantId, boolean isUsePoint, String orderMode) {
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
    private CartCalculationResult processCartItems(List<MtCart> cartList, String orderModel) {
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
            ResCartDto cartDto = buildCartDto(cart, mtGoodsInfo, basePath, orderModel);
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
    private ResCartDto buildCartDto(MtCart cart, MtGoods mtGoodsInfo, String basePath, String orderModel) {
        ResCartDto cartDto = new ResCartDto();
        cartDto.setId(cart.getId());
        cartDto.setGoodsId(cart.getGoodsId());
        cartDto.setNum(cart.getNum());
        cartDto.setSkuId(cart.getSkuId());
        cartDto.setUserId(cart.getUserId());

        // 处理SKU信息
        MtGoods goodsInfo = processGoodsInfo(cart, mtGoodsInfo, basePath);

        if (goodsInfo.getIsSingleSpec().equals(YesOrNoEnum.NO.getKey())) {
            // 设置规格列表
            List<GoodsSpecValueDto> specList = goodsService.getSpecListBySkuId(cart.getSkuId());
            cartDto.setSpecList(specList);
        }
        cartDto.setGoodsInfo(goodsInfo);
        // 检查库存有效性
        boolean isEffect = checkStockAvailability(goodsInfo, cart.getNum());
        if (!isEffect && !StrUtil.equals(OrderModeEnum.DYNAMIC.getKey(), orderModel)) {
            throw exception(GOODS_SKU_NOT_ENOUGH, mtGoodsInfo.getName());
        }
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
        MtGoodsSku sku = null;
        if (!mtGoodsInfo.getIsSingleSpec().equals(YesOrNoEnum.YES.getKey())) {
            if (ObjectUtil.isNull(cart.getSkuId()) || cart.getSkuId() <= 0) {
                throw exception(GOODS_IS_MULTI_SPECIFICATIONS, mtGoodsInfo.getName());
            }
            sku = goodsService.getSkuInfoById(cart.getSkuId());
            if (sku == null) {
                throw exception(GOODS_SKU_NOT_EXIST, cart.getSkuId());
            }
        }
        // 如果有SKU，使用SKU信息
        if (sku != null) {
            MtGoods mtGoods = new MtGoods();
            BeanUtils.copyProperties(mtGoodsInfo, mtGoods);
            updateGoodsWithSkuInfo(mtGoods, sku, basePath);
            return mtGoods;
        }
        return mtGoodsInfo;
    }

    /**
     * 使用SKU信息更新商品信息
     */
    private void updateGoodsWithSkuInfo(MtGoods mtGoods, MtGoodsSku mtGoodsSku, String basePath) {
        mtGoods.setLogo(isHttp(mtGoodsSku.getLogo(), basePath));
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
     * @param merchantId  商户ID
     * @param userInfo    用户信息
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
}
