package com.fuint.openapi.v1.member.user;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.RegexPool;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReUtil;
import cn.iocoder.yudao.framework.ratelimiter.core.annotation.RateLimiter;
import cn.iocoder.yudao.framework.ratelimiter.core.keyresolver.impl.ClientIpRateLimiterKeyResolver;
import cn.iocoder.yudao.framework.signature.core.annotation.ApiSignature;
import com.fuint.common.enums.StaffCategoryEnum;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.enums.YesOrNoEnum;
import com.fuint.common.service.*;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.exception.ServiceException;
import com.fuint.framework.pojo.CommonResult;
import com.fuint.framework.pojo.PageResult;
import com.fuint.framework.web.BaseController;
import com.fuint.openapi.enums.UserErrorCodeConstants;
import com.fuint.openapi.service.OpenUserService;
import com.fuint.openapi.v1.member.user.vo.*;
import com.fuint.repository.mapper.MtUserCouponMapper;
import com.fuint.repository.model.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

import static com.fuint.framework.util.collection.CollectionUtils.*;
import static com.fuint.framework.util.object.BeanUtils.toBean;

/**
 * OpenAPI-员工管理相关接口
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Slf4j
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

    @Resource
    private UserCouponService userCouponService;

    @Resource
    private CouponService couponService;

    @Resource
    private StaffService staffService;

    @Resource
    private OpenUserService openUserService;

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
            List<String> mobileLs = convertList(Collections.singletonList(syncReqVO), MtUserSyncReqVO::getMobile);
            List<MtUser> existLs = openUserService.getUserLsByMobiles(mobileLs);
            Map<String, MtUser> existMap = convertMap(existLs, MtUser::getMobile);
            MtUserSyncRespVO result = syncSingleUser(syncReqVO, existMap);
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
            return CommonResult.error(UserErrorCodeConstants.USER_BATCH_SYNC_EXCEED_LIMIT, 100);
        }
        List<String> mobileLs = convertList(users, MtUserSyncReqVO::getMobile);
        List<MtUser> existLs = openUserService.getUserLsByMobiles(mobileLs);
        Map<String, MtUser> existMap = convertMap(existLs, MtUser::getMobile);

        MtUserBatchSyncRespVO response = new MtUserBatchSyncRespVO();
        List<MtUserSyncRespVO> results = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        for (MtUserSyncReqVO syncReqVO : users) {
            try {
                MtUserSyncRespVO result = syncSingleUser(syncReqVO, existMap);
                results.add(result);
                if (result.getSuccess()) {
                    successCount++;
                } else {
                    failureCount++;
                }
            } catch (Exception e) {
                log.error("同步失败:{}", e.getMessage(), e);
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
    private MtUserSyncRespVO syncSingleUser(MtUserSyncReqVO syncReqVO, Map<String, MtUser> existMap) throws BusinessCheckException {
        MtUserSyncRespVO result = new MtUserSyncRespVO();
        result.setMobile(syncReqVO.getMobile());
        // 校验手机号格式
        if (!ReUtil.isMatch(RegexPool.MOBILE, syncReqVO.getMobile())) {
            result.setSuccess(false);
            result.setMessage("手机号格式不正确");
            return result;
        }
        // 设置默认商户ID
        // 根据手机号查询会员是否存在
        MtUser existingUser = existMap.getOrDefault(syncReqVO.getMobile(), null);
        MtUser mtUser;
        String operationType;
        if (existingUser == null) {
            // 创建新会员
            mtUser = toBean(syncReqVO, MtUser.class);
            operationType = "create";
        } else {
            // 更新已有会员
            mtUser = toBean(syncReqVO, MtUser.class);
            mtUser.setId(existingUser.getId());
            mtUser.setUpdateTime(new Date());
            operationType = "update";
        }
        mtUser.setOperator("openapi");
        // 保存或更新会员
        if (operationType.equals("create")) {
            try {
                MtUser savedUser = memberService.addMember(mtUser);
                if (savedUser == null) {
                    result.setSuccess(false);
                    result.setMessage("创建会员失败");
                    return result;
                }
                result.setUserId(savedUser.getId());
                if (YesOrNoEnum.YES.getKey().equals(savedUser.getIsStaff())) {
                    MtStaff staff = new MtStaff();
                    staff.setUserId(savedUser.getId());
                    staff.setMobile(savedUser.getMobile());
                    staff.setRealName(savedUser.getName());
                    staff.setMerchantId(savedUser.getMerchantId());
                    staff.setStoreId(savedUser.getStoreId());
                    if (syncReqVO.getStaffLevel() == null) {
                        staff.setCategory(Integer.parseInt(StaffCategoryEnum.OTHER.getKey()));
                    } else {
                        staff.setCategory(syncReqVO.getStaffLevel());
                    }
                    staff.setAuditedStatus(StatusEnum.ENABLED.getKey());
                    staffService.createStaff(staff, "openapi");
                    // 员工ID
                    result.setStaffId(staff.getId());
                }
            } catch (ServiceException e) {
                result.setSuccess(false);
                result.setMessage(e.getMessage());
            }
        } else {

            try {
                openUserService.updateMember(mtUser);
                result.setUserId(mtUser.getId());
                if (YesOrNoEnum.YES.getKey().equals(mtUser.getIsStaff())) {
                    MtStaff updateObj = new MtStaff();
                    updateObj.setUserId(mtUser.getId());
                    updateObj.setMobile(mtUser.getMobile());
                    updateObj.setRealName(mtUser.getName());
                    updateObj.setMerchantId(mtUser.getMerchantId());
                    updateObj.setStoreId(mtUser.getStoreId());
                    if (syncReqVO.getStaffLevel() != null) {
                        updateObj.setCategory(syncReqVO.getStaffLevel());
                    }
                    MtStaff staff = staffService.updateStaff(updateObj, "openapi");
                    // 员工ID
                    result.setStaffId(staff.getId());
                } else {
                    staffService.deleteStaff(mtUser.getMobile(), "openapi");
                }
            } catch (ServiceException e) {
                result.setSuccess(false);
                result.setMessage(e.getMessage());
            }
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

            // 使用单个查询方法，避免批量查询的开销
            MtStaff staff = staffService.queryStaffByUserId(mtUser.getId());
            MtUserGrade grade = StringUtils.isNotBlank(mtUser.getGradeId()) ?
                    userGradeService.getById(Integer.parseInt(mtUser.getGradeId())) : null;
            MtUserGroup group = mtUser.getGroupId() != null ?
                    memberGroupService.getById(mtUser.getGroupId()) : null;
            MtStore store = mtUser.getStoreId() != null ?
                    storeService.getById(mtUser.getStoreId()) : null;

            Map<Integer, MtStaff> staffMap = staff != null ?
                    Collections.singletonMap(mtUser.getId(), staff) : Collections.emptyMap();
            Map<Integer, String> gradeMap = grade != null ?
                    Collections.singletonMap(grade.getId(), grade.getName()) : Collections.emptyMap();
            Map<Integer, String> groupMap = group != null ?
                    Collections.singletonMap(group.getId(), group.getName()) : Collections.emptyMap();
            Map<Integer, String> storeMap = store != null ?
                    Collections.singletonMap(store.getId(), store.getName()) : Collections.emptyMap();

            return CommonResult.success(convertToRespVO(mtUser, groupMap, gradeMap, storeMap, staffMap));
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
    public CommonResult<PageResult<MtUserRespVO>> getUserPage(@Valid MtUserPageReqVO pageReqVO) {
        PageResult<MtUser> result = memberService.getMemberPage(pageReqVO);
        List<MtUser> userLs = result.getList();
        if (CollUtil.isEmpty(userLs)) {
            return CommonResult.success(PageResult.empty());
        }

        // 收集关联数据的ID
        Set<Integer> userIds = convertSet(filterList(userLs, user -> ObjectUtil.isNotNull(user.getId())), MtUser::getId);
        Set<Integer> storeIds = convertSet(filterList(userLs, user -> ObjectUtil.isNotNull(user.getStoreId())), MtUser::getStoreId);
        Set<Integer> groupIds = convertSet(filterList(userLs, user -> ObjectUtil.isNotNull(user.getGroupId())), MtUser::getGroupId);
        Set<Integer> gradeIds = convertSet(filterList(userLs, user -> StringUtils.isNotBlank(user.getGradeId())), user -> Integer.parseInt(user.getGradeId()));

        // 批量查询关联数据（添加空集合检查，避免无效查询）
        Map<Integer, MtStaff> staffMap = buildStaffMap(userIds);
        Map<Integer, String> gradeMap = buildGradeMap(gradeIds);
        Map<Integer, String> groupMap = buildGroupMap(groupIds);
        Map<Integer, String> storeMap = buildStoreMap(storeIds);

        // 转换为响应VO
        List<MtUserRespVO> respVOList = userLs.stream()
                .map(user -> convertToRespVO(user, groupMap, gradeMap, storeMap, staffMap))
                .collect(Collectors.toList());

        PageResult<MtUserRespVO> pageRespVO = new PageResult<>();
        pageRespVO.setTotal(result.getTotal());
        pageRespVO.setTotalPages(result.getTotalPages());
        pageRespVO.setList(respVOList);
        pageRespVO.setCurrentPage(result.getCurrentPage());
        pageRespVO.setPageSize(result.getPageSize());
        return CommonResult.success(pageRespVO);
    }

    /**
     * 转换MtUser为响应VO
     */
    private MtUserRespVO convertToRespVO(MtUser mtUser, Map<Integer, String> groupMap, Map<Integer, String> gradeMap, Map<Integer, String> storeMap, Map<Integer, MtStaff> staffLs) {
        MtUserRespVO respVO = new MtUserRespVO();
        BeanUtils.copyProperties(mtUser, respVO);
        // 隐藏手机号中间四位
        String phone = mtUser.getMobile();
        respVO.setMobile(phone);
        respVO.setGroupName(groupMap.getOrDefault(mtUser.getGroupId(), ""));
        if (NumberUtil.isInteger(mtUser.getGradeId())) {
            respVO.setGradeName(gradeMap.getOrDefault(Integer.parseInt(mtUser.getGradeId()), ""));
        } else {
            respVO.setGradeName("");
        }
        MtStaff staff = staffLs.getOrDefault(mtUser.getId(), new MtStaff());
        respVO.setStaffLevel(staff.getCategory());
        respVO.setStaffId(staff.getId());
        respVO.setStoreName(storeMap.getOrDefault(mtUser.getStoreId(), null));
        return respVO;
    }

    /**
     * 构建员工信息Map
     *
     * @param userIds 用户ID集合
     * @return 员工信息Map（key: userId, value: MtStaff）
     */
    private Map<Integer, MtStaff> buildStaffMap(Set<Integer> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        List<MtStaff> staffs = staffService.queryStaffListByUserIds(userIds);
        return convertMap(staffs, MtStaff::getUserId);
    }

    /**
     * 构建等级信息Map
     *
     * @param gradeIds 等级ID集合
     * @return 等级信息Map（key: gradeId, value: gradeName）
     */
    private Map<Integer, String> buildGradeMap(Set<Integer> gradeIds) {
        if (CollUtil.isEmpty(gradeIds)) {
            return Collections.emptyMap();
        }
        List<MtUserGrade> grades = userGradeService.getUserGradeListByIds(gradeIds);
        return convertMap(grades, MtUserGrade::getId, MtUserGrade::getName);
    }

    /**
     * 构建分组信息Map
     *
     * @param groupIds 分组ID集合
     * @return 分组信息Map（key: groupId, value: groupName）
     */
    private Map<Integer, String> buildGroupMap(Set<Integer> groupIds) {
        if (CollUtil.isEmpty(groupIds)) {
            return Collections.emptyMap();
        }
        List<MtUserGroup> groups = memberGroupService.getUserGroupByIds(groupIds);
        return convertMap(groups, MtUserGroup::getId, MtUserGroup::getName);
    }

    /**
     * 构建店铺信息Map
     *
     * @param storeIds 店铺ID集合
     * @return 店铺信息Map（key: storeId, value: storeName）
     */
    private Map<Integer, String> buildStoreMap(Set<Integer> storeIds) {
        if (CollUtil.isEmpty(storeIds)) {
            return Collections.emptyMap();
        }
        List<MtStore> stores = storeService.getStoreByIds(storeIds);
        return convertMap(stores, MtStore::getId, MtStore::getName);
    }

}
