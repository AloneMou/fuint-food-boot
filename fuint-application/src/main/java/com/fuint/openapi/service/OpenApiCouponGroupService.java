package com.fuint.openapi.service;

import com.fuint.common.dto.ReqCouponGroupDto;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.repository.model.MtCouponGroup;

import java.math.BigDecimal;

/**
 * OpenAPI优惠券分组业务接口
 * <p>
 * 独立的OpenAPI优惠券分组服务接口,避免与后台业务冲突
 *
 * @author mjw
 * @since 2026/1/17
 */
public interface OpenApiCouponGroupService {

    /**
     * 分页查询优惠券分组列表
     *
     * @param paginationRequest 分页请求参数
     * @return 分页结果
     * @throws BusinessCheckException 业务异常
     */
    PaginationResponse<MtCouponGroup> queryCouponGroupListByPagination(PaginationRequest paginationRequest) throws BusinessCheckException;

    /**
     * 创建优惠券分组
     *
     * @param reqCouponGroupDto 分组信息
     * @return 创建的分组
     * @throws BusinessCheckException 业务异常
     */
    MtCouponGroup createCouponGroup(ReqCouponGroupDto reqCouponGroupDto) throws BusinessCheckException;

    /**
     * 更新优惠券分组
     *
     * @param reqCouponGroupDto 分组信息
     * @return 更新后的分组
     * @throws BusinessCheckException 业务异常
     */
    void updateCouponGroup(ReqCouponGroupDto reqCouponGroupDto) throws BusinessCheckException;

    /**
     * 根据ID获取优惠券分组信息
     *
     * @param id 分组ID
     * @return 分组信息
     * @throws BusinessCheckException 业务异常
     */
    MtCouponGroup queryCouponGroupById(Integer id) throws BusinessCheckException;

    /**
     * 删除优惠券分组
     *
     * @param id       分组ID
     * @param operator 操作人
     * @throws BusinessCheckException 业务异常
     */
    void deleteCouponGroup(Integer id, String operator) throws BusinessCheckException;

    /**
     * 获取分组下的券种类数量
     *
     * @param id 分组ID
     * @return 券种类数量
     * @throws BusinessCheckException 业务异常
     */
    Integer getCouponNum(Integer id) throws BusinessCheckException;

    /**
     * 获取分组下的券总价值
     *
     * @param id 分组ID
     * @return 券总价值
     * @throws BusinessCheckException 业务异常
     */
    BigDecimal getCouponMoney(Integer id) throws BusinessCheckException;

    /**
     * 获取分组已发放套数
     *
     * @param id 分组ID
     * @return 已发放套数
     * @throws BusinessCheckException 业务异常
     */
    Integer getSendNum(Integer id) throws BusinessCheckException;
}
