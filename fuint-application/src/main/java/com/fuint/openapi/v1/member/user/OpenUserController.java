package com.fuint.openapi.v1.member.user;

import cn.iocoder.yudao.framework.ratelimiter.core.annotation.RateLimiter;
import cn.iocoder.yudao.framework.ratelimiter.core.keyresolver.impl.ClientIpRateLimiterKeyResolver;
import cn.iocoder.yudao.framework.signature.core.annotation.ApiSignature;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fuint.common.dto.UserDto;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.service.*;
import com.fuint.common.util.PhoneFormatCheckUtils;
import com.fuint.common.util.TimeUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.pojo.CommonResult;
import com.fuint.framework.web.BaseController;
import com.fuint.openapi.enums.UserErrorCodeConstants;
import com.fuint.openapi.v1.member.user.vo.*;
import com.fuint.repository.mapper.MtUserCouponMapper;
import com.fuint.repository.model.*;
import com.fuint.utils.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * OpenAPI-员工管理相关接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Validated
@Api(tags = "OpenApi-员工管理相关接口")
@RestController
@AllArgsConstructor
@RequestMapping(value = "/api/v1/member/user")
public class OpenUserController extends BaseController {

    @Resource
    private MemberService memberService;

    @Resource
    private MemberGroupService memberGroupService;

    @Resource
    private UserGradeService userGradeService;

    @Resource
    private StoreService storeService;

    @Resource
    private MtUserCouponMapper mtUserCouponMapper;

    /**
     * 单个员工数据同步
     *
     * @param syncReqVO 同步请求参数
     * @return 同步结果
     */
    @ApiOperation(value = "单个员工数据同步", notes = "根据手机号同步员工数据，如果员工不存在则创建，如果已存在则更新")
    @PostMapping(value = "/sync")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<MtUserSyncRespVO> syncUser(@Valid @RequestBody MtUserSyncReqVO syncReqVO) {
        try {
            MtUserSyncRespVO result = syncSingleUser(syncReqVO);
            return CommonResult.success(result);
        } catch (BusinessCheckException e) {
            return CommonResult.error(100_2_012, e.getMessage());
        } catch (Exception e) {
            return CommonResult.error(500, "同步员工数据失败: " + e.getMessage());
        }
    }

    /**
     * 批量员工数据同步
     *
     * @param batchSyncReqVO 批量同步请求参数
     * @return 批量同步结果
     */
    @ApiOperation(value = "批量员工数据同步", notes = "批量同步员工数据，每次最多同步100条")
    @PostMapping(value = "/batchSync")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<MtUserBatchSyncRespVO> batchSyncUser(@Valid @RequestBody MtUserBatchSyncReqVO batchSyncReqVO) {
        List<MtUserSyncReqVO> users = batchSyncReqVO.getUsers();
        if (users == null || users.isEmpty()) {
            return CommonResult.error(UserErrorCodeConstants.USER_BATCH_SYNC_EMPTY);
        }
        if (users.size() > 100) {
            return CommonResult.error(UserErrorCodeConstants.USER_BATCH_SYNC_EXCEED_LIMIT);
        }

        MtUserBatchSyncRespVO response = new MtUserBatchSyncRespVO();
        List<MtUserSyncRespVO> results = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        for (MtUserSyncReqVO syncReqVO : users) {
            try {
                MtUserSyncRespVO result = syncSingleUser(syncReqVO);
                results.add(result);
                if (result.getSuccess()) {
                    successCount++;
                } else {
                    failureCount++;
                }
            } catch (Exception e) {
                MtUserSyncRespVO result = new MtUserSyncRespVO();
                result.setMobile(syncReqVO.getMobile());
                result.setSuccess(false);
                result.setMessage("同步失败: " + e.getMessage());
                results.add(result);
                failureCount++;
            }
        }

        response.setResults(results);
        response.setSuccessCount(successCount);
        response.setFailureCount(failureCount);
        response.setTotalCount(users.size());

        return CommonResult.success(response);
    }

    /**
     * 同步单个用户的内部方法
     */
    private MtUserSyncRespVO syncSingleUser(MtUserSyncReqVO syncReqVO) throws BusinessCheckException {
        MtUserSyncRespVO result = new MtUserSyncRespVO();
        result.setMobile(syncReqVO.getMobile());

        // 校验手机号格式
        if (!PhoneFormatCheckUtils.isChinaPhoneLegal(syncReqVO.getMobile())) {
            result.setSuccess(false);
            result.setMessage("手机号格式不正确");
            return result;
        }

        // 设置默认商户ID
        Integer merchantId = syncReqVO.getMerchantId() != null ? syncReqVO.getMerchantId() : 1;

        // 根据手机号查询会员是否存在
        MtUser existingUser = memberService.queryMemberByMobile(merchantId, syncReqVO.getMobile());

        MtUser mtUser;
        String operationType;

        if (existingUser == null) {
            // 创建新会员
            mtUser = new MtUser();
            operationType = "create";
        } else {
            // 更新已有会员
            mtUser = existingUser;
            operationType = "update";
        }

        // 设置/更新会员信息
        mtUser.setMobile(syncReqVO.getMobile());
        mtUser.setMerchantId(merchantId);

        if (StringUtil.isNotEmpty(syncReqVO.getName())) {
            mtUser.setName(syncReqVO.getName());
        }
        if (StringUtil.isNotEmpty(syncReqVO.getUserNo())) {
            mtUser.setUserNo(syncReqVO.getUserNo());
        }
        if (syncReqVO.getGroupId() != null) {
            mtUser.setGroupId(syncReqVO.getGroupId());
        }
        if (StringUtil.isNotEmpty(syncReqVO.getGradeId())) {
            mtUser.setGradeId(syncReqVO.getGradeId());
        }
        if (syncReqVO.getStoreId() != null) {
            mtUser.setStoreId(syncReqVO.getStoreId());
        }
        if (syncReqVO.getSex() != null) {
            mtUser.setSex(syncReqVO.getSex());
        }
        if (StringUtil.isNotEmpty(syncReqVO.getBirthday())) {
            mtUser.setBirthday(syncReqVO.getBirthday());
        }
        if (StringUtil.isNotEmpty(syncReqVO.getIdcard())) {
            mtUser.setIdcard(syncReqVO.getIdcard());
        }
        if (StringUtil.isNotEmpty(syncReqVO.getAddress())) {
            mtUser.setAddress(syncReqVO.getAddress());
        }
        if (StringUtil.isNotEmpty(syncReqVO.getAvatar())) {
            mtUser.setAvatar(syncReqVO.getAvatar());
        }
        if (syncReqVO.getBalance() != null) {
            mtUser.setBalance(syncReqVO.getBalance());
        }
        if (syncReqVO.getPoint() != null) {
            mtUser.setPoint(syncReqVO.getPoint());
        }
        if (StringUtil.isNotEmpty(syncReqVO.getStatus())) {
            mtUser.setStatus(syncReqVO.getStatus());
        }
        if (StringUtil.isNotEmpty(syncReqVO.getDescription())) {
            mtUser.setDescription(syncReqVO.getDescription());
        }

        mtUser.setOperator("openapi");

        // 保存或更新会员
        if (operationType.equals("create")) {
            MtUser savedUser = memberService.addMember(mtUser);
            if (savedUser == null) {
                result.setSuccess(false);
                result.setMessage("创建会员失败");
                return result;
            }
            result.setUserId(savedUser.getId());
        } else {
            MtUser updatedUser = memberService.updateMember(mtUser, false);
            if (updatedUser == null) {
                result.setSuccess(false);
                result.setMessage("更新会员失败");
                return result;
            }
            result.setUserId(updatedUser.getId());
        }

        result.setOperationType(operationType);
        result.setSuccess(true);
        result.setMessage("同步成功");

        return result;
    }

    /**
     * 获取员工详情
     *
     * @param id 会员ID
     * @return 员工详情
     */
    @ApiOperation(value = "获取员工详情", notes = "根据ID获取员工详细信息")
    @GetMapping(value = "/detail/{id}")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<MtUserRespVO> getUserDetail(
            @ApiParam(value = "会员ID", required = true, example = "1")
            @PathVariable("id") Integer id) {
        try {
            MtUser mtUser = memberService.queryMemberById(id);
            if (mtUser == null) {
                return CommonResult.error(UserErrorCodeConstants.USER_NOT_FOUND);
            }

            MtUserRespVO respVO = convertToRespVO(mtUser);
            return CommonResult.success(respVO);
        } catch (BusinessCheckException e) {
            return CommonResult.error(500, "获取员工详情失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询员工列表
     *
     * @param pageReqVO 分页查询参数
     * @return 员工列表
     */
    @ApiOperation(value = "分页查询员工列表", notes = "支持按姓名、手机号、状态、优惠券状态等条件分页查询")
    @GetMapping(value = "/page")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<MtUserPageRespVO> getUserPage(@Valid MtUserPageReqVO pageReqVO) {
        try {
            // 构建分页请求
            PaginationRequest paginationRequest = new PaginationRequest();
            paginationRequest.setCurrentPage(pageReqVO.getPage());
            paginationRequest.setPageSize(pageReqVO.getPageSize());

            // 构建查询参数
            Map<String, Object> params = new HashMap<>();
            if (pageReqVO.getId() != null) {
                params.put("id", pageReqVO.getId().toString());
            }
            if (StringUtil.isNotEmpty(pageReqVO.getName())) {
                params.put("name", pageReqVO.getName());
            }
            if (StringUtil.isNotEmpty(pageReqVO.getMobile())) {
                params.put("mobile", pageReqVO.getMobile());
            }
            if (StringUtil.isNotEmpty(pageReqVO.getUserNo())) {
                params.put("userNo", pageReqVO.getUserNo());
            }
            if (StringUtil.isNotEmpty(pageReqVO.getGroupIds())) {
                params.put("groupIds", pageReqVO.getGroupIds());
            }
            if (StringUtil.isNotEmpty(pageReqVO.getGradeId())) {
                params.put("gradeId", pageReqVO.getGradeId());
            }
            if (pageReqVO.getMerchantId() != null) {
                params.put("merchantId", pageReqVO.getMerchantId().toString());
            }
            if (StringUtil.isNotEmpty(pageReqVO.getStoreIds())) {
                params.put("storeIds", pageReqVO.getStoreIds());
            }
            if (StringUtil.isNotEmpty(pageReqVO.getStatus())) {
                params.put("status", pageReqVO.getStatus());
            }
            if (StringUtil.isNotEmpty(pageReqVO.getStartTime())) {
                params.put("startTime", pageReqVO.getStartTime());
            }
            if (StringUtil.isNotEmpty(pageReqVO.getEndTime())) {
                params.put("endTime", pageReqVO.getEndTime());
            }

            paginationRequest.setSearchParams(params);

            // 执行查询
            PaginationResponse<UserDto> paginationResponse = memberService.queryMemberListByPagination(paginationRequest);

            // 如果有优惠券状态筛选，需要额外过滤
            List<UserDto> filteredList = paginationResponse.getContent();
            if (StringUtil.isNotEmpty(pageReqVO.getCouponStatus())) {
                filteredList = filterByCouponStatus(filteredList, pageReqVO.getCouponStatus());
            }

            // 构建响应
            MtUserPageRespVO respVO = new MtUserPageRespVO();

            // 转换数据
            List<MtUserRespVO> list = filteredList.stream()
                    .map(this::convertUserDtoToRespVO)
                    .collect(Collectors.toList());

            respVO.setList(list);
            respVO.setTotal(paginationResponse.getTotalElements());
            respVO.setTotalPages(paginationResponse.getTotalPages());
            respVO.setCurrentPage(pageReqVO.getPage());
            respVO.setPageSize(pageReqVO.getPageSize());

            return CommonResult.success(respVO);
        } catch (BusinessCheckException e) {
            return CommonResult.error(500, "查询员工列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据优惠券状态筛选会员列表
     */
    private List<UserDto> filterByCouponStatus(List<UserDto> userList, String couponStatus) {
        if (userList == null || userList.isEmpty()) {
            return userList;
        }

        return userList.stream()
                .filter(user -> {
                    // 查询该会员的优惠券状态
                    LambdaQueryWrapper<MtUserCoupon> wrapper = Wrappers.lambdaQuery();
                    wrapper.eq(MtUserCoupon::getUserId, user.getId());
                    wrapper.eq(MtUserCoupon::getStatus, couponStatus);
                    wrapper.last("LIMIT 1");
                    List<MtUserCoupon> coupons = mtUserCouponMapper.selectList(wrapper);
                    return !coupons.isEmpty();
                })
                .collect(Collectors.toList());
    }

    /**
     * 转换MtUser为响应VO
     */
    private MtUserRespVO convertToRespVO(MtUser mtUser) {
        MtUserRespVO respVO = new MtUserRespVO();
        BeanUtils.copyProperties(mtUser, respVO);

        // 隐藏手机号中间四位
        String phone = mtUser.getMobile();
        if (phone != null && StringUtil.isNotEmpty(phone) && phone.length() == 11) {
            respVO.setMobile(phone.substring(0, 3) + "****" + phone.substring(7));
        }

        // 设置分组名称
        if (mtUser.getGroupId() != null && mtUser.getGroupId() > 0) {
            try {
                MtUserGroup mtUserGroup = memberGroupService.queryMemberGroupById(mtUser.getGroupId());
                if (mtUserGroup != null) {
                    respVO.setGroupName(mtUserGroup.getName());
                }
            } catch (Exception e) {
                // 忽略异常
            }
        }

        // 设置等级名称
        if (StringUtil.isNotEmpty(mtUser.getGradeId())) {
            try {
                MtUserGrade mtGrade = userGradeService.queryUserGradeById(
                        mtUser.getMerchantId(),
                        Integer.parseInt(mtUser.getGradeId()),
                        mtUser.getId());
                if (mtGrade != null) {
                    respVO.setGradeName(mtGrade.getName());
                }
            } catch (Exception e) {
                // 忽略异常
            }
        }

        // 设置店铺名称
        if (mtUser.getStoreId() != null && mtUser.getStoreId() > 0) {
            try {
                MtStore mtStore = storeService.queryStoreById(mtUser.getStoreId());
                if (mtStore != null) {
                    respVO.setStoreName(mtStore.getName());
                }
            } catch (Exception e) {
                // 忽略异常
            }
        }

        // 设置最后登录时间描述
        respVO.setLastLoginTime(TimeUtil.showTime(new Date(), mtUser.getUpdateTime()));

        return respVO;
    }

    /**
     * 转换UserDto为响应VO
     */
    private MtUserRespVO convertUserDtoToRespVO(UserDto userDto) {
        MtUserRespVO respVO = new MtUserRespVO();
        BeanUtils.copyProperties(userDto, respVO);

        // UserDto已经处理过手机号脱敏
        respVO.setGradeName(userDto.getGradeName());
        respVO.setStoreName(userDto.getStoreName());
        respVO.setLastLoginTime(userDto.getLastLoginTime());

        // 设置分组名称
        if (userDto.getGroupInfo() != null) {
            respVO.setGroupName(userDto.getGroupInfo().getName());
        }

        return respVO;
    }

}
