package com.fuint.openapi.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ThreadUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fuint.common.dto.ReqCouponDto;
import com.fuint.common.enums.CouponExpireTypeEnum;
import com.fuint.common.enums.SendWayEnum;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.enums.UserCouponStatusEnum;
import com.fuint.common.mybatis.query.LambdaQueryWrapperX;
import com.fuint.common.service.UserCouponService;
import com.fuint.common.util.CommonUtil;
import com.fuint.framework.annoation.OperationServiceLog;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.exception.ServiceException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.pojo.PageResult;
import com.fuint.framework.util.SeqUtil;
import com.fuint.openapi.service.EventCallbackService;
import com.fuint.openapi.service.OpenApiCouponService;
import com.fuint.openapi.v1.marketing.coupon.vo.MtCouponPageReqVO;
import com.fuint.repository.mapper.MtCouponGoodsMapper;
import com.fuint.repository.mapper.MtCouponMapper;
import com.fuint.repository.mapper.MtUserCouponMapper;
import com.fuint.repository.model.MtCoupon;
import com.fuint.repository.model.MtCouponGoods;
import com.fuint.repository.model.MtUserCoupon;
import com.fuint.repository.utils.MyBatisUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.fuint.framework.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static com.fuint.framework.exception.util.ServiceExceptionUtil.exception;
import static com.fuint.openapi.enums.CouponErrorCodeConstants.*;
import static com.fuint.openapi.enums.OrderErrorCodeConstants.USER_COUPON_ALREADY_USED;
import static com.fuint.openapi.enums.OrderErrorCodeConstants.USER_COUPON_NOT_FOUND;
import static com.fuint.openapi.enums.RedisKeyConstants.COUPON_REVOKE_LOCK;
import static com.fuint.openapi.enums.RedisKeyConstants.USER_COUPON_REVOKE_LOCK;

/**
 * OpenAPI优惠券业务实现类
 * <p>
 * 独立的OpenAPI优惠券服务实现,避免与后台业务冲突
 *
 * @author mjw
 * @since 2026/1/17
 */
@Service
public class OpenApiCouponServiceImpl implements OpenApiCouponService {

    private static final Logger logger = LoggerFactory.getLogger(OpenApiCouponServiceImpl.class);

    @Resource
    private MtCouponMapper mtCouponMapper;

    @Resource
    private MtUserCouponMapper mtUserCouponMapper;

    @Resource
    private UserCouponService userCouponService;

    @Resource
    private MtCouponGoodsMapper mtCouponGoodsMapper;

    /**
     * Redisson 客户端
     */
    @Resource
    private RedissonClient redissonClient;

    @Resource
    private EventCallbackService eventCallbackService;

    /**
     * 分页查询优惠券列表 (Optimized)
     */
    @Override
    public PageResult<MtCoupon> queryCouponPage(MtCouponPageReqVO reqVO) {
        IPage<MtCoupon> mpPage = mtCouponMapper.selectCouponPage(MyBatisUtils.buildPage(reqVO), reqVO);
        // 转换返回
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal(), mpPage.getPages(), mpPage.getCurrent(), mpPage.getSize());
    }

    /**
     * 分页查询优惠券列表
     */
    @Override
    public PaginationResponse<MtCoupon> queryCouponListByPagination(PaginationRequest paginationRequest) throws BusinessCheckException {
        logger.info("[OpenApiCouponService] 分页查询优惠券列表, 参数: {}", paginationRequest);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<MtCoupon> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(paginationRequest.getCurrentPage(), paginationRequest.getPageSize());
        LambdaQueryWrapper<MtCoupon> wrapper = Wrappers.lambdaQuery();
        wrapper.ne(MtCoupon::getStatus, StatusEnum.DISABLE.getKey());

        // 构建查询条件
        Map<String, Object> params = paginationRequest.getSearchParams();
        if (params != null) {
            String name = params.get("name") != null ? params.get("name").toString() : "";
            if (StringUtils.isNotBlank(name)) {
                wrapper.like(MtCoupon::getName, name);
            }

            String status = params.get("status") != null ? params.get("status").toString() : "";
            if (StringUtils.isNotBlank(status)) {
                wrapper.eq(MtCoupon::getStatus, status);
            }

            String groupId = params.get("groupId") != null ? params.get("groupId").toString() : "";
            if (StringUtils.isNotBlank(groupId)) {
                wrapper.eq(MtCoupon::getGroupId, groupId);
            }

            String type = params.get("type") != null ? params.get("type").toString() : "";
            if (StringUtils.isNotBlank(type)) {
                wrapper.eq(MtCoupon::getType, type);
            }

            String merchantId = params.get("merchantId") != null ? params.get("merchantId").toString() : "";
            if (StringUtils.isNotBlank(merchantId)) {
                wrapper.eq(MtCoupon::getMerchantId, merchantId);
            }

            String storeId = params.get("storeId") != null ? params.get("storeId").toString() : "";
            if (StringUtils.isNotBlank(storeId)) {
                wrapper.eq(MtCoupon::getStoreId, storeId);
            }
        }

        wrapper.orderByDesc(MtCoupon::getId);
        mtCouponMapper.selectPage(page, wrapper);

        List<MtCoupon> dataList = page.getRecords();

        // 构造PageImpl用于PaginationResponse (保持原有返回结构)
        // 注意：PageRequest.of 也是0-based，如果PaginationRequest是1-based，这里可能需要调整，但为了保持与原代码一致逻辑，暂时维持原状
        // 原代码：PageRequest.of(paginationRequest.getCurrentPage(), ...)
        PageRequest pageRequest = PageRequest.of(paginationRequest.getCurrentPage() > 0 ? paginationRequest.getCurrentPage() - 1 : 0, paginationRequest.getPageSize());
        PageImpl<MtCoupon> pageImpl = new PageImpl<>(dataList, pageRequest, page.getTotal());

        PaginationResponse<MtCoupon> paginationResponse = new PaginationResponse(pageImpl, MtCoupon.class);
        paginationResponse.setTotalPages((int) page.getPages());
        paginationResponse.setTotalElements(page.getTotal());
        paginationResponse.setContent(dataList);

        logger.info("[OpenApiCouponService] 查询成功, 总记录数: {}", page.getTotal());
        return paginationResponse;
    }

    /**
     * 创建优惠券
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "OpenAPI创建优惠券")
    public MtCoupon createCoupon(ReqCouponDto reqCouponDto) {
        logger.info("[OpenApiCouponService] 创建优惠券, 参数: {}", reqCouponDto);

        // 参数校验
        validateCouponDto(reqCouponDto);

        MtCoupon mtCoupon = new MtCoupon();
        buildCouponFromDto(mtCoupon, reqCouponDto);

        // 创建时间
        mtCoupon.setCreateTime(new Date());
        mtCoupon.setUpdateTime(new Date());

        // 保存优惠券
        mtCouponMapper.insert(mtCoupon);

        // 处理适用商品
        if (StringUtils.isNotEmpty(reqCouponDto.getGoodsIds())) {
            saveCouponGoods(mtCoupon.getId(), reqCouponDto.getGoodsIds());
        }

        logger.info("[OpenApiCouponService] 创建优惠券成功, 优惠券ID: {}", mtCoupon.getId());
        return mtCoupon;
    }

    /**
     * 更新优惠券
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "OpenAPI更新优惠券")
    public void updateCoupon(ReqCouponDto reqCouponDto) {
        // 检查优惠券是否存在
        MtCoupon mtCoupon = mtCouponMapper.selectById(reqCouponDto.getId());
        if (mtCoupon == null) {
            throw exception(COUPON_NOT_FOUND);
        }
        // 参数校验
        validateCouponDto(reqCouponDto);
        buildCouponFromDto(mtCoupon, reqCouponDto);
        mtCoupon.setUpdateTime(new Date());
        // 更新优惠券
        mtCouponMapper.updateById(mtCoupon);
        // 更新适用商品
        if (StringUtils.isNotEmpty(reqCouponDto.getGoodsIds())) {
            // 先删除原有商品
            deleteCouponGoods(mtCoupon.getId());
            // 再添加新商品
            saveCouponGoods(mtCoupon.getId(), reqCouponDto.getGoodsIds());
        }
    }

    /**
     * 根据ID查询优惠券
     */
    @Override
    public MtCoupon queryCouponById(Integer id) throws BusinessCheckException {
        MtCoupon coupon = mtCouponMapper.selectById(id);
        if (coupon == null) {
            throw new ServiceException(COUPON_NOT_FOUND);
        }
        return coupon;
    }

    /**
     * 删除优惠券
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "OpenAPI删除优惠券")
    public void deleteCoupon(Integer id, String operator) {
        MtCoupon coupon = queryCouponById(id);
        coupon.setStatus(StatusEnum.DISABLE.getKey());
        coupon.setUpdateTime(new Date());
        coupon.setOperator(operator);
        mtCouponMapper.updateById(coupon);
    }

    /**
     * 批量发放优惠券
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "OpenAPI批量发放优惠券")
    public void batchSendCoupon(Integer couponId, List<Integer> userIds, Integer num, String uuid, String operator) throws BusinessCheckException {
        RLock lock = redissonClient.getLock(COUPON_REVOKE_LOCK + couponId);
        if (!lock.tryLock()) {
            throw new ServiceException(COUPON_BATCH_PROCESSING);
        }
        try {
            // 检查优惠券是否存在
            MtCoupon coupon = queryCouponById(couponId);
            // 检查优惠券状态
            if (!StatusEnum.ENABLED.getKey().equals(coupon.getStatus())) {
                throw new BusinessCheckException("优惠券未启用,无法发放");
            }
            // 检查剩余数量
            Integer leftNum = getLeftNum(couponId);
            Integer needNum = userIds.size() * num;
            if (coupon.getTotal() > 0 && leftNum < needNum) {
                throw exception(COUPON_STOCK_NOT_ENOUGH, leftNum, needNum);
            }
            // 批量发放
            for (Integer userId : userIds) {
                for (int i = 0; i < num; i++) {
                    sendCouponToUser(coupon, userId, uuid, operator);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 撤销优惠券
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "OpenAPI撤销优惠券")
    public void revokeCoupon(Integer couponId, String uuid, String operator) throws BusinessCheckException {
        RLock lock = redissonClient.getLock(COUPON_REVOKE_LOCK + couponId);
        if (!lock.tryLock()) {
            throw new ServiceException(COUPON_REVOKE_PROCESSING);
        }
        try {
            List<MtUserCoupon> userCoupons = mtUserCouponMapper.selectList(
                    new LambdaQueryWrapperX<MtUserCoupon>()
                            .eq(MtUserCoupon::getCouponId, couponId)
                            .eq(MtUserCoupon::getUuid, uuid)
                            .eq(MtUserCoupon::getStatus, UserCouponStatusEnum.UNUSED.getKey())
                            .eqIfPresent(MtUserCoupon::getUuid, uuid)
            );
            for (MtUserCoupon userCoupon : userCoupons) {
                ThreadUtil.execAsync(() -> eventCallbackService.sendCouponEventCallback(userCoupon, "REVOKED", null));
            }
            LambdaUpdateWrapper<MtUserCoupon> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(MtUserCoupon::getStatus, UserCouponStatusEnum.DISABLE.getKey());
            updateWrapper.set(MtUserCoupon::getUpdateTime, new Date());
            updateWrapper.set(MtUserCoupon::getOperator, operator);
            updateWrapper.eq(MtUserCoupon::getCouponId, couponId);
            updateWrapper.eq(MtUserCoupon::getStatus, UserCouponStatusEnum.UNUSED.getKey());
            updateWrapper.eq(StringUtils.isNotBlank(uuid), MtUserCoupon::getUuid, uuid);
            userCouponService.update(updateWrapper);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void revokeCoupon(Integer userCouponId) {
        RLock lock = redissonClient.getLock(USER_COUPON_REVOKE_LOCK + userCouponId);
        if (!lock.tryLock()) {
            throw new ServiceException(COUPON_REVOKE_PROCESSING);
        }
        try {
            MtUserCoupon coupon = mtUserCouponMapper.selectById(userCouponId);
            if (coupon == null) {
                throw new ServiceException(USER_COUPON_NOT_FOUND);
            }
            if (UserCouponStatusEnum.USED.getKey().equals(coupon.getStatus())) {
                throw new ServiceException(USER_COUPON_ALREADY_USED);
            }
            coupon.setStatus(UserCouponStatusEnum.DISABLE.getKey());
            coupon.setUpdateTime(new Date());
            coupon.setOperator("openapi");
            mtUserCouponMapper.updateById(coupon);

            coupon = mtUserCouponMapper.selectById(userCouponId);
            eventCallbackService.sendCouponEventCallback(coupon, "REVOKED", null);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取已发放数量
     */
    @Override
    public Integer getSendNum(Integer couponId) {
        Long num = mtUserCouponMapper.getSendNum(couponId);
        return num != null ? num.intValue() : 0;
    }

    /**
     * 获取剩余数量
     */
    @Override
    public Integer getLeftNum(Integer couponId) {
        MtCoupon coupon = mtCouponMapper.selectById(couponId);
        if (coupon == null || coupon.getTotal() == null) {
            return 0;
        }

        Integer sendNum = getSendNum(couponId);
        Integer leftNum = coupon.getTotal() - sendNum;
        return Math.max(leftNum, 0);
    }

    /**
     * 获取优惠券关联的商品ID列表
     */
    @Override
    public List<Integer> getCouponGoodsIds(Integer couponId) {
        List<MtCouponGoods> couponGoods = mtCouponGoodsMapper.getCouponGoods(couponId);
        if (couponGoods == null || couponGoods.isEmpty()) {
            return Collections.emptyList();
        }

        return couponGoods.stream()
                .map(MtCouponGoods::getGoodsId)
                .collect(Collectors.toList());
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 校验优惠券DTO参数
     */
    private void validateCouponDto(ReqCouponDto dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new ServiceException(BAD_REQUEST.getCode(), "优惠券名称不能为空");
        }

        if (dto.getExpireType() == null) {
            throw new ServiceException(BAD_REQUEST.getCode(), "有效期类型不能为空");
        }
        // 固定有效期验证
        if (CouponExpireTypeEnum.FIX.getKey().equals(dto.getExpireType())) {
            if (StringUtils.isEmpty(dto.getBeginTime()) || StringUtils.isEmpty(dto.getEndTime())) {
                throw new ServiceException(BAD_REQUEST.getCode(), "固定有效期的开始时间和结束时间不能为空");
            }
            Date startTime = cn.hutool.core.date.DateUtil.parse(dto.getBeginTime(), DatePattern.NORM_DATETIME_PATTERN);
            Date endTime = cn.hutool.core.date.DateUtil.parse(dto.getEndTime(), DatePattern.NORM_DATETIME_PATTERN);
            if (endTime.before(startTime)) {
                throw new ServiceException(BAD_REQUEST.getCode(), "结束时间不能早于开始时间");
            }
        }

        // 灵活有效期验证
        if (CouponExpireTypeEnum.FLEX.getKey().equals(dto.getExpireType())) {
            if (dto.getExpireTime() == null || dto.getExpireTime() <= 0) {
                throw new ServiceException(BAD_REQUEST.getCode(), "灵活有效期的天数必须大于0");
            }
        }
    }

    /**
     * 从DTO构建优惠券对象
     */
    private void buildCouponFromDto(MtCoupon coupon, ReqCouponDto dto) {
        // 基本信息
        if (dto.getMerchantId() != null) {
            coupon.setMerchantId(dto.getMerchantId());
        }
        if (dto.getStoreId() != null) {
            coupon.setStoreId(dto.getStoreId());
        }
        if (dto.getGroupId() != null) {
            coupon.setGroupId(dto.getGroupId());
        }
        if (dto.getType() != null) {
            coupon.setType(dto.getType());
        }
        if (dto.getName() != null) {
            coupon.setName(CommonUtil.replaceXSS(dto.getName()));
        }

        // 发放设置
        if (dto.getTotal() != null) {
            coupon.setTotal(dto.getTotal());
        } else {
            coupon.setTotal(0);
        }

        if (dto.getSendNum() != null) {
            coupon.setSendNum(dto.getSendNum());
        } else {
            coupon.setSendNum(1);
        }

        if (dto.getLimitNum() != null) {
            coupon.setLimitNum(dto.getLimitNum());
        } else {
            coupon.setLimitNum(1);
        }

        // 金额设置
        if (dto.getAmount() != null) {
            coupon.setAmount(dto.getAmount());
        } else {
            coupon.setAmount(BigDecimal.ZERO);
        }

//        if (dto.getDiscountRate() != null) {
//            coupon.setDiscountRate(dto.getDiscountRate());
//        }
//
//        if (dto.getMaxDiscountAmount() != null) {
//            coupon.setMaxDiscountAmount(dto.getMaxDiscountAmount());
//        }

        // 有效期设置
        coupon.setExpireType(dto.getExpireType());
        if (CouponExpireTypeEnum.FIX.getKey().equals(dto.getExpireType())) {
            coupon.setBeginTime(DateUtil.parse(dto.getBeginTime(), DatePattern.NORM_DATETIME_PATTERN));
            coupon.setEndTime(DateUtil.parse(dto.getEndTime(), DatePattern.NORM_DATETIME_PATTERN));
        } else if (CouponExpireTypeEnum.FLEX.getKey().equals(dto.getExpireType())) {
            coupon.setExpireTime(dto.getExpireTime());
        }
        // 使用规则
        if (dto.getInRule() != null) {
            coupon.setInRule(CommonUtil.replaceXSS(dto.getInRule()));
        }
        if (dto.getOutRule() != null) {
            coupon.setOutRule(CommonUtil.replaceXSS(dto.getOutRule()));
        }
        if (dto.getExceptTime() != null) {
            coupon.setExceptTime(CommonUtil.replaceXSS(dto.getExceptTime()));
        }

        // 适用范围
        if (dto.getApplyGoods() != null) {
            coupon.setApplyGoods(dto.getApplyGoods());
        }
        if (dto.getUseFor() != null) {
            coupon.setUseFor(dto.getUseFor());
        }
        if (dto.getStoreIds() != null) {
            coupon.setStoreIds(dto.getStoreIds());
        }
        if (dto.getGradeIds() != null) {
            coupon.setGradeIds(dto.getGradeIds());
        }

        // 其他信息
        if (dto.getDescription() != null) {
            coupon.setDescription(CommonUtil.replaceXSS(dto.getDescription()));
        }
        if (dto.getRemarks() != null) {
            coupon.setRemarks(CommonUtil.replaceXSS(dto.getRemarks()));
        }
        if (dto.getImage() != null) {
            coupon.setImage(dto.getImage());
        }

        // 状态和操作人
        if (dto.getStatus() != null) {
            coupon.setStatus(dto.getStatus());
        } else {
            coupon.setStatus(StatusEnum.ENABLED.getKey());
        }

        if (dto.getOperator() != null) {
            coupon.setOperator(dto.getOperator());
        }

        // 发放方式
        if (dto.getSendWay() != null) {
            coupon.setSendWay(dto.getSendWay());
        } else {
            coupon.setSendWay(SendWayEnum.FRONT.getKey());
        }

        // 积分设置
        if (dto.getPoint() != null) {
            coupon.setPoint(dto.getPoint());
        } else {
            coupon.setPoint(0);
        }

        // 领取码
        if (dto.getReceiveCode() != null) {
            coupon.setReceiveCode(dto.getReceiveCode());
        } else {
            coupon.setReceiveCode("");
        }

        // 是否可转赠
        if (dto.getIsGive() != null) {
            coupon.setIsGive(dto.getIsGive() == 1);
        } else {
            coupon.setIsGive(false);
        }
    }

    /**
     * 发放优惠券给用户
     */
    private void sendCouponToUser(MtCoupon coupon, Integer userId, String uuid, String operator) {
        MtUserCoupon userCoupon = new MtUserCoupon();
        userCoupon.setMerchantId(coupon.getMerchantId());
        userCoupon.setStoreId(coupon.getStoreId());
        userCoupon.setCouponId(coupon.getId());
        userCoupon.setGroupId(coupon.getGroupId());
        userCoupon.setUserId(userId);
        userCoupon.setStatus(UserCouponStatusEnum.UNUSED.getKey());
        userCoupon.setType(coupon.getType());
        userCoupon.setAmount(coupon.getAmount());
        userCoupon.setBalance(coupon.getAmount());
        userCoupon.setImage(coupon.getImage());
        userCoupon.setUuid(uuid);
        userCoupon.setOperator(operator);
        userCoupon.setCreateTime(new Date());
        userCoupon.setUpdateTime(new Date());

        // 设置过期时间
        if (CouponExpireTypeEnum.FIX.getKey().equals(coupon.getExpireType())) {
            userCoupon.setExpireTime(coupon.getEndTime());
        } else if (CouponExpireTypeEnum.FLEX.getKey().equals(coupon.getExpireType())) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, coupon.getExpireTime());
            userCoupon.setExpireTime(calendar.getTime());
        }

        // 生成券码 (12位随机数)
        String code = SeqUtil.getRandomNumber(4) +
                SeqUtil.getRandomNumber(4) +
                SeqUtil.getRandomNumber(4);
        userCoupon.setCode(code);

        mtUserCouponMapper.insert(userCoupon);
    }

    /**
     * 保存优惠券适用商品
     */
    private void saveCouponGoods(Integer couponId, String goodsIds) {
        if (StringUtils.isEmpty(goodsIds)) {
            return;
        }

        String[] goodsIdArray = goodsIds.split(",");
        for (String goodsIdStr : goodsIdArray) {
            if (StringUtils.isEmpty(goodsIdStr)) {
                continue;
            }

            try {
                Integer goodsId = Integer.parseInt(goodsIdStr.trim());
                MtCouponGoods couponGoods = new MtCouponGoods();
                couponGoods.setCouponId(couponId);
                couponGoods.setGoodsId(goodsId);
                couponGoods.setStatus(StatusEnum.ENABLED.getKey());
                couponGoods.setCreateTime(new Date());
                couponGoods.setUpdateTime(new Date());
                mtCouponGoodsMapper.insert(couponGoods);
            } catch (NumberFormatException e) {
                logger.warn("[OpenApiCouponService] 无效的商品ID: {}", goodsIdStr);
            }
        }
    }

    /**
     * 删除优惠券适用商品
     */
    private void deleteCouponGoods(Integer couponId) {
        List<MtCouponGoods> couponGoodsList = mtCouponGoodsMapper.getCouponGoods(couponId);
        if (couponGoodsList != null && !couponGoodsList.isEmpty()) {
            for (MtCouponGoods couponGoods : couponGoodsList) {
                mtCouponGoodsMapper.deleteById(couponGoods.getId());
            }
        }
    }
}
