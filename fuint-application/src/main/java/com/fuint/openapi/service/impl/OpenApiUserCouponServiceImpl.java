package com.fuint.openapi.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.fuint.common.enums.CouponExpireTypeEnum;
import com.fuint.common.enums.CouponTypeEnum;
import com.fuint.common.enums.UserCouponStatusEnum;
import com.fuint.common.service.*;
import com.fuint.common.util.DateUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pojo.PageResult;
import com.fuint.openapi.service.OpenApiUserCouponService;
import com.fuint.openapi.v1.member.coupon.vo.UserCouponPageReqVO;
import com.fuint.openapi.v1.member.coupon.vo.UserCouponRespVO;
import com.fuint.repository.model.MtCoupon;
import com.fuint.repository.model.MtStore;
import com.fuint.repository.model.MtUserCoupon;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.fuint.framework.util.collection.CollectionUtils.*;
import static com.fuint.framework.util.object.BeanUtils.toBean;

/**
 * OpenAPI用户优惠券服务实现类
 * <p>
 * 独立的OpenAPI用户优惠券服务实现,避免与后台业务冲突
 * 优化性能：批量查询，避免N+1问题
 *
 * @author mjw
 * @since 2026/1/18
 */
@Slf4j
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
     * 分页查询用户优惠券列表（使用 MyBatis Plus 优化性能）
     */
    @Override
    public PageResult<UserCouponRespVO> queryUserCouponPage(UserCouponPageReqVO pageReqVO) throws BusinessCheckException {
        logger.info("[OpenApiUserCouponService] 分页查询用户优惠券列表, 参数: {}", pageReqVO);

        // 使用 MyBatis Plus 进行分页查询
        PageResult<MtUserCoupon> pageResult = userCouponService.getUserCouponPage(pageReqVO);
        List<MtUserCoupon> userCouponList = pageResult.getList();

        if (CollUtil.isEmpty(userCouponList)) {
            return PageResult.empty();
        }

        // 批量收集关联数据的ID（避免N+1查询）
        Set<Integer> couponIds = convertSet(
                filterList(userCouponList, uc -> ObjectUtil.isNotNull(uc.getCouponId())),
                MtUserCoupon::getCouponId
        );

        // 批量查询优惠券信息
        Map<Integer, MtCoupon> couponMap = buildCouponMap(couponIds);

        // 批量查询店铺信息（从优惠券中收集店铺ID）
        Set<Integer> storeIds = collectStoreIds(couponMap.values());
        Map<Integer, String> storeIdToNameMap = buildStoreMap(storeIds);

        // 构建 storeIds -> storeNames 的映射（按优惠券维度）
        Map<String, String> storeNamesMap = buildStoreNamesMap(couponMap.values(), storeIdToNameMap);

        // 批量查询计次卡核销次数（只查询计次卡类型）
        Map<Integer, Long> confirmCountMap = buildConfirmCountMap(userCouponList, couponMap);

        // 获取图片基础路径
        String baseImage = settingService.getUploadBasePath();

        // 转换为响应VO
        List<UserCouponRespVO> respVOList = userCouponList.stream()
                .map(userCoupon -> convertToRespVO(userCoupon, couponMap, storeNamesMap, confirmCountMap, baseImage))
                .collect(Collectors.toList());

        // 构建分页响应
        PageResult<UserCouponRespVO> result = new PageResult<>();
        result.setList(respVOList);
        result.setTotal(pageResult.getTotal());
        result.setTotalPages(pageResult.getTotalPages());
        result.setCurrentPage(pageResult.getCurrentPage());
        result.setPageSize(pageResult.getPageSize());

        return result;
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

        // 构建优惠券Map
        Map<Integer, MtCoupon> couponMap = Collections.singletonMap(couponInfo.getId(), couponInfo);

        // 查询店铺信息
        Set<Integer> storeIds = collectStoreIds(Collections.singletonList(couponInfo));
        Map<Integer, String> storeIdToNameMap = buildStoreMap(storeIds);
        Map<String, String> storeNamesMap = buildStoreNamesMap(Collections.singletonList(couponInfo), storeIdToNameMap);

        // 如果是计次卡，查询核销次数
        Map<Integer, Long> confirmCountMap = buildConfirmCountMap(
                Collections.singletonList(userCoupon),
                couponMap
        );

        // 获取图片基础路径
        String baseImage = settingService.getUploadBasePath();

        return convertToRespVO(userCoupon, couponMap, storeNamesMap, confirmCountMap, baseImage);
    }

    /**
     * 转换MtUserCoupon为响应VO（使用批量查询的结果，优化字段设置）
     */
    private UserCouponRespVO convertToRespVO(MtUserCoupon userCoupon,
                                             Map<Integer, MtCoupon> couponMap,
                                              Map<String, String> storeNamesMap,
                                              Map<Integer, Long> confirmCountMap,
                                              String baseImage) {
        UserCouponRespVO respVO = new UserCouponRespVO();
        respVO.setCreateTime(userCoupon.getCreateTime());
        // 设置用户优惠券基础信息
        respVO.setUserCouponId(userCoupon.getId());
        respVO.setCouponId(userCoupon.getCouponId());
        respVO.setCode(userCoupon.getCode());
        respVO.setStatus(userCoupon.getStatus());
        respVO.setAmount(userCoupon.getAmount());
        respVO.setBalance(userCoupon.getBalance());

        // 从Map中获取优惠券信息（避免重复查询）
        MtCoupon couponInfo = couponMap.get(userCoupon.getCouponId());
        if (couponInfo == null) {
            return respVO;
        }

        // 设置优惠券基本信息
        respVO.setCouponName(couponInfo.getName());
        respVO.setCouponType(couponInfo.getType());

        // 设置图片（拼接完整路径）
        if (StringUtils.isNotEmpty(couponInfo.getImage())) {
            respVO.setImage(baseImage + couponInfo.getImage());
        }

        // 设置使用门槛说明（优化：简化逻辑）
        String outRule = couponInfo.getOutRule();
        if (StringUtils.isEmpty(outRule) || "0".equals(outRule)) {
            respVO.setDescription("无使用门槛");
        } else {
            respVO.setDescription("满" + outRule + "元可用");
        }

        // 设置有效期（格式化为字符串，移除单独的开始/结束时间字段以减少响应大小）
        String effectiveDate = buildEffectiveDate(couponInfo, userCoupon);
        respVO.setEffectiveDate(effectiveDate);
        if (StringUtils.isNotEmpty(effectiveDate) && effectiveDate.contains("~")) {
            try {
                String[] dates = effectiveDate.split("~");
                if (dates.length == 2) {
                    Date startTime = parseDateFromString(dates[0].trim());
                    Date endTime = parseDateFromString(dates[1].trim());
                    respVO.setEffectiveStartTime(startTime);
                    respVO.setEffectiveEndTime(endTime);
                }
            } catch (Exception e) {
                log.warn("解析优惠券有效期失败: {}", effectiveDate, e);
            }
        }

        // 设置适用店铺（从Map中获取，避免重复查询）
        String storeNames = storeNamesMap.getOrDefault(couponInfo.getStoreIds(), "");
        respVO.setStoreNames(storeNames);

        // 判断是否可用（需要同时满足优惠券有效和状态为未使用）
        boolean canUse = couponService.isCouponEffective(couponInfo, userCoupon)
                && UserCouponStatusEnum.UNUSED.getKey().equals(userCoupon.getStatus());
        respVO.setCanUse(canUse);

        // 设置提示信息（根据优惠券类型）
        String tips = buildCouponTips(couponInfo, userCoupon, confirmCountMap);
        respVO.setTips(tips);

        return respVO;
    }

    /**
     * 构建有效期字符串
     */
    private String buildEffectiveDate(MtCoupon couponInfo, MtUserCoupon userCoupon) {
        if (CouponExpireTypeEnum.FIX.getKey().equals(couponInfo.getExpireType())) {
            // 固定有效期
            if (couponInfo.getBeginTime() != null && couponInfo.getEndTime() != null) {
                return DateUtil.formatDate(couponInfo.getBeginTime(), "yyyy.MM.dd HH:mm") + "-"
                        + DateUtil.formatDate(couponInfo.getEndTime(), "yyyy.MM.dd HH:mm");
            }
        } else if (CouponExpireTypeEnum.FLEX.getKey().equals(couponInfo.getExpireType())) {
            // 灵活有效期
            if (userCoupon.getCreateTime() != null && userCoupon.getExpireTime() != null) {
                return DateUtil.formatDate(userCoupon.getCreateTime(), "yyyy.MM.dd HH:mm") + "-"
                        + DateUtil.formatDate(userCoupon.getExpireTime(), "yyyy.MM.dd HH:mm");
            }
        }
        return "";
    }

    /**
     * 构建优惠券提示信息
     */
    private String buildCouponTips(MtCoupon couponInfo, MtUserCoupon userCoupon, Map<Integer, Long> confirmCountMap) {
        String couponType = couponInfo.getType();
        
        // 普通优惠券
        if (CouponTypeEnum.COUPON.getKey().equals(couponType)) {
            String outRule = couponInfo.getOutRule();
            if (StringUtils.isNotEmpty(outRule) && Float.parseFloat(outRule) > 0) {
                return "满" + outRule + "可用";
            } else {
                return "无门槛券";
            }
        }
        
        // 储值卡
        if (CouponTypeEnum.PRESTORE.getKey().equals(couponType)) {
            return "￥" + userCoupon.getAmount() + "，余额￥" + userCoupon.getBalance();
        }
        
        // 计次卡（从Map中获取核销次数，避免重复查询）
        if (CouponTypeEnum.TIMER.getKey().equals(couponType)) {
            Long confirmNum = confirmCountMap.getOrDefault(userCoupon.getId(), 0L);
            return "已使用" + confirmNum + "次，可使用" + couponInfo.getOutRule() + "次";
        }
        
        return "";
    }

    /**
     * 批量构建优惠券Map
     *
     * @param couponIds 优惠券ID集合
     * @return 优惠券Map（key: couponId, value: MtCoupon）
     */
    private Map<Integer, MtCoupon> buildCouponMap(Set<Integer> couponIds) {
        if (CollUtil.isEmpty(couponIds)) {
            return Collections.emptyMap();
        }
        List<MtCoupon> couponList = couponService.queryCouponListByIds(new ArrayList<>(couponIds));
        return convertMap(couponList, MtCoupon::getId);
    }

    /**
     * 从优惠券列表中收集所有店铺ID
     *
     * @param coupons 优惠券集合
     * @return 店铺ID集合
     */
    private Set<Integer> collectStoreIds(Collection<MtCoupon> coupons) {
        Set<Integer> storeIds = new HashSet<>();
        for (MtCoupon coupon : coupons) {
            if (StringUtils.isNotEmpty(coupon.getStoreIds())) {
                String[] storeIdStrs = coupon.getStoreIds().split(",");
                for (String storeIdStr : storeIdStrs) {
                    if (StringUtils.isNotEmpty(storeIdStr.trim())) {
                        try {
                            storeIds.add(Integer.parseInt(storeIdStr.trim()));
                        } catch (NumberFormatException e) {
                            logger.warn("店铺ID格式错误: {}", storeIdStr);
                        }
                    }
                }
            }
        }
        return storeIds;
    }

    /**
     * 批量构建店铺Map
     *
     * @param storeIds 店铺ID集合
     * @return 店铺Map（key: storeId, value: storeName）
     */
    private Map<Integer, String> buildStoreMap(Set<Integer> storeIds) {
        if (CollUtil.isEmpty(storeIds)) {
            return Collections.emptyMap();
        }
        try {
            List<MtStore> stores = storeService.getStoreByIds(storeIds);
            return convertMap(stores, MtStore::getId, MtStore::getName);
        } catch (Exception e) {
            logger.warn("批量查询店铺信息失败", e);
            return Collections.emptyMap();
        }
    }

    /**
     * 构建 storeIds -> storeNames 的映射
     *
     * @param coupons           优惠券集合
     * @param storeIdToNameMap 店铺ID到名称的映射
     * @return storeIds字符串到storeNames字符串的映射
     */
    private Map<String, String> buildStoreNamesMap(Collection<MtCoupon> coupons, Map<Integer, String> storeIdToNameMap) {
        Map<String, String> storeNamesMap = new HashMap<>();
        for (MtCoupon coupon : coupons) {
            if (StringUtils.isNotEmpty(coupon.getStoreIds())) {
                String[] storeIdStrs = coupon.getStoreIds().split(",");
                List<String> storeNames = new ArrayList<>();
                for (String storeIdStr : storeIdStrs) {
                    if (StringUtils.isNotEmpty(storeIdStr.trim())) {
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
                if (!storeNames.isEmpty()) {
                    storeNamesMap.put(coupon.getStoreIds(), String.join(",", storeNames));
                }
            }
        }
        return storeNamesMap;
    }

    /**
     * 批量构建计次卡核销次数Map
     *
     * @param userCouponList 用户优惠券列表
     * @param couponMap      优惠券Map
     * @return 核销次数Map（key: userCouponId, value: confirmCount）
     */
    private Map<Integer, Long> buildConfirmCountMap(List<MtUserCoupon> userCouponList, Map<Integer, MtCoupon> couponMap) {
        // 只查询计次卡类型的核销次数
        Set<Integer> timerUserCouponIds = new HashSet<>();
        for (MtUserCoupon userCoupon : userCouponList) {
            MtCoupon coupon = couponMap.get(userCoupon.getCouponId());
            if (coupon != null && CouponTypeEnum.TIMER.getKey().equals(coupon.getType())) {
                timerUserCouponIds.add(userCoupon.getId());
            }
        }

        if (CollUtil.isEmpty(timerUserCouponIds)) {
            return Collections.emptyMap();
        }

        Map<Integer, Long> confirmCountMap = new HashMap<>();
        for (Integer userCouponId : timerUserCouponIds) {
            try {
                Long confirmNum = confirmLogService.getConfirmNum(userCouponId);
                confirmCountMap.put(userCouponId, confirmNum);
            } catch (Exception e) {
                logger.warn("获取核销次数失败, userCouponId: {}", userCouponId, e);
                confirmCountMap.put(userCouponId, 0L);
            }
        }
        return confirmCountMap;
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
}
