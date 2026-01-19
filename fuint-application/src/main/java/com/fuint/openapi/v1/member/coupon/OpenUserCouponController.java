package com.fuint.openapi.v1.member.coupon;

import cn.iocoder.yudao.framework.ratelimiter.core.annotation.RateLimiter;
import cn.iocoder.yudao.framework.ratelimiter.core.keyresolver.impl.ClientIpRateLimiterKeyResolver;
import cn.iocoder.yudao.framework.signature.core.annotation.ApiSignature;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pojo.CommonResult;
import com.fuint.framework.pojo.PageResult;
import com.fuint.framework.web.BaseController;
import com.fuint.openapi.service.OpenApiUserCouponService;
import com.fuint.openapi.v1.member.coupon.vo.UserCouponPageReqVO;
import com.fuint.openapi.v1.member.coupon.vo.UserCouponRespVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

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
    @ApiOperation(value = "分页查询用户优惠券列表", notes = "支持按用户ID、状态、优惠券类型等条件分页查询，使用MyBatis Plus优化性能")
    @ApiSignature
    @RateLimiter(time = 60, count = 200, keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<PageResult<UserCouponRespVO>> pageUserCoupons(@Valid UserCouponPageReqVO pageReqVO) {
        try {
            // 调用Service层处理业务逻辑（使用MyBatis Plus分页，批量查询关联数据）
            PageResult<UserCouponRespVO> pageResult = openApiUserCouponService.queryUserCouponPage(pageReqVO);
            return CommonResult.success(pageResult);
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
