package com.fuint.openapi.v1.marketing.coupon;

import cn.iocoder.yudao.framework.ratelimiter.core.annotation.RateLimiter;
import cn.iocoder.yudao.framework.ratelimiter.core.keyresolver.impl.ClientIpRateLimiterKeyResolver;
import cn.iocoder.yudao.framework.signature.core.annotation.ApiSignature;
import com.fuint.common.Constants;
import com.fuint.common.dto.ReqCouponDto;
import com.fuint.common.enums.StatusEnum;
import com.fuint.openapi.service.OpenApiCouponService;
import com.fuint.common.service.*;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.pojo.CommonResult;
import com.fuint.framework.web.BaseController;
import com.fuint.openapi.v1.marketing.coupon.vo.*;
import com.fuint.repository.mapper.MtCouponGoodsMapper;
import com.fuint.repository.mapper.MtUserCouponMapper;
import com.fuint.repository.model.*;
import com.fuint.utils.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * OpenApi-优惠券管理相关接口
 *
 * @author mjw
 * @since 2026/1/17
 */
@Validated
@Api(tags = "OpenApi-优惠券管理")
@RestController
@RequestMapping(value = "/api/v1/coupon")
public class OpenCouponController extends BaseController {

    @Resource
    private OpenApiCouponService openApiCouponService;

    @Resource
    private CouponGroupService couponGroupService;

    @Resource
    private MemberService memberService;

    @Resource
    private MemberGroupService memberGroupService;

    @Resource
    private GoodsService goodsService;

    @Resource
    private MtCouponGoodsMapper mtCouponGoodsMapper;

    /**
     * 创建优惠券
     *
     * @param createReqVO 创建请求参数
     * @return 创建结果
     */
    @PostMapping("/create")
    @ApiOperation(value = "创建优惠券", notes = "创建优惠券，支持配置多商品、数量、固定金额、费率、最大优惠额")
    @ApiSignature
    @RateLimiter(time = 60, count = 100, keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<MtCouponRespVO> createCoupon(@Valid @RequestBody MtCouponCreateReqVO createReqVO) {
        try {
            // 转换VO到DTO
            ReqCouponDto reqCouponDto = convertToDto(createReqVO);

            // 调用OpenAPI service创建优惠券
            MtCoupon coupon = openApiCouponService.createCoupon(reqCouponDto);

            // 转换返回结果
            MtCouponRespVO respVO = convertToRespVO(coupon);

            return CommonResult.success(respVO);
        } catch (BusinessCheckException | ParseException e) {
            return CommonResult.error(500, e.getMessage());
        }
    }

    /**
     * 更新优惠券
     *
     * @param updateReqVO 更新请求参数
     * @return 更新结果
     */
    @PutMapping("/update")
    @ApiOperation(value = "更新优惠券", notes = "更新优惠券信息")
    @ApiSignature
    @RateLimiter(time = 60, count = 100, keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<MtCouponRespVO> updateCoupon(@Valid @RequestBody MtCouponUpdateReqVO updateReqVO) {
        try {
            // 检查优惠券是否存在
            MtCoupon existCoupon = openApiCouponService.queryCouponById(updateReqVO.getId());
            if (existCoupon == null) {
                return CommonResult.error(404, "优惠券不存在");
            }

            // 转换VO到DTO
            ReqCouponDto reqCouponDto = convertToDto(updateReqVO);
            reqCouponDto.setId(updateReqVO.getId());

            // 调用OpenAPI service更新优惠券
            MtCoupon coupon = openApiCouponService.updateCoupon(reqCouponDto);

            // 转换返回结果
            MtCouponRespVO respVO = convertToRespVO(coupon);

            return CommonResult.success(respVO);
        } catch (BusinessCheckException | ParseException e) {
            return CommonResult.error(500, e.getMessage());
        }
    }

    /**
     * 分页查询优惠券列表
     *
     * @param pageReqVO 分页查询参数
     * @return 分页结果
     */
    @GetMapping("/page")
    @ApiOperation(value = "分页查询优惠券列表", notes = "支持按名称、类型、状态等条件查询")
    @ApiSignature
    @RateLimiter(time = 60, count = 200, keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<MtCouponPageRespVO> pageCoupons(@Valid @ModelAttribute MtCouponPageReqVO pageReqVO) {
        try {
            // 构建分页请求
            PaginationRequest paginationRequest = new PaginationRequest();
            paginationRequest.setCurrentPage(pageReqVO.getPage() == null ? Constants.PAGE_NUMBER : pageReqVO.getPage());
            paginationRequest.setPageSize(pageReqVO.getPageSize() == null ? Constants.PAGE_SIZE : pageReqVO.getPageSize());

            // 构建查询参数
            Map<String, Object> params = new HashMap<>();
            if (pageReqVO.getId() != null) {
                params.put("id", pageReqVO.getId().toString());
            }
            if (pageReqVO.getGroupId() != null) {
                params.put("groupId", pageReqVO.getGroupId().toString());
            }
            if (pageReqVO.getMerchantId() != null) {
                params.put("merchantId", pageReqVO.getMerchantId());
            }
            if (pageReqVO.getStoreId() != null) {
                params.put("storeId", pageReqVO.getStoreId());
            }
            if (StringUtil.isNotEmpty(pageReqVO.getName())) {
                params.put("name", pageReqVO.getName());
            }
            if (StringUtil.isNotEmpty(pageReqVO.getType())) {
                params.put("type", pageReqVO.getType());
            }
            if (StringUtil.isNotEmpty(pageReqVO.getStatus())) {
                params.put("status", pageReqVO.getStatus());
            } else {
                params.put("status", StatusEnum.ENABLED.getKey());
            }

            paginationRequest.setSearchParams(params);

            // 查询分页数据
            PaginationResponse<MtCoupon> paginationResponse = openApiCouponService.queryCouponListByPagination(paginationRequest);

            // 转换为响应VO
            MtCouponPageRespVO pageRespVO = new MtCouponPageRespVO();
            pageRespVO.setTotalElements(paginationResponse.getTotalElements());
            pageRespVO.setTotalPages(paginationResponse.getTotalPages());
            pageRespVO.setCurrentPage(paginationRequest.getCurrentPage());
            pageRespVO.setPageSize(paginationRequest.getPageSize());

            List<MtCouponRespVO> respVOList = paginationResponse.getContent().stream()
                    .map(this::convertToRespVO)
                    .collect(Collectors.toList());
            pageRespVO.setContent(respVOList);

            return CommonResult.success(pageRespVO);
        } catch (BusinessCheckException e) {
            return CommonResult.error(500, e.getMessage());
        }
    }

    /**
     * 获取优惠券详情
     *
     * @param id 优惠券ID
     * @return 优惠券详情
     */
    @GetMapping("/get/{id}")
    @ApiOperation(value = "获取优惠券详情", notes = "根据ID获取优惠券详细信息")
    @ApiSignature
    @RateLimiter(time = 60, count = 200, keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<MtCouponRespVO> getCoupon(@PathVariable("id") Integer id) {
        try {
            MtCoupon coupon = openApiCouponService.queryCouponById(id);
            if (coupon == null) {
                return CommonResult.error(404, "优惠券不存在");
            }

            MtCouponRespVO respVO = convertToRespVO(coupon);
            return CommonResult.success(respVO);
        } catch (BusinessCheckException e) {
            return CommonResult.error(500, e.getMessage());
        }
    }

    /**
     * 发放优惠券
     *
     * @param sendReqVO 发券请求参数
     * @return 发券结果（包含批次号）
     */
    @PostMapping("/send")
    @ApiOperation(value = "发放优惠券", notes = "支持单个用户、批量用户、会员分组发券")
    @ApiSignature
    @RateLimiter(time = 60, count = 50, keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<Map<String, Object>> sendCoupon(@Valid @RequestBody CouponSendReqVO sendReqVO) {
        try {
            // 检查优惠券是否存在
            MtCoupon coupon = openApiCouponService.queryCouponById(sendReqVO.getCouponId());
            if (coupon == null) {
                return CommonResult.error(404, "优惠券不存在");
            }

            // 生成批次号
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            List<Integer> userIdList = new ArrayList<>();

            // 根据发放对象类型获取用户列表
            String sendObject = sendReqVO.getSendObject();
            if ("single".equals(sendObject)) {
                // 单个用户
                if (sendReqVO.getUserId() != null) {
                    userIdList.add(sendReqVO.getUserId());
                } else if (StringUtil.isNotEmpty(sendReqVO.getMobile())) {
                    MtUser user = memberService.queryMemberByMobile(coupon.getMerchantId(), sendReqVO.getMobile());
                    if (user != null) {
                        userIdList.add(user.getId());
                    } else {
                        return CommonResult.error(404, "用户不存在");
                    }
                } else {
                    return CommonResult.error(400, "请提供用户ID或手机号");
                }
            } else if ("batch".equals(sendObject)) {
                // 批量用户
                if (sendReqVO.getUserIds() == null || sendReqVO.getUserIds().isEmpty()) {
                    return CommonResult.error(400, "用户ID列表不能为空");
                }
                userIdList.addAll(sendReqVO.getUserIds());
            } else if ("group".equals(sendObject)) {
                // 会员分组
                if (sendReqVO.getGroupId() == null) {
                    return CommonResult.error(400, "会员分组ID不能为空");
                }
                // 获取分组下所有用户
                userIdList = memberService.getUserIdList(coupon.getMerchantId(), null);
                // 过滤分组用户
                userIdList = userIdList.stream()
                        .filter(userId -> {
                            try {
                                MtUser user = memberService.queryMemberById(userId);
                                return user != null && sendReqVO.getGroupId().equals(user.getGroupId());
                            } catch (Exception e) {
                                return false;
                            }
                        })
                        .collect(Collectors.toList());
            } else {
                return CommonResult.error(400, "不支持的发放对象类型");
            }

            if (userIdList.isEmpty()) {
                return CommonResult.error(400, "没有找到符合条件的用户");
            }

            // 批量发券
            Boolean sendMessage = sendReqVO.getSendMessage() != null && sendReqVO.getSendMessage();
            String operator = StringUtil.isNotEmpty(sendReqVO.getOperator()) ? sendReqVO.getOperator() : "system";
            openApiCouponService.batchSendCoupon(sendReqVO.getCouponId(), userIdList, sendReqVO.getNum(), uuid, operator);

            Map<String, Object> result = new HashMap<>();
            result.put("uuid", uuid);
            result.put("userCount", userIdList.size());
            result.put("totalCouponCount", userIdList.size() * sendReqVO.getNum());
            result.put("message", "发券成功");

            return CommonResult.success(result);
        } catch (BusinessCheckException e) {
            return CommonResult.error(500, e.getMessage());
        }
    }

    /**
     * 撤销优惠券
     *
     * @param revokeReqVO 撤销请求参数
     * @return 撤销结果
     */
    @PostMapping("/revoke")
    @ApiOperation(value = "撤销优惠券", notes = "根据批次号撤销已发放的优惠券")
    @ApiSignature
    @RateLimiter(time = 60, count = 50, keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<Map<String, Object>> revokeCoupon(@Valid @RequestBody CouponRevokeReqVO revokeReqVO) {
        try {
            // 检查优惠券是否存在
            MtCoupon coupon = openApiCouponService.queryCouponById(revokeReqVO.getCouponId());
            if (coupon == null) {
                return CommonResult.error(404, "优惠券不存在");
            }

            String operator = StringUtil.isNotEmpty(revokeReqVO.getOperator()) ? revokeReqVO.getOperator() : "system";
            openApiCouponService.revokeCoupon(revokeReqVO.getCouponId(), revokeReqVO.getUuid(), operator);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "撤销成功");
            result.put("uuid", revokeReqVO.getUuid());

            return CommonResult.success(result);
        } catch (BusinessCheckException e) {
            return CommonResult.error(500, e.getMessage());
        }
    }

    /**
     * 转换CreateReqVO到DTO
     */
    private ReqCouponDto convertToDto(MtCouponCreateReqVO createReqVO) {
        ReqCouponDto dto = new ReqCouponDto();
        BeanUtils.copyProperties(createReqVO, dto);

        // 处理商品列表
        if (createReqVO.getGoodsList() != null && !createReqVO.getGoodsList().isEmpty()) {
            String goodsIds = createReqVO.getGoodsList().stream()
                    .map(item -> item.getGoodsId().toString())
                    .collect(Collectors.joining(","));
            dto.setGoodsIds(goodsIds);
        }

        return dto;
    }

    /**
     * 转换MtCoupon到RespVO
     */
    private MtCouponRespVO convertToRespVO(MtCoupon coupon) {
        MtCouponRespVO respVO = new MtCouponRespVO();
        BeanUtils.copyProperties(coupon, respVO);

        // 获取分组名称
        try {
            MtCouponGroup group = couponGroupService.queryCouponGroupById(coupon.getGroupId());
            if (group != null) {
                respVO.setGroupName(group.getName());
            }
        } catch (BusinessCheckException e) {
            // ignore
        }

        // 获取商品列表
        List<MtCouponGoods> couponGoodsList = mtCouponGoodsMapper.getCouponGoods(coupon.getId());
        if (couponGoodsList != null && !couponGoodsList.isEmpty()) {
            List<CouponGoodsItemVO> goodsList = couponGoodsList.stream().map(cg -> {
                CouponGoodsItemVO item = new CouponGoodsItemVO();
                item.setGoodsId(cg.getGoodsId());
                try {
                    MtGoods goods = goodsService.queryGoodsById(cg.getGoodsId());
                    if (goods != null) {
                        item.setGoodsName(goods.getName());
                    }
                } catch (BusinessCheckException e) {
                    // ignore
                }
                return item;
            }).collect(Collectors.toList());
            respVO.setGoodsList(goodsList);
        }

        // 获取已发放数量和剩余数量
        Integer sendNum = openApiCouponService.getSendNum(coupon.getId());
        respVO.setSentNum(sendNum);
        Integer leftNum = openApiCouponService.getLeftNum(coupon.getId());
        respVO.setLeftNum(leftNum);

        return respVO;
    }
}
