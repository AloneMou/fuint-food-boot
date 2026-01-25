package com.fuint.openapi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.fuint.common.enums.*;
import com.fuint.common.service.*;
import com.fuint.common.service.MemberService;
import com.fuint.common.service.UserGradeService;
import com.fuint.openapi.service.OpenGoodsService;
import com.fuint.openapi.v1.goods.product.vo.model.GoodsSkuRespVO;
import com.fuint.openapi.v1.goods.product.vo.model.GoodsSpecChildVO;
import com.fuint.openapi.v1.goods.product.vo.model.GoodsSpecItemVO;
import com.fuint.openapi.v1.goods.product.vo.response.CGoodsListRespVO;
import com.fuint.repository.mapper.MtCouponGoodsMapper;
import com.fuint.repository.mapper.MtGoodsSkuMapper;
import com.fuint.repository.mapper.MtGoodsSpecMapper;
import com.fuint.repository.model.*;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import static com.fuint.framework.util.collection.CollectionUtils.*;
import static com.fuint.framework.util.string.StrUtils.splitToInt;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

/**
 * Foot-Fuint-Backend-master
 *
 * @author mjw
 * @since 2026/1/23 0:15
 */
@Service
public class OpenGoodsServiceImpl implements OpenGoodsService {

    @Resource
    private MtGoodsSkuMapper goodsSkuMapper;

    @Resource
    private MtGoodsSpecMapper goodsSpecMapper;

    @Resource
    private MemberService memberService;

    @Resource
    private UserGradeService userGradeService;

    @Resource
    private SettingService settingService;

    @Resource
    private UserCouponService userCouponService;

    @Resource
    private CouponService couponService;

    @Resource
    private MtCouponGoodsMapper mtCouponGoodsMapper;

    @Resource(name = "goodsPriceExecutor")
    private ThreadPoolExecutor goodsPriceExecutor;

    private static final BigDecimal TEN = new BigDecimal("10");

    @Data
    private static class CouponCtx {
        private MtUserCoupon userCoupon;
        private MtCoupon coupon;
        private Set<Integer> applicableGoodsIds;

        public CouponCtx(MtUserCoupon userCoupon, MtCoupon coupon, Set<Integer> applicableGoodsIds) {
            this.userCoupon = userCoupon;
            this.coupon = coupon;
            this.applicableGoodsIds = applicableGoodsIds;
        }
    }

    @Override
    public List<CGoodsListRespVO> getGoodsList(List<MtGoods> goodsLs, Integer userId) {
        if (goodsLs == null || goodsLs.isEmpty()) {
            return new ArrayList<>();
        }
        List<Integer> goodsIds = convertList(goodsLs, MtGoods::getId);
        List<MtGoodsSku> skuLs = goodsSkuMapper.selectSkuLsByGoodsIds(goodsIds);
        List<MtGoodsSpec> specLs = goodsSpecMapper.selectSpecLsByGoodsIds(goodsIds);
        Map<Integer, List<MtGoodsSku>> skuMap = convertMultiMap(skuLs, MtGoodsSku::getGoodsId);
        Map<Integer, List<MtGoodsSpec>> specMap = convertMultiMap(specLs, MtGoodsSpec::getGoodsId);

        // 预先获取会员折扣率
        BigDecimal memberDiscountRate = getMemberDiscountRate(goodsLs.get(0).getMerchantId(), userId);

        // 预先获取用户信息和积分设置
        PointSettings pointSettings = getUserPointSettings(goodsLs.get(0).getMerchantId(), userId);

        // 预先获取可用优惠券
        List<CouponCtx> couponCtxList = getAvailableCoupons(userId);

        final PointSettings finalPointSettings = pointSettings;
        final List<CouponCtx> finalCouponCtxList = couponCtxList;

        List<CompletableFuture<CGoodsListRespVO>> futures = goodsLs.stream()
                .map(goods -> CompletableFuture.supplyAsync(
                        () -> buildGoodsVO(goods, skuMap, specMap, memberDiscountRate, finalPointSettings, finalCouponCtxList),
                        goodsPriceExecutor
                ))
                .collect(Collectors.toList());

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    private CGoodsListRespVO buildGoodsVO(MtGoods goods,
                                          Map<Integer, List<MtGoodsSku>> skuMap,
                                          Map<Integer, List<MtGoodsSpec>> specMap,
                                          BigDecimal memberDiscountRate,
                                          PointSettings pointSettings,
                                          List<CouponCtx> couponCtxList) {
        CGoodsListRespVO goodsVO = new CGoodsListRespVO();
        BeanUtils.copyProperties(goods, goodsVO);
        goodsVO.setCouponIds(splitToInt(goods.getCouponIds(), ","));
        
        // 计算单规格商品的动态价格
        if (StrUtil.equals(YesOrNoEnum.YES.getKey(), goods.getIsSingleSpec())) {
            BigDecimal dynamicPrice = calculateDynamicPrice(
                    goods.getPrice(),
                    goods.getPrice(),
                    goods,
                    memberDiscountRate,
                    pointSettings,
                    couponCtxList
            );
            goodsVO.setDynamicPrice(dynamicPrice);
        }

        // 构建SKU数据
        List<GoodsSkuRespVO> skuVOList = buildSkuData(
                goods,
                skuMap.getOrDefault(goods.getId(), Collections.emptyList()),
                memberDiscountRate,
                pointSettings,
                couponCtxList
        );
        goodsVO.setSkuData(skuVOList);

        // 构建规格数据
        List<GoodsSpecItemVO> specVOList = buildSpecData(specMap.getOrDefault(goods.getId(), Collections.emptyList()));
        goodsVO.setSpecData(specVOList);
        
        return goodsVO;
    }

    /**
     * 获取会员折扣率（例如 0.8 表示 8折）
     *
     * @param merchantId 商户ID
     * @param userId     用户ID
     * @return 折扣率，如果无折扣返回 0
     */
    private BigDecimal getMemberDiscountRate(Integer merchantId, Integer userId) {
        if (userId == null) {
            return BigDecimal.ZERO;
        }
        MtUser userInfo = memberService.queryMemberById(userId);
        if (userInfo == null || userInfo.getGradeId() == null) {
            return BigDecimal.ZERO;
        }

        try {
            MtUserGrade userGrade = userGradeService.queryUserGradeById(merchantId,
                    Integer.parseInt(userInfo.getGradeId()), userInfo.getId());
            if (userGrade == null || userGrade.getDiscount() == null || userGrade.getDiscount() <= 0) {
                return BigDecimal.ZERO;
            }

            // 折扣率 = discount / 10
            return BigDecimal.valueOf(userGrade.getDiscount()).divide(TEN, 2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * 用户积分设置信息
     */
    @Data
    private static class PointSettings {
        private MtUser userInfo;
        private BigDecimal pointExchangeRate;
        private boolean canUsePointAsMoney;
    }

    /**
     * 获取用户积分设置
     *
     * @param merchantId 商户ID
     * @param userId     用户ID
     * @return 积分设置信息
     */
    private PointSettings getUserPointSettings(Integer merchantId, Integer userId) {
        PointSettings settings = new PointSettings();
        settings.setUserInfo(null);
        settings.setPointExchangeRate(BigDecimal.ZERO);
        settings.setCanUsePointAsMoney(false);

        if (userId == null) {
            return settings;
        }

        MtUser userInfo = memberService.queryMemberById(userId);
        if (userInfo == null) {
            return settings;
        }

        settings.setUserInfo(userInfo);
        MtSetting pointSetting = settingService.querySettingByName(merchantId, SettingTypeEnum.POINT.getKey(), PointSettingEnum.CAN_USE_AS_MONEY.getKey());
        if (pointSetting != null && YesOrNoEnum.YES.getKey().equals(pointSetting.getValue())) {
            settings.setCanUsePointAsMoney(true);
            MtSetting exchangeSetting = settingService.querySettingByName(merchantId, SettingTypeEnum.POINT.getKey(), PointSettingEnum.EXCHANGE_NEED_POINT.getKey());
            if (exchangeSetting != null && StrUtil.isNotEmpty(exchangeSetting.getValue())) {
                settings.setPointExchangeRate(new BigDecimal(exchangeSetting.getValue()));
            }
        }

        return settings;
    }

    /**
     * 获取可用优惠券
     *
     * @param userId 用户ID
     * @return 可用优惠券上下文列表
     */
    private List<CouponCtx> getAvailableCoupons(Integer userId) {
        List<CouponCtx> couponCtxList = new ArrayList<>();
        if (userId == null) {
            return couponCtxList;
        }

        List<String> statusList = Collections.singletonList(UserCouponStatusEnum.UNUSED.getKey());
        List<MtUserCoupon> userCoupons = userCouponService.getUserCouponList(userId, statusList);
        if (!isNotEmpty(userCoupons)) {
            return couponCtxList;
        }

        // 1. 批量获取优惠券信息
        List<Integer> couponIds = userCoupons.stream().map(MtUserCoupon::getCouponId).distinct().collect(Collectors.toList());
        List<MtCoupon> coupons = couponService.queryCouponListByIds(couponIds);
        Map<Integer, MtCoupon> couponMap = coupons.stream().collect(Collectors.toMap(MtCoupon::getId, c -> c));

        // 2. 批量获取部分商品优惠券的关联商品
        List<Integer> parkGoodsCouponIds = coupons.stream()
                .filter(c -> ApplyGoodsEnum.PARK_GOODS.getKey().equals(c.getApplyGoods()))
                .map(MtCoupon::getId)
                .collect(Collectors.toList());

        Map<Integer, Set<Integer>> couponGoodsMap = new HashMap<>();
        if (isNotEmpty(parkGoodsCouponIds)) {
            List<MtCouponGoods> allCouponGoods = mtCouponGoodsMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<MtCouponGoods>()
                            .in(MtCouponGoods::getCouponId, parkGoodsCouponIds)
            );
            if (isNotEmpty(allCouponGoods)) {
                couponGoodsMap = allCouponGoods.stream().collect(
                        Collectors.groupingBy(
                                MtCouponGoods::getCouponId,
                                Collectors.mapping(MtCouponGoods::getGoodsId, Collectors.toSet())
                        )
                );
            }
        }

        // 3. 构建 CouponCtx
        for (MtUserCoupon uc : userCoupons) {
            MtCoupon coupon = couponMap.get(uc.getCouponId());
            if (coupon != null && couponService.isCouponEffective(coupon, uc)) {
                Set<Integer> applicableGoods = Collections.emptySet();
                if (ApplyGoodsEnum.PARK_GOODS.getKey().equals(coupon.getApplyGoods())) {
                    applicableGoods = couponGoodsMap.getOrDefault(coupon.getId(), Collections.emptySet());
                }
                couponCtxList.add(new CouponCtx(uc, coupon, applicableGoods));
            }
        }

        return couponCtxList;
    }

    /**
     * 计算优惠券抵扣金额
     *
     * @param price         当前价格（已应用会员折扣）
     * @param originalPrice 商品原价（用于检查门槛）
     * @param goods         商品信息
     * @param couponCtxList 优惠券上下文列表
     * @return 优惠券抵扣金额
     */
    private BigDecimal calculateCouponDeduction(BigDecimal price, BigDecimal originalPrice, MtGoods goods, List<CouponCtx> couponCtxList) {
        BigDecimal couponDeduction = BigDecimal.ZERO;
        
        for (CouponCtx ctx : couponCtxList) {
            MtCoupon coupon = ctx.getCoupon();
            
            // 检查适用商品
            if (ApplyGoodsEnum.PARK_GOODS.getKey().equals(coupon.getApplyGoods())) {
                if (ctx.getApplicableGoodsIds() == null || !ctx.getApplicableGoodsIds().contains(goods.getId())) {
                    continue;
                }
            } else if (!ApplyGoodsEnum.ALL_GOODS.getKey().equals(coupon.getApplyGoods())) {
                continue;
            }

            // 检查门槛（使用原价）
            if (StrUtil.isNotEmpty(coupon.getOutRule()) && !"0".equals(coupon.getOutRule())) {
                if (originalPrice.compareTo(new BigDecimal(coupon.getOutRule())) < 0) {
                    continue;
                }
            }

            // 计算优惠券金额
            BigDecimal amount = BigDecimal.ZERO;
            if (CouponTypeEnum.COUPON.getKey().equals(coupon.getType())) {
                amount = ctx.getUserCoupon().getAmount();
            } else if (CouponTypeEnum.PRESTORE.getKey().equals(coupon.getType())) {
                amount = ctx.getUserCoupon().getBalance().min(price);
            }

            if (amount.compareTo(couponDeduction) > 0) {
                couponDeduction = amount;
            }
        }
        
        // 优惠券抵扣不能超过当前价格
        return couponDeduction.min(price);
    }

    /**
     * 计算积分抵扣金额
     *
     * @param afterCouponPrice 优惠券抵扣后的价格
     * @param goods            商品信息
     * @param pointSettings    积分设置
     * @return 积分抵扣金额
     */
    private BigDecimal calculatePointDeduction(BigDecimal afterCouponPrice, MtGoods goods, PointSettings pointSettings) {
        if (!pointSettings.isCanUsePointAsMoney() 
                || !StrUtil.equals(YesOrNoEnum.YES.getKey(), goods.getCanUsePoint())
                || pointSettings.getUserInfo() == null
                || pointSettings.getUserInfo().getPoint() <= 0
                || pointSettings.getPointExchangeRate().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal maxPointMoney = new BigDecimal(pointSettings.getUserInfo().getPoint())
                .divide(pointSettings.getPointExchangeRate(), 2, RoundingMode.FLOOR);
        return maxPointMoney.min(afterCouponPrice);
    }

    /**
     * 计算商品动态价格（包含会员折扣、优惠券抵扣、积分抵扣）
     *
     * @param originalPrice     商品原价
     * @param currentPrice      当前价格（用于计算，通常是原价或SKU价格）
     * @param goods             商品信息
     * @param memberDiscountRate 会员折扣率
     * @param pointSettings     积分设置
     * @param couponCtxList    优惠券上下文列表
     * @return 动态价格
     */
    private BigDecimal calculateDynamicPrice(BigDecimal originalPrice,
                                             BigDecimal currentPrice,
                                             MtGoods goods,
                                             BigDecimal memberDiscountRate,
                                             PointSettings pointSettings,
                                             List<CouponCtx> couponCtxList) {
        // 1. 应用会员折扣
        BigDecimal price = currentPrice;
        if (StrUtil.equals(YesOrNoEnum.YES.getKey(), goods.getIsMemberDiscount()) 
                && memberDiscountRate.compareTo(BigDecimal.ZERO) > 0) {
            price = price.multiply(memberDiscountRate).setScale(2, RoundingMode.HALF_UP);
        }

        // 2. 计算优惠券抵扣
        BigDecimal couponDeduction = calculateCouponDeduction(price, originalPrice, goods, couponCtxList);
        BigDecimal afterCouponPrice = price.subtract(couponDeduction);

        // 3. 计算积分抵扣
        BigDecimal pointDeduction = calculatePointDeduction(afterCouponPrice, goods, pointSettings);

        // 4. 计算最终价格
        BigDecimal realPrice = afterCouponPrice.subtract(pointDeduction);
        return realPrice.max(BigDecimal.ZERO);
    }

    /**
     * 构建SKU数据
     *
     * @param goods             商品信息
     * @param skuList           SKU列表
     * @param memberDiscountRate 会员折扣率
     * @param pointSettings     积分设置
     * @param couponCtxList    优惠券上下文列表
     * @return SKU VO列表
     */
    private List<GoodsSkuRespVO> buildSkuData(MtGoods goods,
                                              List<MtGoodsSku> skuList,
                                              BigDecimal memberDiscountRate,
                                              PointSettings pointSettings,
                                              List<CouponCtx> couponCtxList) {
        List<GoodsSkuRespVO> skuVOList = new ArrayList<>();
        
        for (MtGoodsSku sku : skuList) {
            GoodsSkuRespVO skuVO = new GoodsSkuRespVO();
            BeanUtils.copyProperties(sku, skuVO);

            // 计算SKU动态价格
            BigDecimal dynamicPrice = calculateDynamicPrice(
                    goods.getPrice(),
                    sku.getPrice(),
                    goods,
                    memberDiscountRate,
                    pointSettings,
                    couponCtxList
            );
            skuVO.setDynamicPrice(dynamicPrice);
            skuVOList.add(skuVO);
        }
        
        return skuVOList;
    }

    /**
     * 构建规格数据
     *
     * @param specList 规格列表
     * @return 规格VO列表
     */
    private List<GoodsSpecItemVO> buildSpecData(List<MtGoodsSpec> specList) {
        List<GoodsSpecItemVO> specVOList = new ArrayList<>();
        Map<String, List<MtGoodsSpec>> specNameMap = convertMultiMap(specList, MtGoodsSpec::getName);
        
        for (String key : specNameMap.keySet()) {
            GoodsSpecItemVO specVO = new GoodsSpecItemVO();
            specVO.setName(key);
            
            List<MtGoodsSpec> childLs = specNameMap.get(key);
            List<GoodsSpecChildVO> childVOList = new ArrayList<>();
            for (MtGoodsSpec child : childLs) {
                GoodsSpecChildVO childVO = new GoodsSpecChildVO();
                BeanUtils.copyProperties(child, childVO);
                childVOList.add(childVO);
            }
            specVO.setChild(childVOList);
            specVOList.add(specVO);
        }
        
        return specVOList;
    }
}
