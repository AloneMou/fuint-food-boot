package com.fuint.openapi.service.impl;

import com.fuint.common.Constants;
import com.fuint.common.enums.CouponExpireTypeEnum;
import com.fuint.common.enums.CouponTypeEnum;
import com.fuint.common.enums.UserCouponStatusEnum;
import com.fuint.common.service.*;
import com.fuint.common.util.DateUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.openapi.service.OpenApiUserCouponService;
import com.fuint.openapi.v1.member.coupon.vo.UserCouponPageRespVO;
import com.fuint.openapi.v1.member.coupon.vo.UserCouponRespVO;
import com.fuint.repository.model.MtCoupon;
import com.fuint.repository.model.MtStore;
import com.fuint.repository.model.MtUserCoupon;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * OpenAPI用户优惠券服务实现类
 * <p>
 * 独立的OpenAPI用户优惠券服务实现,避免与后台业务冲突
 * 优化性能：批量查询，避免N+1问题
 *
 * @author mjw
 * @since 2026/1/18
 */
@Service
@AllArgsConstructor
public class OpenApiUserCouponServiceImpl implements OpenApiUserCouponService {

    private static final Logger logger = LoggerFactory.getLogger(OpenApiUserCouponServiceImpl.class);

    private final UserCouponService userCouponService;
    private final CouponService couponService;
    private final StoreService storeService;
    private final SettingService settingService;
    private final ConfirmLogService confirmLogService;

    /**
     * 分页查询用户优惠券列表
     */
    @Override
    public UserCouponPageRespVO queryUserCouponPage(PaginationRequest paginationRequest) throws BusinessCheckException {
        logger.info("[OpenApiUserCouponService] 分页查询用户优惠券列表, 参数: {}", paginationRequest);

        // 查询分页数据
        PaginationResponse<MtUserCoupon> paginationResponse = userCouponService.queryUserCouponListByPagination(paginationRequest);
        List<MtUserCoupon> userCouponList = paginationResponse.getContent();

        if (userCouponList.isEmpty()) {
            return buildEmptyPageRespVO(paginationRequest);
        }

        // 批量查询优惠券信息，避免N+1问题
        Set<Integer> couponIds = userCouponList.stream()
                .map(MtUserCoupon::getCouponId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Integer, MtCoupon> couponMap;
        if (!couponIds.isEmpty()) {
            List<MtCoupon> couponList = couponService.queryCouponListByIds(new ArrayList<>(couponIds));
            couponMap = couponList.stream()
                    .collect(Collectors.toMap(MtCoupon::getId, coupon -> coupon));
        } else {
            couponMap = new HashMap<>();
        }

        // 批量查询店铺信息（优化：收集所有店铺ID，批量查询）
        Set<Integer> allStoreIds = new HashSet<>();
        for (MtCoupon coupon : couponMap.values()) {
            if (StringUtils.isNotEmpty(coupon.getStoreIds())) {
                String[] storeIdStrs = coupon.getStoreIds().split(",");
                for (String storeIdStr : storeIdStrs) {
                    if (StringUtils.isNotEmpty(storeIdStr)) {
                        try {
                            allStoreIds.add(Integer.parseInt(storeIdStr.trim()));
                        } catch (NumberFormatException e) {
                            logger.warn("店铺ID格式错误: {}", storeIdStr);
                        }
                    }
                }
            }
        }
        // 批量查询店铺信息
        Map<Integer, String> storeIdToNameMap = new HashMap<>();
        if (!allStoreIds.isEmpty()) {
            try {
                List<MtStore> stores = storeService.getStoreByIds(allStoreIds);
                for (MtStore store : stores) {
                    storeIdToNameMap.put(store.getId(), store.getName());
                }
            } catch (Exception e) {
                logger.warn("批量查询店铺信息失败", e);
            }
        }
        // 构建storeIds -> storeNames的映射
        Map<String, String> storeNamesMap = new HashMap<>();
        for (MtCoupon coupon : couponMap.values()) {
            if (StringUtils.isNotEmpty(coupon.getStoreIds())) {
                String[] storeIdStrs = coupon.getStoreIds().split(",");
                List<String> storeNames = new ArrayList<>();
                for (String storeIdStr : storeIdStrs) {
                    if (StringUtils.isNotEmpty(storeIdStr)) {
                        try {
                            Integer storeId = Integer.parseInt(storeIdStr.trim());
                            String storeName = storeIdToNameMap.get(storeId);
                            if (StringUtils.isNotEmpty(storeName)) {
                                storeNames.add(storeName);
                            }
                        } catch (NumberFormatException e) {
                            // 忽略格式错误的ID
                        }
                    }
                }
                storeNamesMap.put(coupon.getStoreIds(), String.join(",", storeNames));
            }
        }

        // 批量查询计次卡核销次数（只查询计次卡类型）
        Set<Integer> timerUserCouponIds = new HashSet<>();
        for (MtUserCoupon userCoupon : userCouponList) {
            MtCoupon coupon = couponMap.get(userCoupon.getCouponId());
            if (coupon != null && CouponTypeEnum.TIMER.getKey().equals(coupon.getType())) {
                timerUserCouponIds.add(userCoupon.getId());
            }
        }
        Map<Integer, Long> confirmCountMap = new HashMap<>();
        if (!timerUserCouponIds.isEmpty()) {
            for (Integer userCouponId : timerUserCouponIds) {
                try {
                    Long confirmNum = confirmLogService.getConfirmNum(userCouponId);
                    confirmCountMap.put(userCouponId, confirmNum);
                } catch (Exception e) {
                    logger.warn("获取核销次数失败, userCouponId: {}", userCouponId, e);
                }
            }
        }

        // 获取图片基础路径
        String baseImage = settingService.getUploadBasePath();

        // 转换为响应VO（使用批量查询的结果）
        List<UserCouponRespVO> respVOList = userCouponList.stream()
                .map(userCoupon -> convertToRespVO(userCoupon, couponMap, storeNamesMap, confirmCountMap, baseImage))
                .collect(Collectors.toList());

        // 构建分页响应
        UserCouponPageRespVO pageRespVO = new UserCouponPageRespVO();
        pageRespVO.setTotalElements(paginationResponse.getTotalElements());
        pageRespVO.setTotalPages(paginationResponse.getTotalPages());
        pageRespVO.setCurrentPage(paginationRequest.getCurrentPage());
        pageRespVO.setPageSize(paginationRequest.getPageSize());
        pageRespVO.setContent(respVOList);

        return pageRespVO;
    }

    /**
     * 根据ID获取用户优惠券详情
     */
    @Override
    public UserCouponRespVO getUserCouponDetail(Integer id) throws BusinessCheckException {
        logger.info("[OpenApiUserCouponService] 获取用户优惠券详情, ID: {}", id);

        MtUserCoupon userCoupon = userCouponService.getUserCouponDetail(id);
        if (userCoupon == null) {
            throw new BusinessCheckException("用户优惠券不存在");
        }

        // 查询优惠券信息
        MtCoupon couponInfo = couponService.queryCouponById(userCoupon.getCouponId());
        if (couponInfo == null) {
            throw new BusinessCheckException("优惠券不存在");
        }

        // 查询店铺信息（优化：批量查询）
        Map<String, String> storeNamesMap = new HashMap<>();
        if (StringUtils.isNotEmpty(couponInfo.getStoreIds())) {
            Set<Integer> storeIds = new HashSet<>();
            String[] storeIdStrs = couponInfo.getStoreIds().split(",");
            for (String storeIdStr : storeIdStrs) {
                if (StringUtils.isNotEmpty(storeIdStr)) {
                    try {
                        storeIds.add(Integer.parseInt(storeIdStr.trim()));
                    } catch (NumberFormatException e) {
                        logger.warn("店铺ID格式错误: {}", storeIdStr);
                    }
                }
            }
            if (!storeIds.isEmpty()) {
                try {
                    List<MtStore> stores = storeService.getStoreByIds(storeIds);
                    List<String> storeNames = new ArrayList<>();
                    for (MtStore store : stores) {
                        storeNames.add(store.getName());
                    }
                    storeNamesMap.put(couponInfo.getStoreIds(), String.join(",", storeNames));
                } catch (Exception e) {
                    logger.warn("查询店铺信息失败", e);
                }
            }
        }

        // 如果是计次卡，查询核销次数
        Map<Integer, Long> confirmCountMap = new HashMap<>();
        if (CouponTypeEnum.TIMER.getKey().equals(couponInfo.getType())) {
            Long confirmNum = confirmLogService.getConfirmNum(userCoupon.getId());
            confirmCountMap.put(userCoupon.getId(), confirmNum);
        }

        // 获取图片基础路径
        String baseImage = settingService.getUploadBasePath();

        // 构建优惠券Map
        Map<Integer, MtCoupon> couponMap = new HashMap<>();
        couponMap.put(couponInfo.getId(), couponInfo);

        return convertToRespVO(userCoupon, couponMap, storeNamesMap, confirmCountMap, baseImage);
    }

    /**
     * 转换MtUserCoupon为响应VO（使用批量查询的结果）
     */
    private UserCouponRespVO convertToRespVO(MtUserCoupon userCoupon,
                                             Map<Integer, MtCoupon> couponMap,
                                              Map<String, String> storeNamesMap,
                                              Map<Integer, Long> confirmCountMap,
                                              String baseImage) {
        UserCouponRespVO respVO = new UserCouponRespVO();
        respVO.setUserCouponId(userCoupon.getId());
        respVO.setCouponId(userCoupon.getCouponId());
        respVO.setCode(userCoupon.getCode());
        respVO.setStatus(userCoupon.getStatus());
        respVO.setAmount(userCoupon.getAmount());
        respVO.setBalance(userCoupon.getBalance());
        respVO.setCreateTime(userCoupon.getCreateTime());
        respVO.setUsedTime(userCoupon.getUsedTime());

        // 从Map中获取优惠券信息（避免重复查询）
        MtCoupon couponInfo = couponMap.get(userCoupon.getCouponId());
        if (couponInfo != null) {
            respVO.setCouponName(couponInfo.getName());
            respVO.setCouponType(couponInfo.getType());
            respVO.setUseRule(couponInfo.getOutRule());
            respVO.setIsGive(couponInfo.getIsGive());
            respVO.setDescription(couponInfo.getDescription());

            // 设置图片
            String image = couponInfo.getImage();
            if (StringUtils.isNotEmpty(image)) {
                respVO.setImage(baseImage + image);
            }

            // 设置使用门槛说明
            if (StringUtils.isEmpty(couponInfo.getOutRule()) || couponInfo.getOutRule().equals("0")) {
                respVO.setDescription("无使用门槛");
            } else {
                respVO.setDescription("满" + couponInfo.getOutRule() + "元可用");
            }

            // 设置有效期
            String effectiveDate = "";
            if (couponInfo.getExpireType().equals(CouponExpireTypeEnum.FIX.getKey())) {
                respVO.setEffectiveStartTime(couponInfo.getBeginTime());
                respVO.setEffectiveEndTime(couponInfo.getEndTime());
                if (couponInfo.getBeginTime() != null && couponInfo.getEndTime() != null) {
                    effectiveDate = DateUtil.formatDate(couponInfo.getBeginTime(), "yyyy.MM.dd HH:mm") + "-"
                            + DateUtil.formatDate(couponInfo.getEndTime(), "yyyy.MM.dd HH:mm");
                }
            } else if (couponInfo.getExpireType().equals(CouponExpireTypeEnum.FLEX.getKey())) {
                respVO.setEffectiveStartTime(userCoupon.getCreateTime());
                respVO.setEffectiveEndTime(userCoupon.getExpireTime());
                if (userCoupon.getCreateTime() != null && userCoupon.getExpireTime() != null) {
                    effectiveDate = DateUtil.formatDate(userCoupon.getCreateTime(), "yyyy.MM.dd HH:mm") + "-"
                            + DateUtil.formatDate(userCoupon.getExpireTime(), "yyyy.MM.dd HH:mm");
                }
            }
            respVO.setEffectiveDate(effectiveDate);

            // 设置适用店铺（从Map中获取）
            String storeNames = storeNamesMap.getOrDefault(couponInfo.getStoreIds(), "");
            respVO.setStoreNames(storeNames);

            // 判断是否可用
            boolean canUse = couponService.isCouponEffective(couponInfo, userCoupon);
            if (!userCoupon.getStatus().equals(UserCouponStatusEnum.UNUSED.getKey())) {
                canUse = false;
            }
            respVO.setCanUse(canUse);

            // 设置提示信息
            String tips = "";
            // 优惠券tips
            if (couponInfo.getType().equals(CouponTypeEnum.COUPON.getKey())) {
                if (StringUtils.isNotEmpty(couponInfo.getOutRule()) && Float.parseFloat(couponInfo.getOutRule()) > 0) {
                    tips = "满" + couponInfo.getOutRule() + "可用";
                } else {
                    tips = "无门槛券";
                }
            }
            // 储值卡tips
            if (couponInfo.getType().equals(CouponTypeEnum.PRESTORE.getKey())) {
                tips = "￥" + userCoupon.getAmount() + "，余额￥" + userCoupon.getBalance();
            }
            // 计次卡tips（从Map中获取核销次数）
            if (couponInfo.getType().equals(CouponTypeEnum.TIMER.getKey())) {
                Long confirmNum = confirmCountMap.getOrDefault(userCoupon.getId(), 0L);
                tips = "已使用" + confirmNum + "次，可使用" + couponInfo.getOutRule() + "次";
            }
            respVO.setTips(tips);
        }

        return respVO;
    }

    /**
     * 构建空的分页响应
     */
    private UserCouponPageRespVO buildEmptyPageRespVO(PaginationRequest paginationRequest) {
        UserCouponPageRespVO pageRespVO = new UserCouponPageRespVO();
        pageRespVO.setTotalElements(0L);
        pageRespVO.setTotalPages(0);
        pageRespVO.setCurrentPage(paginationRequest.getCurrentPage());
        pageRespVO.setPageSize(paginationRequest.getPageSize());
        pageRespVO.setContent(Collections.emptyList());
        return pageRespVO;
    }
}
