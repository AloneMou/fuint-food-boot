package com.fuint.openapi.service;

import com.fuint.common.dto.ReqCouponDto;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.pojo.PageResult;
import com.fuint.openapi.v1.marketing.coupon.vo.MtCouponPageReqVO;
import com.fuint.repository.model.MtCoupon;

import java.util.List;

/**
 * OpenAPI优惠券业务接口
 * <p>
 * 独立的OpenAPI优惠券服务接口,避免与后台业务冲突
 *
 * @author mjw
 * @since 2026/1/17
 */
public interface OpenApiCouponService {

    /**
     * 分页查询优惠券列表
     *
     * @param reqVO 分页请求参数
     * @return 分页结果
     */
    PageResult<MtCoupon> queryCouponPage(MtCouponPageReqVO reqVO);

    /**
     * 分页查询优惠券列表 (Old)
     *
     * @param paginationRequest 分页请求参数
     * @return 分页结果
     * @throws BusinessCheckException 业务异常
     */
    PaginationResponse<MtCoupon> queryCouponListByPagination(PaginationRequest paginationRequest) throws BusinessCheckException;

    /**
     * 创建优惠券
     *
     * @param reqCouponDto 优惠券信息
     * @return 创建的优惠券
     * @throws BusinessCheckException 业务异常
     */
    MtCoupon createCoupon(ReqCouponDto reqCouponDto);

    /**
     * 更新优惠券
     *
     * @param reqCouponDto 优惠券信息
     * @return 更新后的优惠券
     * @throws BusinessCheckException 业务异常
     */
    void updateCoupon(ReqCouponDto reqCouponDto);

    /**
     * 根据ID获取优惠券信息
     *
     * @param id 优惠券ID
     * @return 优惠券信息
     * @throws BusinessCheckException 业务异常
     */
    MtCoupon queryCouponById(Integer id) throws BusinessCheckException;

    /**
     * 删除优惠券
     *
     * @param id       优惠券ID
     * @param operator 操作人
     * @throws BusinessCheckException 业务异常
     */
    void deleteCoupon(Integer id, String operator);

    /**
     * 批量发放优惠券
     *
     * @param couponId 优惠券ID
     * @param userIds  用户ID列表
     * @param num      每个用户发放数量
     * @param uuid     批次号
     * @param operator 操作人
     * @throws BusinessCheckException 业务异常
     */
    void batchSendCoupon(Integer couponId, List<Integer> userIds, Integer num, String uuid, String operator) throws BusinessCheckException;

    /**
     * 撤销已发放的优惠券
     *
     * @param couponId 优惠券ID
     * @param uuid     批次号
     * @param operator 操作人
     * @throws BusinessCheckException 业务异常
     */
    void revokeCoupon(Integer couponId, String uuid, String operator) throws BusinessCheckException;

    /**
     * 获取已发放数量
     *
     * @param couponId 优惠券ID
     * @return 已发放数量
     */
    Integer getSendNum(Integer couponId);

    /**
     * 获取剩余数量
     *
     * @param couponId 优惠券ID
     * @return 剩余数量
     */
    Integer getLeftNum(Integer couponId);

    /**
     * 获取优惠券关联的商品列表
     *
     * @param couponId 优惠券ID
     * @return 商品ID列表
     */
    List<Integer> getCouponGoodsIds(Integer couponId);
}
