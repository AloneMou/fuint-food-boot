package com.fuint.openapi.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fuint.common.dto.ReqCouponGroupDto;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.util.CommonUtil;
import com.fuint.framework.annoation.OperationServiceLog;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.pojo.PageResult;
import com.fuint.framework.util.object.BeanUtils;
import com.fuint.openapi.service.OpenApiCouponGroupService;
import com.fuint.openapi.v1.marketing.group.vo.CouponGroupPageReqVO;
import com.fuint.repository.mapper.MtCouponGroupMapper;
import com.fuint.repository.mapper.MtCouponMapper;
import com.fuint.repository.mapper.MtUserCouponMapper;
import com.fuint.repository.model.MtCoupon;
import com.fuint.repository.model.MtCouponGroup;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

import static com.fuint.framework.exception.util.ServiceExceptionUtil.exception;
import static com.fuint.openapi.enums.CouponGroupErrorCodeConstants.COUPON_GROUP_ALREADY_DELETED;
import static com.fuint.openapi.enums.CouponGroupErrorCodeConstants.COUPON_GROUP_NOT_FOUND;

/**
 * OpenAPI优惠券分组业务实现类
 * <p>
 * 独立的OpenAPI优惠券分组服务实现,避免与后台业务冲突
 *
 * @author mjw
 * @since 2026/1/17
 */
@Service
@AllArgsConstructor
public class OpenApiCouponGroupServiceImpl implements OpenApiCouponGroupService {

    private static final Logger logger = LoggerFactory.getLogger(OpenApiCouponGroupServiceImpl.class);

    private final MtCouponGroupMapper mtCouponGroupMapper;
    private final MtCouponMapper mtCouponMapper;
    private final MtUserCouponMapper mtUserCouponMapper;

    /**
     * 分页查询优惠券分组列表
     */
    @Override
    public PaginationResponse<MtCouponGroup> queryCouponGroupListByPagination(PaginationRequest paginationRequest) throws BusinessCheckException {
        logger.info("[OpenApiCouponGroupService] 分页查询优惠券分组列表, 参数: {}", paginationRequest);

        Page<MtCouponGroup> pageHelper = PageHelper.startPage(paginationRequest.getCurrentPage(), paginationRequest.getPageSize());
        LambdaQueryWrapper<MtCouponGroup> wrapper = Wrappers.lambdaQuery();
        wrapper.ne(MtCouponGroup::getStatus, StatusEnum.DISABLE.getKey());

        // 构建查询条件
        Map<String, Object> params = paginationRequest.getSearchParams();
        if (params != null) {
            String name = params.get("name") != null ? params.get("name").toString() : "";
            if (StringUtils.isNotBlank(name)) {
                wrapper.like(MtCouponGroup::getName, name);
            }

            String status = params.get("status") != null ? params.get("status").toString() : "";
            if (StringUtils.isNotBlank(status)) {
                wrapper.eq(MtCouponGroup::getStatus, status);
            }

            String id = params.get("id") != null ? params.get("id").toString() : "";
            if (StringUtils.isNotBlank(id)) {
                wrapper.eq(MtCouponGroup::getId, id);
            }

            String merchantId = params.get("merchantId") != null ? params.get("merchantId").toString() : "";
            if (StringUtils.isNotBlank(merchantId)) {
                wrapper.eq(MtCouponGroup::getMerchantId, merchantId);
            }

            String storeId = params.get("storeId") != null ? params.get("storeId").toString() : "";
            if (StringUtils.isNotBlank(storeId)) {
                wrapper.eq(MtCouponGroup::getStoreId, storeId);
            }
        }

        wrapper.orderByDesc(MtCouponGroup::getId);
        List<MtCouponGroup> dataList = mtCouponGroupMapper.selectList(wrapper);

        PageRequest pageRequest = PageRequest.of(paginationRequest.getCurrentPage(), paginationRequest.getPageSize());
        PageImpl pageImpl = new PageImpl(dataList, pageRequest, pageHelper.getTotal());
        PaginationResponse<MtCouponGroup> paginationResponse = new PaginationResponse(pageImpl, MtCouponGroup.class);
        paginationResponse.setTotalPages(pageHelper.getPages());
        paginationResponse.setTotalElements(pageHelper.getTotal());
        paginationResponse.setContent(dataList);

        logger.info("[OpenApiCouponGroupService] 查询成功, 总记录数: {}", pageHelper.getTotal());
        return paginationResponse;
    }

    /**
     * 创建优惠券分组
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "OpenAPI创建优惠券分组")
    public MtCouponGroup createCouponGroup(ReqCouponGroupDto reqCouponGroupDto) throws BusinessCheckException {
        MtCouponGroup couponGroup = new MtCouponGroup();
        couponGroup.setMerchantId(reqCouponGroupDto.getMerchantId());
        couponGroup.setStoreId(reqCouponGroupDto.getStoreId());
        couponGroup.setName(CommonUtil.replaceXSS(reqCouponGroupDto.getName()));
        // 设置默认值
        if (reqCouponGroupDto.getMoney() != null) {
            couponGroup.setMoney(reqCouponGroupDto.getMoney());
        } else {
            couponGroup.setMoney(BigDecimal.ZERO);
        }
        if (reqCouponGroupDto.getTotal() != null) {
            couponGroup.setTotal(reqCouponGroupDto.getTotal());
        } else {
            couponGroup.setTotal(0);
        }
        if (reqCouponGroupDto.getDescription() != null) {
            couponGroup.setDescription(CommonUtil.replaceXSS(reqCouponGroupDto.getDescription()));
        }
        couponGroup.setStatus(StatusEnum.ENABLED.getKey());
        couponGroup.setCreateTime(new Date());
        couponGroup.setUpdateTime(new Date());
        couponGroup.setNum(0);
        // OpenAPI操作时，默认operator为"openapi"
        couponGroup.setOperator(reqCouponGroupDto.getOperator() != null ? reqCouponGroupDto.getOperator() : "openapi");
        mtCouponGroupMapper.insert(couponGroup);
        return couponGroup;
    }

    /**
     * 更新优惠券分组
     *
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "OpenAPI更新优惠券分组")
    public void updateCouponGroup(ReqCouponGroupDto reqCouponGroupDto) throws BusinessCheckException {
        // 检查分组是否存在
        MtCouponGroup couponGroup = mtCouponGroupMapper.selectById(reqCouponGroupDto.getId());
        if (couponGroup == null) {
            throw exception(COUPON_GROUP_NOT_FOUND);
        }
        if (StatusEnum.DISABLE.getKey().equals(couponGroup.getStatus())) {
            throw exception(COUPON_GROUP_ALREADY_DELETED);
        }
        MtCouponGroup updateObj = BeanUtils.toBean(reqCouponGroupDto, MtCouponGroup.class);
        // 更新字段
        if (StringUtils.isNotBlank(reqCouponGroupDto.getName())) {
            updateObj.setName(CommonUtil.replaceXSS(reqCouponGroupDto.getName()));
        }
        updateObj.setUpdateTime(new Date());
        // OpenAPI操作时，默认operator为"openapi"
        updateObj.setOperator("openapi");
        mtCouponGroupMapper.updateById(updateObj);
    }

    /**
     * 根据ID查询优惠券分组
     */
    @Override
    public MtCouponGroup queryCouponGroupById(Integer id) throws BusinessCheckException {
        if (id == null || id <= 0) {
            throw new BusinessCheckException("分组ID不能为空");
        }
        MtCouponGroup couponGroup = mtCouponGroupMapper.selectById(id);
        if (couponGroup == null) {
            throw exception(COUPON_GROUP_NOT_FOUND);
        }
        if (StatusEnum.DISABLE.getKey().equals(couponGroup.getStatus())) {
            throw exception(COUPON_GROUP_ALREADY_DELETED);
        }
        return couponGroup;
    }

    /**
     * 删除优惠券分组
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "OpenAPI删除优惠券分组")
    public void deleteCouponGroup(Integer id, String operator) throws BusinessCheckException {
        MtCouponGroup couponGroup = queryCouponGroupById(id);
        if (couponGroup == null) {
            throw exception(COUPON_GROUP_NOT_FOUND);
        }
        if (StatusEnum.DISABLE.getKey().equals(couponGroup.getStatus())) {
            throw exception(COUPON_GROUP_ALREADY_DELETED);
        }
        couponGroup.setStatus(StatusEnum.DISABLE.getKey());
        couponGroup.setUpdateTime(new Date());
        // OpenAPI操作时，默认operator为"openapi"
        couponGroup.setOperator(operator != null ? operator : "openapi");
        mtCouponGroupMapper.updateById(couponGroup);
    }

    /**
     * 获取分组下的券种类数量
     */
    @Override
    public Integer getCouponNum(Integer id) throws BusinessCheckException {
        if (id == null || id <= 0) {
            return 0;
        }

        Long num = mtCouponMapper.queryNumByGroupId(id);
        return num != null ? num.intValue() : 0;
    }

    /**
     * 获取分组下的券总价值
     */
    @Override
    public BigDecimal getCouponMoney(Integer id) throws BusinessCheckException {
        if (id == null || id <= 0) {
            return BigDecimal.ZERO;
        }

        MtCouponGroup groupInfo = mtCouponGroupMapper.selectById(id);
        if (groupInfo == null) {
            return BigDecimal.ZERO;
        }

        List<MtCoupon> couponList = mtCouponMapper.queryByGroupId(id);
        if (couponList == null || couponList.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalMoney = BigDecimal.ZERO;
        for (MtCoupon coupon : couponList) {
            if (coupon.getAmount() != null && coupon.getSendNum() != null) {
                // 单张券金额 * 每套数量 * 套数
                BigDecimal couponMoney = coupon.getAmount()
                        .multiply(BigDecimal.valueOf(coupon.getSendNum()))
                        .multiply(BigDecimal.valueOf(groupInfo.getTotal() != null ? groupInfo.getTotal() : 0));
                totalMoney = totalMoney.add(couponMoney);
            }
        }

        return totalMoney;
    }

    /**
     * 获取分组已发放套数
     */
    @Override
    public Integer getSendNum(Integer id) throws BusinessCheckException {
        if (id == null || id <= 0) {
            return 0;
        }
        // 查询该分组下所有券的已发放数量
        List<MtCoupon> couponList = mtCouponMapper.queryByGroupId(id);
        if (CollUtil.isEmpty(couponList)) {
            return 0;
        }

        // 取第一个券的已发放套数作为整体发放套数
        // 因为套餐是整体发放的,所以只需要查一个券的发放数即可
        if (!couponList.isEmpty()) {
            Integer couponId = couponList.get(0).getId();
            Long sendNum = mtUserCouponMapper.getSendNum(couponId);
            if (sendNum != null && couponList.get(0).getSendNum() != null && couponList.get(0).getSendNum() > 0) {
                // 总发放数 / 每套数量 = 发放套数
                return (int) (sendNum / couponList.get(0).getSendNum());
            }
        }

        return 0;
    }

    @Override
    public PageResult<MtCouponGroup> getCouponGroupPage(CouponGroupPageReqVO pageReqVO) {
        return mtCouponGroupMapper.selectCouponGroupPage(pageReqVO);
    }

    @Override
    public List<MtCouponGroup> getCouponGroupListByIds(Collection<Integer> ids) {
        if (CollUtil.isNotEmpty(ids)) {
            return mtCouponGroupMapper.selectBatchIds(ids);
        }
        return Collections.emptyList();
    }
}
