package com.fuint.openapi.service;

import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.openapi.v1.member.coupon.vo.UserCouponPageRespVO;
import com.fuint.openapi.v1.member.coupon.vo.UserCouponRespVO;
import com.fuint.repository.model.MtUserCoupon;

/**
 * OpenAPI用户优惠券服务接口
 *
 * @author mjw
 * @since 2026/1/18
 */
public interface OpenApiUserCouponService {

    /**
     * 分页查询用户优惠券列表
     *
     * @param paginationRequest 分页请求参数
     * @return 用户优惠券分页响应
     * @throws BusinessCheckException 业务异常
     */
    UserCouponPageRespVO queryUserCouponPage(PaginationRequest paginationRequest) throws BusinessCheckException;

    /**
     * 根据ID获取用户优惠券详情
     *
     * @param id 用户优惠券ID
     * @return 用户优惠券详情
     * @throws BusinessCheckException 业务异常
     */
    UserCouponRespVO getUserCouponDetail(Integer id) throws BusinessCheckException;
}
