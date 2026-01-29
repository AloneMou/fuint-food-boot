package com.fuint.openapi.v1.marketing.coupon;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.ratelimiter.core.annotation.RateLimiter;
import cn.iocoder.yudao.framework.ratelimiter.core.keyresolver.impl.ClientIpRateLimiterKeyResolver;
import cn.iocoder.yudao.framework.signature.core.annotation.ApiSignature;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fuint.common.dto.ReqCouponDto;
import com.fuint.common.enums.CouponExpireTypeEnum;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.enums.UserCouponStatusEnum;
import com.fuint.common.service.MemberService;
import com.fuint.common.service.StoreService;
import com.fuint.common.service.UserGradeService;
import com.fuint.framework.annoation.OperationServiceLog;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pojo.CommonResult;
import com.fuint.framework.pojo.PageResult;
import com.fuint.framework.util.string.StrUtils;
import com.fuint.framework.web.BaseController;
import com.fuint.openapi.service.EventCallbackService;
import com.fuint.openapi.service.OpenApiCouponGroupService;
import com.fuint.openapi.service.OpenApiCouponService;
import com.fuint.openapi.v1.marketing.coupon.vo.*;
import com.fuint.repository.mapper.MtCouponGoodsMapper;
import com.fuint.repository.mapper.MtGoodsMapper;
import com.fuint.repository.mapper.MtUserCouponMapper;
import com.fuint.repository.model.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

import static com.fuint.framework.util.collection.CollectionUtils.convertMap;
import static com.fuint.framework.util.collection.CollectionUtils.convertSet;
import static com.fuint.openapi.enums.CouponErrorCodeConstants.COUPON_NOT_FOUND;
import static com.fuint.openapi.enums.UserErrorCodeConstants.USER_NOT_FOUND;

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
    private OpenApiCouponGroupService openApiCouponGroupService;

    @Resource
    private MemberService memberService;


    @Resource
    private MtCouponGoodsMapper mtCouponGoodsMapper;

    @Resource
    private MtGoodsMapper mtGoodsMapper;

    @Resource
    private StoreService storeService;

    @Resource
    private UserGradeService userGradeService;

    @Resource
    private EventCallbackService eventCallbackService;

    @Resource
    private MtUserCouponMapper mtUserCouponMapper;

    @PostMapping("/create")
    @ApiOperation(value = "创建优惠券", notes = "创建优惠券，支持配置多商品、数量、固定金额、费率、最大优惠额")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    @OperationServiceLog(description = "(OpenApi)创建优惠券")
    public CommonResult<Integer> createCoupon(@Valid @RequestBody MtCouponCreateReqVO createReqVO) {
        ReqCouponDto reqCouponDto = convertToDto(createReqVO);
        MtCoupon coupon = openApiCouponService.createCoupon(reqCouponDto);
        return CommonResult.success(coupon.getId());

    }

    @PutMapping("/update")
    @ApiOperation(value = "更新优惠券", notes = "更新优惠券信息")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    @OperationServiceLog(description = "(OpenApi)更新优惠券")
    public CommonResult<Boolean> updateCoupon(@Valid @RequestBody MtCouponUpdateReqVO updateReqVO) {
        MtCoupon existCoupon = openApiCouponService.queryCouponById(updateReqVO.getId());
        if (existCoupon == null) {
            return CommonResult.error(COUPON_NOT_FOUND);
        }
        if (StatusEnum.DISABLE.getKey().equals(existCoupon.getStatus())) {
            return CommonResult.error(COUPON_NOT_FOUND);
        }
        ReqCouponDto reqCouponDto = convertToDto(updateReqVO);
        reqCouponDto.setId(updateReqVO.getId());
        openApiCouponService.updateCoupon(reqCouponDto);
        return CommonResult.success(true);

    }


    @GetMapping("/page")
    @ApiOperation(value = "分页查询优惠券列表", notes = "支持按名称、类型、状态等条件查询")
    @ApiSignature
    @RateLimiter(time = 60, count = 200, keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<PageResult<MtCouponRespVO>> pageCoupons(@Valid @ModelAttribute MtCouponPageReqVO pageReqVO) {
        // 使用优化后的MyBatis Plus分页查询
        PageResult<MtCoupon> page = openApiCouponService.queryCouponPage(pageReqVO);
        List<MtCouponRespVO> list = page.getList().stream().map(this::convertToRespVO).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(list)) {
            // 批量填充商品信息
            fillGoodsInfo(list);
            // 批量填充店铺信息
            fillStoreInfo(list);
            // 批量填充会员等级信息
            fillGradeInfo(list);
            // 批量填充优惠券分组信息
            fillGroupInfo(list);
        }
        PageResult<MtCouponRespVO> pageResult = new PageResult<>();
        pageResult.setList(list);
        pageResult.setTotal(page.getTotal());
        pageResult.setCurrentPage(page.getCurrentPage());
        pageResult.setPageSize(page.getPageSize());
        pageResult.setTotalPages(page.getTotalPages());
        return CommonResult.success(pageResult);
    }

    /**
     * 批量填充商品信息
     */
    private void fillGoodsInfo(List<MtCouponRespVO> list) {
        Set<Integer> couponIds = list.stream().map(MtCouponRespVO::getId).collect(Collectors.toSet());
        if (couponIds.isEmpty()) return;

        // 1. 批量查询优惠券与商品的关联关系
        LambdaQueryWrapper<MtCouponGoods> wrapper = Wrappers.lambdaQuery();
        wrapper.in(MtCouponGoods::getCouponId, couponIds);
        List<MtCouponGoods> couponGoodsList = mtCouponGoodsMapper.selectList(wrapper);
        if (couponGoodsList.isEmpty()) return;
        // 2. 收集所有商品ID
        Set<Integer> goodsIds = couponGoodsList.stream().map(MtCouponGoods::getGoodsId).collect(Collectors.toSet());
        if (goodsIds.isEmpty()) return;
        // 3. 批量查询商品信息
        List<MtGoods> goodsList = mtGoodsMapper.selectBatchIds(goodsIds);
        Map<Integer, String> goodsNameMap = goodsList.stream()
                .collect(Collectors.toMap(MtGoods::getId, MtGoods::getName, (v1, v2) -> v1));
        // 4. 组装数据
        Map<Integer, List<CouponGoodsItemVO>> couponGoodsMap = new HashMap<>();
        for (MtCouponGoods cg : couponGoodsList) {
            if (goodsNameMap.containsKey(cg.getGoodsId())) {
                CouponGoodsItemVO item = new CouponGoodsItemVO();
                item.setGoodsId(cg.getGoodsId());
                item.setGoodsName(goodsNameMap.get(cg.getGoodsId()));
                couponGoodsMap.computeIfAbsent(cg.getCouponId(), k -> new ArrayList<>()).add(item);
            }
        }
        // 5. 填充到VO
        for (MtCouponRespVO vo : list) {
            if (couponGoodsMap.containsKey(vo.getId())) {
                vo.setGoodsList(couponGoodsMap.get(vo.getId()));
            }
        }
    }

    /**
     * 批量填充门店信息
     *
     * @param list 优惠券列表
     */
    private void fillStoreInfo(List<MtCouponRespVO> list) {
        Set<Integer> storeIds = new HashSet<>();
        for (MtCouponRespVO vo : list) {
            if (vo.getStoreIds() != null) {
                storeIds.addAll(vo.getStoreIds());
            }
        }
        List<MtStore> storeList = storeService.getStoreByIds(storeIds);
        Map<Integer, String> storeNameMap = convertMap(storeList, MtStore::getId, MtStore::getName);
        for (MtCouponRespVO vo : list) {
            if (vo.getStoreIds() != null) {
                List<MtCouponRespVO.CouponStoreItemVO> storeLs = new ArrayList<>();
                for (Integer storeId : vo.getStoreIds()) {
                    MtCouponRespVO.CouponStoreItemVO item = new MtCouponRespVO.CouponStoreItemVO();
                    item.setStoreId(storeId);
                    item.setStoreName(storeNameMap.get(storeId));
                    storeLs.add(item);
                }
                vo.setStoreLs(storeLs);
            }
        }
    }

    /**
     * 批量填充会员等级信息
     *
     * @param list 优惠券列表
     */
    private void fillGradeInfo(List<MtCouponRespVO> list) {
        Set<Integer> gradeIds = new HashSet<>();
        for (MtCouponRespVO vo : list) {
            if (vo.getGradeIds() != null) {
                gradeIds.addAll(vo.getGradeIds());
            }
        }
        List<MtUserGrade> gradeList = userGradeService.getUserGradeListByIds(gradeIds);
        Map<Integer, String> gradeNameMap = convertMap(gradeList, MtUserGrade::getId, MtUserGrade::getName);
        for (MtCouponRespVO vo : list) {
            if (vo.getGradeIds() != null) {
                List<MtCouponRespVO.CouponGradeItemVO> gradeLs = new ArrayList<>();
                for (Integer gradeId : vo.getGradeIds()) {
                    MtCouponRespVO.CouponGradeItemVO item = new MtCouponRespVO.CouponGradeItemVO();
                    item.setGradeId(gradeId);
                    item.setGradeName(gradeNameMap.get(gradeId));
                }
                vo.setGradeLs(gradeLs);
            }
        }
    }

    private void fillGroupInfo(List<MtCouponRespVO> list) {
        Set<Integer> groupIds = convertSet(list, MtCouponRespVO::getGroupId);
        List<MtCouponGroup> groups = openApiCouponGroupService.getCouponGroupListByIds(groupIds);
        Map<Integer, String> groupNameMap = convertMap(groups, MtCouponGroup::getId, MtCouponGroup::getName);
        for (MtCouponRespVO vo : list) {
            vo.setGroupName(groupNameMap.getOrDefault(vo.getGroupId(), ""));
        }
    }

    @GetMapping("/get/{id}")
    @ApiOperation(value = "获取优惠券详情", notes = "根据ID获取优惠券详细信息")
    @ApiSignature
    @RateLimiter(time = 60, count = 200, keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<MtCouponRespVO> getCoupon(@PathVariable("id") Integer id) {
        MtCoupon coupon = openApiCouponService.queryCouponById(id);
        if (coupon == null) {
            return CommonResult.error(COUPON_NOT_FOUND);
        }
        if (coupon.getStatus().equals(StatusEnum.DISABLE.getKey())) {
            return CommonResult.error(COUPON_NOT_FOUND);
        }
        MtCouponRespVO respVO = convertToRespVO(coupon);
        List<MtCouponRespVO> couponLs = Collections.singletonList(respVO);
        fillGoodsInfo(couponLs);
        fillStoreInfo(couponLs);
        fillGradeInfo(couponLs);
        fillGroupInfo(couponLs);
        return CommonResult.success(CollUtil.get(couponLs, 0));
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
    @OperationServiceLog(description = "(OpenApi)发放优惠券")
    public CommonResult<Map<String, Object>> sendCoupon(@Valid @RequestBody CouponSendReqVO sendReqVO) {
        try {
            // 检查优惠券是否存在
            MtCoupon coupon = openApiCouponService.queryCouponById(sendReqVO.getCouponId());
            if (coupon == null) {
                return CommonResult.error(COUPON_NOT_FOUND);
            }
            if (StatusEnum.DISABLE.getKey().equals(coupon.getStatus())) {
                return CommonResult.error(COUPON_NOT_FOUND);
            }

            // 生成批次号
            String uuid = UUID.randomUUID(true).toString();
            List<Integer> userIdList = new ArrayList<>();

            // 根据发放对象类型获取用户列表
            String sendObject = sendReqVO.getSendObject();
            if ("single".equals(sendObject)) {
                // 单个用户
                if (sendReqVO.getUserId() != null) {
                    userIdList.add(sendReqVO.getUserId());
                } else if (StringUtils.isNotEmpty(sendReqVO.getMobile())) {
                    MtUser user = memberService.queryMemberByMobile(coupon.getMerchantId(), sendReqVO.getMobile());
                    if (user != null) {
                        userIdList.add(user.getId());
                    } else {
                        return CommonResult.error(USER_NOT_FOUND);
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
//            Boolean sendMessage = sendReqVO.getSendMessage() != null && sendReqVO.getSendMessage();
            String operator = StringUtils.isNotEmpty(sendReqVO.getOperator()) ? sendReqVO.getOperator() : "system";
            openApiCouponService.batchSendCoupon(sendReqVO.getCouponId(), userIdList, sendReqVO.getNum(), uuid, operator);

            // 发送领取优惠券回调
            LambdaQueryWrapper<MtUserCoupon> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(MtUserCoupon::getUuid, uuid);
            List<MtUserCoupon> userCoupons = mtUserCouponMapper.selectList(queryWrapper);
            if (CollUtil.isNotEmpty(userCoupons)) {
                for (MtUserCoupon userCoupon : userCoupons) {
                    eventCallbackService.sendCouponEventCallback(userCoupon, "RECEIVED", null);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("uuid", uuid);
            result.put("userCount", userIdList.size());
            result.put("totalCouponCount", userIdList.size() * sendReqVO.getNum());
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
    @OperationServiceLog(description = "(OpenApi)撤销优惠券")
    public CommonResult<Boolean> revokeCoupon(@Valid @RequestBody CouponRevokeReqVO revokeReqVO) {
        if (ObjectUtil.isNotNull(revokeReqVO.getUserCouponId())) {
            openApiCouponService.revokeCoupon(revokeReqVO.getUserCouponId());
        } else {
            // 检查优惠券是否存在
            MtCoupon coupon = openApiCouponService.queryCouponById(revokeReqVO.getCouponId());
            if (coupon == null) {
                return CommonResult.error(COUPON_NOT_FOUND);
            }
            if (StatusEnum.DISABLE.getKey().equals(coupon.getStatus())) {
                return CommonResult.error(COUPON_NOT_FOUND);
            }
            String operator = StringUtils.isNotEmpty(revokeReqVO.getOperator()) ? revokeReqVO.getOperator() : "system";
            openApiCouponService.revokeCoupon(revokeReqVO.getCouponId(), revokeReqVO.getUuid(), operator);

            // 发送撤销优惠券回调
            LambdaQueryWrapper<MtUserCoupon> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(MtUserCoupon::getUuid, revokeReqVO.getUuid());
            queryWrapper.eq(MtUserCoupon::getStatus, UserCouponStatusEnum.DISABLE.getKey());
            List<MtUserCoupon> userCoupons = mtUserCouponMapper.selectList(queryWrapper);
            if (CollUtil.isNotEmpty(userCoupons)) {
                for (MtUserCoupon userCoupon : userCoupons) {
                    eventCallbackService.sendCouponEventCallback(userCoupon, "REVOKED", null);
                }
            }

        }

        return CommonResult.success(true);

    }

    /**
     * 转换CreateReqVO到DTO
     */
    private ReqCouponDto convertToDto(MtCouponCreateReqVO createReqVO) {
        ReqCouponDto dto = new ReqCouponDto();
        BeanUtils.copyProperties(createReqVO, dto);
        // 处理商品列表
        if (CollUtil.isNotEmpty(createReqVO.getGoodsList())) {
            dto.setGoodsIds(CollUtil.join(createReqVO.getGoodsList(), ","));
        }
        if (CollUtil.isNotEmpty(createReqVO.getStoreIds())) {
            dto.setStoreIds(CollUtil.join(createReqVO.getStoreIds(), ","));
        }
        if (CollUtil.isNotEmpty(createReqVO.getGradeIds())) {
            dto.setGradeIds(CollUtil.join(createReqVO.getGradeIds(), ","));
        }
        dto.setStatus(StatusEnum.ENABLED.getKey());
        if (createReqVO.getBeginTime() != null) {
            dto.setBeginTime(DateUtil.format(createReqVO.getBeginTime(), DatePattern.NORM_DATETIME_PATTERN));
        }
        if (createReqVO.getEndTime() != null) {
            dto.setEndTime(DateUtil.format(createReqVO.getEndTime(), DatePattern.NORM_DATETIME_PATTERN));
        }
        dto.setExpireType(createReqVO.getExpireType().getKey());
        return dto;
    }


    /**
     * 转换CreateReqVO到DTO
     */
    private ReqCouponDto convertToDto(MtCouponUpdateReqVO createReqVO) {
        ReqCouponDto dto = new ReqCouponDto();
        BeanUtils.copyProperties(createReqVO, dto);
        // 处理商品列表
        if (CollUtil.isNotEmpty(createReqVO.getGoodsList())) {
            dto.setGoodsIds(CollUtil.join(createReqVO.getGoodsList(), ","));
        }
        if (CollUtil.isNotEmpty(createReqVO.getStoreIds())) {
            dto.setStoreIds(CollUtil.join(createReqVO.getStoreIds(), ","));
        }
        if (CollUtil.isNotEmpty(createReqVO.getGradeIds())) {
            dto.setGradeIds(CollUtil.join(createReqVO.getGradeIds(), ","));
        }
        if (createReqVO.getBeginTime() != null) {
            dto.setBeginTime(DateUtil.format(createReqVO.getBeginTime(), DatePattern.NORM_DATETIME_PATTERN));
        }
        if (createReqVO.getEndTime() != null) {
            dto.setEndTime(DateUtil.format(createReqVO.getEndTime(), DatePattern.NORM_DATETIME_PATTERN));
        }
        dto.setExpireType(createReqVO.getExpireType().getKey());
        return dto;
    }

    /**
     * 转换MtCoupon到RespVO
     */
    private MtCouponRespVO convertToRespVO(MtCoupon coupon) {
        MtCouponRespVO respVO = new MtCouponRespVO();
        BeanUtils.copyProperties(coupon, respVO);
        respVO.setExpireType(CouponExpireTypeEnum.getType(coupon.getExpireType()));
        respVO.setGoodsIds(openApiCouponService.getCouponGoodsIds(coupon.getId()));
        respVO.setStoreIds(StrUtils.splitToInt(coupon.getStoreIds(), ","));
        respVO.setGradeIds(StrUtils.splitToInt(coupon.getGradeIds(), ","));
        // 获取已发放数量和剩余数量
        Integer sendNum = openApiCouponService.getSendNum(coupon.getId());
        respVO.setSentNum(sendNum);
        Integer leftNum = openApiCouponService.getLeftNum(coupon.getId());
        respVO.setLeftNum(leftNum);
        return respVO;
    }
}
