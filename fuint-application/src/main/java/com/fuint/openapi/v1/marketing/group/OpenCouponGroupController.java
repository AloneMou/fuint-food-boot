package com.fuint.openapi.v1.marketing.group;

import cn.iocoder.yudao.framework.ratelimiter.core.annotation.RateLimiter;
import cn.iocoder.yudao.framework.ratelimiter.core.keyresolver.impl.ClientIpRateLimiterKeyResolver;
import cn.iocoder.yudao.framework.signature.core.annotation.ApiSignature;
import com.fuint.common.dto.ReqCouponGroupDto;
import com.fuint.common.enums.StatusEnum;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pojo.CommonResult;
import com.fuint.framework.pojo.PageResult;
import com.fuint.framework.web.BaseController;
import com.fuint.openapi.service.OpenApiCouponGroupService;
import com.fuint.openapi.v1.marketing.group.vo.CouponGroupCreateReqVO;
import com.fuint.openapi.v1.marketing.group.vo.CouponGroupPageReqVO;
import com.fuint.openapi.v1.marketing.group.vo.CouponGroupRespVO;
import com.fuint.openapi.v1.marketing.group.vo.CouponGroupUpdateReqVO;
import com.fuint.repository.model.MtCouponGroup;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

import static com.fuint.openapi.enums.CouponGroupErrorCodeConstants.COUPON_GROUP_CREATE_FAILED;

/**
 * OpenApi优惠券分组管理接口
 * <p>
 * 参考若依框架规范实现
 *
 * @author mjw
 * @since 2026/1/17 13:50
 */
@Validated
@Api(tags = "OpenApi-优惠券分组")
@RestController
@RequestMapping(value = "/api/v1/coupon/group")
public class OpenCouponGroupController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(OpenCouponGroupController.class);

    @Resource
    private OpenApiCouponGroupService openApiCouponGroupService;

    @ApiOperation(value = "创建优惠券分组", notes = "创建新的优惠券分组")
    @PostMapping("/create")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<Integer> createCouponGroup(@Valid @RequestBody CouponGroupCreateReqVO reqVO) throws BusinessCheckException {
        ReqCouponGroupDto dto = new ReqCouponGroupDto();
        BeanUtils.copyProperties(reqVO, dto);
        dto.setStatus(StatusEnum.ENABLED.getKey());
        dto.setOperator("openapi");
        MtCouponGroup couponGroup = openApiCouponGroupService.createCouponGroup(dto);
        if (couponGroup == null) {
            return CommonResult.error(COUPON_GROUP_CREATE_FAILED);
        }
        return CommonResult.success(couponGroup.getId());
    }

    /**
     * 更新优惠券分组
     *
     * @param reqVO 更新请求参数
     * @return 更新结果
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "更新优惠券分组", notes = "更新已存在的优惠券分组信息")
    @PutMapping("/update")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<Boolean> updateCouponGroup(@Valid @RequestBody CouponGroupUpdateReqVO reqVO) throws BusinessCheckException {
        ReqCouponGroupDto dto = new ReqCouponGroupDto();
        BeanUtils.copyProperties(reqVO, dto);
        dto.setOperator("openapi");
        openApiCouponGroupService.updateCouponGroup(dto);
        return CommonResult.success(true);
    }

    /**
     * 删除优惠券分组
     *
     * @param id       分组ID
     * @param operator 操作人
     * @return 删除结果
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "删除优惠券分组", notes = "根据ID删除优惠券分组")
    @DeleteMapping("/delete/{id}")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<Boolean> deleteCouponGroup(
            @ApiParam(value = "分组ID", required = true, example = "1") @PathVariable("id") Integer id,
            @ApiParam(value = "操作人", required = false, example = "openapi") @RequestParam(value = "operator", required = false, defaultValue = "openapi") String operator) throws BusinessCheckException {
        // 调用OpenAPI服务层删除（内部会校验ID和分组是否存在）
        openApiCouponGroupService.deleteCouponGroup(id, operator);
        return CommonResult.success(true);
    }

    /**
     * 获取优惠券分组详情
     *
     * @param id 分组ID
     * @return 分组详情
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "获取优惠券分组详情", notes = "根据ID获取优惠券分组详细信息")
    @GetMapping("/detail/{id}")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<CouponGroupRespVO> getCouponGroupDetail(
            @ApiParam(value = "分组ID", required = true, example = "1") @PathVariable("id") Integer id) throws BusinessCheckException {
        // 查询分组信息（内部会校验ID和分组是否存在）
        MtCouponGroup couponGroup = openApiCouponGroupService.queryCouponGroupById(id);
        // 构建响应VO
        CouponGroupRespVO respVO = buildCouponGroupRespVO(couponGroup);
        // 补充额外统计信息
        Integer couponNum = openApiCouponGroupService.getCouponNum(id);
        respVO.setCouponNum(couponNum);
        return CommonResult.success(respVO);
    }

    /**
     * 分页查询优惠券分组列表
     *
     * @param reqVO 查询请求参数
     * @return 分页列表
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "分页查询优惠券分组列表", notes = "支持按名称、商户ID、状态等条件分页查询")
    @GetMapping("/page")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<PageResult<CouponGroupRespVO>> getCouponGroupList(@Valid CouponGroupPageReqVO reqVO) throws BusinessCheckException {
        PageResult<MtCouponGroup> pageResult = openApiCouponGroupService.getCouponGroupPage(reqVO);
        // 转换为响应VO
        List<CouponGroupRespVO> respList = new ArrayList<>();
        for (MtCouponGroup group : pageResult.getList()) {
            CouponGroupRespVO respVO = buildCouponGroupRespVO(group);
            // 补充统计信息
            try {
                Integer couponNum = openApiCouponGroupService.getCouponNum(group.getId());
                respVO.setCouponNum(couponNum);
            } catch (Exception e) {
                log.warn("[getCouponGroupList] 获取分组统计信息失败，分组ID: {}", group.getId(), e);
            }
            respList.add(respVO);
        }
        PageResult<CouponGroupRespVO> result = new PageResult<>();
        result.setTotal(pageResult.getTotal());
        result.setTotalPages(pageResult.getTotalPages());
        result.setCurrentPage(pageResult.getCurrentPage());
        result.setPageSize(pageResult.getPageSize());
        result.setList(respList);
        return CommonResult.success(result);
    }

    /**
     * 构建优惠券分组响应VO
     *
     * @param group 优惠券分组实体
     * @return 响应VO
     */
    private CouponGroupRespVO buildCouponGroupRespVO(MtCouponGroup group) {
        CouponGroupRespVO respVO = new CouponGroupRespVO();
        BeanUtils.copyProperties(group, respVO);
        return respVO;
    }
}
