package com.fuint.openapi.v1.member.coupon;

import cn.iocoder.yudao.framework.ratelimiter.core.annotation.RateLimiter;
import cn.iocoder.yudao.framework.ratelimiter.core.keyresolver.impl.ClientIpRateLimiterKeyResolver;
import cn.iocoder.yudao.framework.signature.core.annotation.ApiSignature;
import com.fuint.common.Constants;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pojo.CommonResult;
import com.fuint.framework.web.BaseController;
import com.fuint.openapi.service.OpenApiUserCouponService;
import com.fuint.openapi.v1.member.coupon.vo.UserCouponPageReqVO;
import com.fuint.openapi.v1.member.coupon.vo.UserCouponPageRespVO;
import com.fuint.openapi.v1.member.coupon.vo.UserCouponRespVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * OpenApi-会员优惠券相关接口
 * <p>
 * Controller层只负责参数校验和调用Service，业务逻辑在Service层实现
 *
 * @author mjw
 * @since 2026/1/18 22:32
 */
@Validated
@Api(tags = "OpenApi-会员优惠券相关接口")
@RestController
@RequestMapping(value = "/api/v1/member/coupon")
public class OpenUserCouponController extends BaseController {

    @Resource
    private OpenApiUserCouponService openApiUserCouponService;

    /**
     * 分页查询用户优惠券列表
     *
     * @param pageReqVO 分页查询参数
     * @return 用户优惠券分页列表
     */
    @GetMapping("/page")
    @ApiOperation(value = "分页查询用户优惠券列表", notes = "支持按用户ID、状态等条件分页查询")
    @ApiSignature
    @RateLimiter(time = 60, count = 200, keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<UserCouponPageRespVO> pageUserCoupons(@Valid UserCouponPageReqVO pageReqVO) {
        try {
            // 构建分页请求
            PaginationRequest paginationRequest = new PaginationRequest();
            paginationRequest.setCurrentPage(pageReqVO.getPageNo() == null ? Constants.PAGE_NUMBER : pageReqVO.getPageNo());
            paginationRequest.setPageSize(pageReqVO.getPageSize() == null ? Constants.PAGE_SIZE : pageReqVO.getPageSize());

            // 构建查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("userId", pageReqVO.getUserId().toString());
            if (StringUtils.isNotEmpty(pageReqVO.getStatus())) {
                params.put("status", pageReqVO.getStatus());
            }
            paginationRequest.setSearchParams(params);

            // 调用Service层处理业务逻辑（包含性能优化）
            UserCouponPageRespVO pageRespVO = openApiUserCouponService.queryUserCouponPage(paginationRequest);

            return CommonResult.success(pageRespVO);
        } catch (BusinessCheckException e) {
            return CommonResult.error(500, e.getMessage());
        }
    }

    /**
     * 获取用户优惠券详情
     *
     * @param id 用户优惠券ID
     * @return 用户优惠券详情
     */
    @GetMapping("/get/{id}")
    @ApiOperation(value = "获取用户优惠券详情", notes = "根据ID获取用户优惠券详细信息")
    @ApiSignature
    @RateLimiter(time = 60, count = 200, keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<UserCouponRespVO> getUserCoupon(
            @ApiParam(value = "用户优惠券ID", required = true, example = "1")
            @PathVariable("id") Integer id) {
        try {
            // 调用Service层处理业务逻辑
            UserCouponRespVO respVO = openApiUserCouponService.getUserCouponDetail(id);
            return CommonResult.success(respVO);
        } catch (BusinessCheckException e) {
            return CommonResult.error(404, e.getMessage());
        }
    }
}
