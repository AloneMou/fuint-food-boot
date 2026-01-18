package com.fuint.openapi.service.impl;

import com.fuint.common.dto.CouponDto;
import com.fuint.common.dto.GoodsSpecValueDto;
import com.fuint.common.dto.ResCartDto;
import com.fuint.common.enums.*;
import com.fuint.common.service.*;
import com.fuint.common.util.DateUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.exception.ServiceException;
import com.fuint.openapi.service.OpenApiOrderService;
import com.fuint.repository.mapper.*;
import com.fuint.repository.model.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

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

        // 计算会员折扣
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
     * @param merchantId    商户ID
     * @param userId        用户ID
     * @param cartList      购物车列表
     * @param userCouponId  指定使用的用户优惠券ID（为0时自动匹配最优券）
     * @param usePoint      使用积分数量
     * @param platform      平台
     * @param orderMode     订单模式
     * @param storeId       店铺ID
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

        // 提取计算结果
        BigDecimal totalPrice = getBigDecimalValue(cartData.get("totalPrice"));
        BigDecimal deliveryFee = getBigDecimalValue(cartData.get("deliveryFee"));
        BigDecimal couponAmount = getBigDecimalValue(cartData.get("couponAmount"));
        BigDecimal usePointAmount = getBigDecimalValue(cartData.get("usePointAmount"));
        List<CouponDto> couponList = (List<CouponDto>) cartData.get("couponList");
        Integer myPoint = getIntegerValue(cartData.get("myPoint"));
        Integer calculatedUsePoint = getIntegerValue(cartData.get("usePoint"));

        // 构建可用优惠券列表
        List<Map<String, Object>> availableCoupons = buildAvailableCouponsList(couponList, selectedCouponId);

        // 计算最终应付金额
        BigDecimal payableAmount = calculatePayableAmount(totalPrice, couponAmount, usePointAmount, deliveryFee);

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("totalAmount", totalPrice);
        result.put("discountAmount", couponAmount);
        result.put("pointAmount", usePointAmount);
        result.put("deliveryFee", deliveryFee);
        result.put("payableAmount", payableAmount);
        result.put("usePoint", calculatedUsePoint);
        result.put("availablePoint", myPoint);
        result.put("availableCoupons", availableCoupons);
        result.put("selectedCouponId", selectedCouponId);
        result.put("goodsList", cartData.get("list"));
        result.put("calculateTime", new Date());
        return result;
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
     * 计算会员折扣
     */
    private BigDecimal calculateMemberDiscount(Integer merchantId, MtUser userInfo) {
        try {
            MtUserGrade userGrade = userGradeService.queryUserGradeById(merchantId,
                    Integer.parseInt(userInfo.getGradeId()), userInfo.getId());
            if (userGrade == null || userGrade.getDiscount() <= 0) {
                return ONE;
            }

            BigDecimal discount = BigDecimal.valueOf(userGrade.getDiscount()).divide(TEN, BigDecimal.ROUND_CEILING, RoundingMode.FLOOR);
            return discount.compareTo(ZERO) > 0 ? discount : ONE;
        } catch (Exception e) {
            return ONE;
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
