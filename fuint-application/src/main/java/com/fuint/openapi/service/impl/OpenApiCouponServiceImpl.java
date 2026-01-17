package com.fuint.openapi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fuint.common.dto.ReqCouponDto;
import com.fuint.common.enums.CouponExpireTypeEnum;
import com.fuint.common.enums.CouponTypeEnum;
import com.fuint.common.enums.SendWayEnum;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.enums.UserCouponStatusEnum;
import com.fuint.common.util.CommonUtil;
import com.fuint.common.util.DateUtil;
import com.fuint.framework.annoation.OperationServiceLog;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.openapi.service.OpenApiCouponService;
import com.fuint.repository.mapper.MtCouponGoodsMapper;
import com.fuint.repository.mapper.MtCouponMapper;
import com.fuint.repository.mapper.MtUserCouponMapper;
import com.fuint.repository.model.MtCoupon;
import com.fuint.repository.model.MtCouponGoods;
import com.fuint.repository.model.MtUserCoupon;
import com.fuint.utils.SeqUtil;
import com.fuint.utils.StringUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * OpenAPI优惠券业务实现类
 * <p>
 * 独立的OpenAPI优惠券服务实现,避免与后台业务冲突
 *
 * @author mjw
 * @since 2026/1/17
 */
@Service
@AllArgsConstructor
public class OpenApiCouponServiceImpl implements OpenApiCouponService {

    private static final Logger logger = LoggerFactory.getLogger(OpenApiCouponServiceImpl.class);

    private final MtCouponMapper mtCouponMapper;
    private final MtUserCouponMapper mtUserCouponMapper;
    private final MtCouponGoodsMapper mtCouponGoodsMapper;

    /**
     * 分页查询优惠券列表
     */
    @Override
    public PaginationResponse<MtCoupon> queryCouponListByPagination(PaginationRequest paginationRequest) throws BusinessCheckException {
        logger.info("[OpenApiCouponService] 分页查询优惠券列表, 参数: {}", paginationRequest);

        Page<MtCoupon> pageHelper = PageHelper.startPage(paginationRequest.getCurrentPage(), paginationRequest.getPageSize());
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
        List<MtCoupon> dataList = mtCouponMapper.selectList(wrapper);

        PageRequest pageRequest = PageRequest.of(paginationRequest.getCurrentPage(), paginationRequest.getPageSize());
        PageImpl pageImpl = new PageImpl(dataList, pageRequest, pageHelper.getTotal());
        PaginationResponse<MtCoupon> paginationResponse = new PaginationResponse(pageImpl, MtCoupon.class);
        paginationResponse.setTotalPages(pageHelper.getPages());
        paginationResponse.setTotalElements(pageHelper.getTotal());
        paginationResponse.setContent(dataList);

        logger.info("[OpenApiCouponService] 查询成功, 总记录数: {}", pageHelper.getTotal());
        return paginationResponse;
    }

    /**
     * 创建优惠券
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "OpenAPI创建优惠券")
    public MtCoupon createCoupon(ReqCouponDto reqCouponDto) throws BusinessCheckException, ParseException {
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
        if (StringUtil.isNotEmpty(reqCouponDto.getGoodsIds())) {
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
    public MtCoupon updateCoupon(ReqCouponDto reqCouponDto) throws BusinessCheckException, ParseException {
        logger.info("[OpenApiCouponService] 更新优惠券, 参数: {}", reqCouponDto);

        if (reqCouponDto.getId() == null) {
            throw new BusinessCheckException("优惠券ID不能为空");
        }

        // 检查优惠券是否存在
        MtCoupon mtCoupon = mtCouponMapper.selectById(reqCouponDto.getId());
        if (mtCoupon == null) {
            throw new BusinessCheckException("优惠券不存在");
        }

        // 参数校验
        validateCouponDto(reqCouponDto);

        buildCouponFromDto(mtCoupon, reqCouponDto);
        mtCoupon.setUpdateTime(new Date());

        // 更新优惠券
        mtCouponMapper.updateById(mtCoupon);

        // 更新适用商品
        if (StringUtil.isNotEmpty(reqCouponDto.getGoodsIds())) {
            // 先删除原有商品
            deleteCouponGoods(mtCoupon.getId());
            // 再添加新商品
            saveCouponGoods(mtCoupon.getId(), reqCouponDto.getGoodsIds());
        }

        logger.info("[OpenApiCouponService] 更新优惠券成功, 优惠券ID: {}", mtCoupon.getId());
        return mtCoupon;
    }

    /**
     * 根据ID查询优惠券
     */
    @Override
    public MtCoupon queryCouponById(Integer id) throws BusinessCheckException {
        if (id == null || id <= 0) {
            throw new BusinessCheckException("优惠券ID不能为空");
        }

        MtCoupon coupon = mtCouponMapper.selectById(id);
        if (coupon == null) {
            throw new BusinessCheckException("优惠券不存在");
        }

        return coupon;
    }

    /**
     * 删除优惠券
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "OpenAPI删除优惠券")
    public void deleteCoupon(Integer id, String operator) throws BusinessCheckException {
        logger.info("[OpenApiCouponService] 删除优惠券, 优惠券ID: {}, 操作人: {}", id, operator);

        MtCoupon coupon = queryCouponById(id);
        coupon.setStatus(StatusEnum.DISABLE.getKey());
        coupon.setUpdateTime(new Date());
        coupon.setOperator(operator);

        mtCouponMapper.updateById(coupon);
        logger.info("[OpenApiCouponService] 删除优惠券成功, 优惠券ID: {}", id);
    }

    /**
     * 批量发放优惠券
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "OpenAPI批量发放优惠券")
    public Boolean batchSendCoupon(Integer couponId, List<Integer> userIds, Integer num, String uuid, String operator) throws BusinessCheckException {
        logger.info("[OpenApiCouponService] 批量发放优惠券, 优惠券ID: {}, 用户数: {}, 数量: {}, 批次号: {}", 
                couponId, userIds.size(), num, uuid);

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
            throw new BusinessCheckException("优惠券库存不足,剩余: " + leftNum + ", 需要: " + needNum);
        }

        // 批量发放
        for (Integer userId : userIds) {
            for (int i = 0; i < num; i++) {
                sendCouponToUser(coupon, userId, uuid, operator);
            }
        }

        logger.info("[OpenApiCouponService] 批量发放优惠券成功, 优惠券ID: {}, 发放数量: {}", couponId, needNum);
        return true;
    }

    /**
     * 撤销优惠券
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "OpenAPI撤销优惠券")
    public void revokeCoupon(Integer couponId, String uuid, String operator) throws BusinessCheckException {
        logger.info("[OpenApiCouponService] 撤销优惠券, 优惠券ID: {}, 批次号: {}", couponId, uuid);

        LambdaQueryWrapper<MtUserCoupon> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(MtUserCoupon::getCouponId, couponId);
        wrapper.eq(MtUserCoupon::getUuid, uuid);
        wrapper.eq(MtUserCoupon::getStatus, UserCouponStatusEnum.UNUSED.getKey());

        List<MtUserCoupon> userCoupons = mtUserCouponMapper.selectList(wrapper);
        
        if (userCoupons.isEmpty()) {
            logger.warn("[OpenApiCouponService] 未找到可撤销的优惠券");
            return;
        }

        for (MtUserCoupon userCoupon : userCoupons) {
            userCoupon.setStatus(UserCouponStatusEnum.DISABLE.getKey());
            userCoupon.setUpdateTime(new Date());
            userCoupon.setOperator(operator);
            mtUserCouponMapper.updateById(userCoupon);
        }

        logger.info("[OpenApiCouponService] 撤销优惠券成功, 撤销数量: {}", userCoupons.size());
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
    private void validateCouponDto(ReqCouponDto dto) throws BusinessCheckException, ParseException {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new BusinessCheckException("优惠券名称不能为空");
        }

        if (dto.getType() == null) {
            throw new BusinessCheckException("优惠券类型不能为空");
        }

        if (dto.getExpireType() == null) {
            throw new BusinessCheckException("有效期类型不能为空");
        }

        // 固定有效期验证
        if (CouponExpireTypeEnum.FIX.getKey().equals(dto.getExpireType())) {
            if (StringUtil.isEmpty(dto.getBeginTime()) || StringUtil.isEmpty(dto.getEndTime())) {
                throw new BusinessCheckException("固定有效期的开始时间和结束时间不能为空");
            }
            Date startTime = DateUtil.parseDate(dto.getBeginTime());
            Date endTime = DateUtil.parseDate(dto.getEndTime());
            if (endTime.before(startTime)) {
                throw new BusinessCheckException("结束时间不能早于开始时间");
            }
        }

        // 灵活有效期验证
        if (CouponExpireTypeEnum.FLEX.getKey().equals(dto.getExpireType())) {
            if (dto.getExpireTime() == null || dto.getExpireTime() <= 0) {
                throw new BusinessCheckException("灵活有效期的天数必须大于0");
            }
        }
    }

    /**
     * 从DTO构建优惠券对象
     */
    private void buildCouponFromDto(MtCoupon coupon, ReqCouponDto dto) throws ParseException {
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

        if (dto.getDiscountRate() != null) {
            coupon.setDiscountRate(dto.getDiscountRate());
        }

        if (dto.getMaxDiscountAmount() != null) {
            coupon.setMaxDiscountAmount(dto.getMaxDiscountAmount());
        }

        // 有效期设置
        coupon.setExpireType(dto.getExpireType());
        if (CouponExpireTypeEnum.FIX.getKey().equals(dto.getExpireType())) {
            coupon.setBeginTime(DateUtil.parseDate(dto.getBeginTime()));
            coupon.setEndTime(DateUtil.parseDate(dto.getEndTime()));
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
        if (StringUtil.isEmpty(goodsIds)) {
            return;
        }

        String[] goodsIdArray = goodsIds.split(",");
        for (String goodsIdStr : goodsIdArray) {
            if (StringUtil.isEmpty(goodsIdStr)) {
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
