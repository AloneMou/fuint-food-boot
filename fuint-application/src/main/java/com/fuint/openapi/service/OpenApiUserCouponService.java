package com.fuint.openapi.service;

import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pojo.PageResult;
import com.fuint.openapi.v1.member.coupon.vo.UserCouponPageReqVO;
import com.fuint.openapi.v1.member.coupon.vo.UserCouponRespVO;

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
     * @param pageReqVO 分页查询参数
     * @return 用户优惠券分页响应
     * @throws BusinessCheckException 业务异常
     */
    PageResult<UserCouponRespVO> queryUserCouponPage(UserCouponPageReqVO pageReqVO) throws BusinessCheckException;

    /**
     * 根据ID获取用户优惠券详情
     *
     * @param id 用户优惠券ID
     * @return 用户优惠券详情
     * @throws BusinessCheckException 业务异常
     */
    UserCouponRespVO getUserCouponDetail(Integer id) throws BusinessCheckException;
}
