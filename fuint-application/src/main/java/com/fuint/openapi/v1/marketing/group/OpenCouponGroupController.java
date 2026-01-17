package com.fuint.openapi.v1.marketing.group;

import cn.iocoder.yudao.framework.ratelimiter.core.annotation.RateLimiter;
import cn.iocoder.yudao.framework.ratelimiter.core.keyresolver.impl.ClientIpRateLimiterKeyResolver;
import cn.iocoder.yudao.framework.signature.core.annotation.ApiSignature;
import com.fuint.common.dto.ReqCouponGroupDto;
import com.fuint.common.enums.StatusEnum;
import com.fuint.openapi.service.OpenApiCouponGroupService;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.pojo.CommonResult;
import com.fuint.framework.web.BaseController;
import com.fuint.openapi.v1.marketing.group.vo.CouponGroupCreateReqVO;
import com.fuint.openapi.v1.marketing.group.vo.CouponGroupPageReqVO;
import com.fuint.openapi.v1.marketing.group.vo.CouponGroupRespVO;
import com.fuint.openapi.v1.marketing.group.vo.CouponGroupUpdateReqVO;
import com.fuint.repository.model.MtCouponGroup;
import com.fuint.utils.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * 创建优惠券分组
     *
     * @param reqVO 创建请求参数
     * @return 创建结果
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "创建优惠券分组", notes = "创建新的优惠券分组")
    @PostMapping("/create")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<CouponGroupRespVO> createCouponGroup(@Valid @RequestBody CouponGroupCreateReqVO reqVO) throws BusinessCheckException {
        log.info("[createCouponGroup] 创建优惠券分组，请求参数: {}", reqVO);

        // 参数校验
        if (StringUtil.isEmpty(reqVO.getName())) {
            return CommonResult.error(400001, "分组名称不能为空");
        }

        // 转换DTO
        ReqCouponGroupDto dto = new ReqCouponGroupDto();
        BeanUtils.copyProperties(reqVO, dto);
        dto.setStatus(StatusEnum.ENABLED.getKey());
        dto.setOperator(reqVO.getOperator() != null ? reqVO.getOperator() : "system");

        // 调用OpenAPI服务层创建
        MtCouponGroup couponGroup = openApiCouponGroupService.createCouponGroup(dto);
        if (couponGroup == null) {
            return CommonResult.error(500000, "创建优惠券分组失败");
        }

        // 构建响应VO
        CouponGroupRespVO respVO = buildCouponGroupRespVO(couponGroup);
        log.info("[createCouponGroup] 创建优惠券分组成功，分组ID: {}", couponGroup.getId());

        return CommonResult.success(respVO);
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
    public CommonResult<CouponGroupRespVO> updateCouponGroup(@Valid @RequestBody CouponGroupUpdateReqVO reqVO) throws BusinessCheckException {
        log.info("[updateCouponGroup] 更新优惠券分组，请求参数: {}", reqVO);

        // 参数校验
        if (reqVO.getId() == null || reqVO.getId() <= 0) {
            return CommonResult.error(400001, "分组ID不能为空");
        }

        // 检查分组是否存在
        MtCouponGroup existGroup = openApiCouponGroupService.queryCouponGroupById(reqVO.getId());
        if (existGroup == null) {
            return CommonResult.error(404001, "优惠券分组不存在");
        }

        // 转换DTO
        ReqCouponGroupDto dto = new ReqCouponGroupDto();
        BeanUtils.copyProperties(reqVO, dto);
        dto.setOperator(reqVO.getOperator() != null ? reqVO.getOperator() : "system");

        // 调用OpenAPI服务层更新
        MtCouponGroup couponGroup = openApiCouponGroupService.updateCouponGroup(dto);
        if (couponGroup == null) {
            return CommonResult.error(500000, "更新优惠券分组失败");
        }

        // 构建响应VO
        CouponGroupRespVO respVO = buildCouponGroupRespVO(couponGroup);
        log.info("[updateCouponGroup] 更新优惠券分组成功，分组ID: {}", couponGroup.getId());

        return CommonResult.success(respVO);
    }

    /**
     * 删除优惠券分组
     *
     * @param id 分组ID
     * @return 删除结果
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "删除优惠券分组", notes = "根据ID删除优惠券分组")
    @DeleteMapping("/delete/{id}")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<Boolean> deleteCouponGroup(
            @ApiParam(value = "分组ID", required = true) @PathVariable("id") Integer id,
            @ApiParam(value = "操作人", required = false) @RequestParam(value = "operator", required = false, defaultValue = "system") String operator) throws BusinessCheckException {
        log.info("[deleteCouponGroup] 删除优惠券分组，分组ID: {}, 操作人: {}", id, operator);

        // 参数校验
        if (id == null || id <= 0) {
            return CommonResult.error(400001, "分组ID不能为空");
        }

        // 检查分组是否存在
        MtCouponGroup existGroup = openApiCouponGroupService.queryCouponGroupById(id);
        if (existGroup == null) {
            return CommonResult.error(404001, "优惠券分组不存在");
        }

        // 调用OpenAPI服务层删除
        openApiCouponGroupService.deleteCouponGroup(id, operator);
        log.info("[deleteCouponGroup] 删除优惠券分组成功，分组ID: {}", id);

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
            @ApiParam(value = "分组ID", required = true) @PathVariable("id") Integer id) throws BusinessCheckException {
        log.info("[getCouponGroupDetail] 获取优惠券分组详情，分组ID: {}", id);

        // 参数校验
        if (id == null || id <= 0) {
            return CommonResult.error(400001, "分组ID不能为空");
        }

        // 查询分组信息
        MtCouponGroup couponGroup = openApiCouponGroupService.queryCouponGroupById(id);
        if (couponGroup == null) {
            return CommonResult.error(404001, "优惠券分组不存在");
        }

        // 构建响应VO
        CouponGroupRespVO respVO = buildCouponGroupRespVO(couponGroup);

        // 补充额外统计信息
        Integer couponNum = openApiCouponGroupService.getCouponNum(id);
        Integer sendNum = openApiCouponGroupService.getSendNum(id);
        respVO.setCouponNum(couponNum);
        respVO.setSendNum(sendNum);

        log.info("[getCouponGroupDetail] 获取优惠券分组详情成功，分组ID: {}", id);
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
    @GetMapping("/list")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<PaginationResponse<CouponGroupRespVO>> getCouponGroupList(@Valid CouponGroupPageReqVO reqVO) throws BusinessCheckException {
        log.info("[getCouponGroupList] 分页查询优惠券分组列表，请求参数: {}", reqVO);

        // 设置默认值
        Integer pageNo = reqVO.getPageNo() != null ? reqVO.getPageNo() : 1;
        Integer pageSize = reqVO.getPageSize() != null ? reqVO.getPageSize() : 20;
        if (pageSize > 100) {
            pageSize = 100; // 限制最大每页100条
        }

        // 构建分页请求
        PaginationRequest paginationRequest = new PaginationRequest();
        paginationRequest.setCurrentPage(pageNo);
        paginationRequest.setPageSize(pageSize);

        // 构建查询参数
        Map<String, Object> params = new HashMap<>();
        if (reqVO.getMerchantId() != null && reqVO.getMerchantId() > 0) {
            params.put("merchantId", reqVO.getMerchantId());
        }
        if (reqVO.getStoreId() != null && reqVO.getStoreId() > 0) {
            params.put("storeId", reqVO.getStoreId());
        }
        if (StringUtil.isNotEmpty(reqVO.getName())) {
            params.put("name", reqVO.getName());
        }
        if (StringUtil.isNotEmpty(reqVO.getStatus())) {
            params.put("status", reqVO.getStatus());
        }
        paginationRequest.setSearchParams(params);

        // 查询分页数据
        PaginationResponse<MtCouponGroup> paginationResponse = openApiCouponGroupService.queryCouponGroupListByPagination(paginationRequest);

        // 转换为响应VO
        List<CouponGroupRespVO> respList = new ArrayList<>();
        if (paginationResponse.getContent() != null && !paginationResponse.getContent().isEmpty()) {
            for (MtCouponGroup group : paginationResponse.getContent()) {
                CouponGroupRespVO respVO = buildCouponGroupRespVO(group);
                // 补充统计信息
                try {
                    Integer couponNum = openApiCouponGroupService.getCouponNum(group.getId());
                    Integer sendNum = openApiCouponGroupService.getSendNum(group.getId());
                    respVO.setCouponNum(couponNum);
                    respVO.setSendNum(sendNum);
                } catch (Exception e) {
                    log.warn("[getCouponGroupList] 获取分组统计信息失败，分组ID: {}", group.getId(), e);
                }
                respList.add(respVO);
            }
        }

        // 构建返回结果
        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize);
        PageImpl<CouponGroupRespVO> pageImpl = new PageImpl<>(respList, pageRequest, paginationResponse.getTotalElements());
        PaginationResponse<CouponGroupRespVO> result = new PaginationResponse<>(pageImpl, CouponGroupRespVO.class);
        result.setContent(respList);
        result.setTotalElements(paginationResponse.getTotalElements());
        result.setTotalPages(paginationResponse.getTotalPages());
        result.setCurrentPage(pageNo);
        result.setPageSize(pageSize);

        log.info("[getCouponGroupList] 查询成功，总记录数: {}", paginationResponse.getTotalElements());
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
