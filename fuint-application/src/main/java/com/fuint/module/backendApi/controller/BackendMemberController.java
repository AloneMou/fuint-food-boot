package com.fuint.module.backendApi.controller;

import com.fuint.common.Constants;
import com.fuint.common.dto.*;
import com.fuint.common.enums.SettingTypeEnum;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.enums.UserSettingEnum;
import com.fuint.common.enums.YesOrNoEnum;
import com.fuint.common.service.*;
import com.fuint.common.util.DateUtil;
import com.fuint.common.util.PhoneFormatCheckUtils;
import com.fuint.common.util.TokenUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.web.BaseController;
import com.fuint.framework.web.ResponseObject;
import com.fuint.openapi.service.EventCallbackService;
import com.fuint.repository.model.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import weixin.popular.util.JsonUtil;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.*;

/**
 * 会员管理类controller
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Api(tags="管理端-会员相关接口")
@RestController
@AllArgsConstructor
@RequestMapping(value = "/backendApi/member")
public class BackendMemberController extends BaseController {

    /**
     * 会员服务接口
     */
    private MemberService memberService;

    /**
     * 配置服务接口
     * */
    private SettingService settingService;

    /**
     * 后台账户服务接口
     */
    private AccountService accountService;

    /**
     * 店铺服务接口
     */
    private StoreService storeService;

    /**
     * 会员分组服务接口
     */
    private MemberGroupService memberGroupService;

    /**
     * 微信相关接口
     * */
    private WeixinService weixinService;

    /**
     * 事件回调服务
     */
    private EventCallbackService eventCallbackService;

    /**
     * 查询会员列表
     *
     * @param request  HttpServletRequest对象
     * @return 会员列表
     */
    @ApiOperation(value = "查询会员列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('member:index')")
    public ResponseObject list(HttpServletRequest request) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        String mobile = request.getParameter("mobile");
        String userId = request.getParameter("id");
        String name = request.getParameter("name");
        String birthday = request.getParameter("birthday");
        String userNo = request.getParameter("userNo");
        String gradeId = request.getParameter("gradeId");
        String startTime = request.getParameter("startTime") == null ? "" : request.getParameter("startTime");
        String endTime = request.getParameter("endTime") == null ? "" : request.getParameter("endTime");
        String status = request.getParameter("status");
        String storeIds = request.getParameter("storeIds");
        String groupIds = request.getParameter("groupIds");
        Integer page = request.getParameter("page") == null ? Constants.PAGE_NUMBER : Integer.parseInt(request.getParameter("page"));
        Integer pageSize = request.getParameter("pageSize") == null ? Constants.PAGE_SIZE : Integer.parseInt(request.getParameter("pageSize"));

        PaginationRequest paginationRequest = new PaginationRequest();
        paginationRequest.setCurrentPage(page);
        paginationRequest.setPageSize(pageSize);

        Map<String, Object> params = new HashMap<>();
        if (StringUtils.isNotEmpty(userId)) {
            params.put("id", userId);
        }
        if (StringUtils.isNotEmpty(name)) {
            params.put("name", name);
        }
        if (StringUtils.isNotEmpty(mobile)) {
            params.put("mobile", mobile);
        }
        if (StringUtils.isNotEmpty(birthday)) {
            params.put("birthday", birthday);
        }
        if (StringUtils.isNotEmpty(userNo)) {
            params.put("userNo", userNo);
        }
        if (StringUtils.isNotEmpty(gradeId)) {
            params.put("gradeId", gradeId);
        }
        if (StringUtils.isNotEmpty(status)) {
            params.put("status", status);
        }
        if (StringUtils.isNotEmpty(storeIds)) {
            params.put("storeIds", storeIds);
        }
        if (StringUtils.isNotEmpty(groupIds)) {
            params.put("groupIds", groupIds);
        }
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }
        TAccount account = accountService.getAccountInfoById(accountInfo.getId());
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            params.put("merchantId", accountInfo.getMerchantId());
        }
        if (StringUtils.isNotEmpty(startTime)) {
            params.put("startTime", startTime);
        }
        if (StringUtils.isNotEmpty(endTime)) {
            params.put("endTime", endTime);
        }
        paginationRequest.setSearchParams(params);
        PaginationResponse<UserDto> paginationResponse = memberService.queryMemberListByPagination(paginationRequest);

        // 会员等级列表
        Map<String, Object> param = new HashMap<>();
        param.put("STATUS", StatusEnum.ENABLED.getKey());
        if (account.getMerchantId() != null && account.getMerchantId() > 0) {
            param.put("MERCHANT_ID", account.getMerchantId());
        }
        List<MtUserGrade> userGradeList = memberService.queryMemberGradeByParams(param);

        // 店铺列表
        Map<String, Object> paramsStore = new HashMap<>();
        paramsStore.put("status", StatusEnum.ENABLED.getKey());
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            paramsStore.put("merchantId", accountInfo.getMerchantId());
        }
        List<MtStore> storeList = storeService.queryStoresByParams(paramsStore);

        // 会员分组
        List<UserGroupDto> groupList = new ArrayList<>();
        Map<String, Object> searchParams = new HashMap<>();
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            searchParams.put("merchantId", accountInfo.getMerchantId());
        }
        searchParams.put("status", StatusEnum.ENABLED.getKey());
        PaginationRequest groupRequest = new PaginationRequest();
        groupRequest.setCurrentPage(1);
        groupRequest.setPageSize(Constants.MAX_ROWS);
        groupRequest.setSearchParams(searchParams);
        PaginationResponse<UserGroupDto> groupResponse = memberGroupService.queryMemberGroupListByPagination(groupRequest);
        if (groupResponse != null && groupResponse.getContent() != null) {
            groupList = groupResponse.getContent();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("paginationResponse", paginationResponse);
        result.put("userGradeList", userGradeList);
        result.put("storeList", storeList);
        result.put("groupList", groupList);

        return getSuccessResult(result);
    }

    /**
     * 更新会员状态
     *
     * @return
     */
    @ApiOperation(value = "更新会员状态")
    @RequestMapping(value = "/updateStatus", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('member:index')")
    public ResponseObject updateStatus(HttpServletRequest request, @RequestBody Map<String, Object> param) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        Integer userId = param.get("userId") == null ? 0 : Integer.parseInt(param.get("userId").toString());
        String status = param.get("status") == null ? StatusEnum.ENABLED.getKey() : param.get("status").toString();

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        MtUser userInfo = memberService.queryMemberById(userId);
        if (userInfo == null) {
            return getFailureResult(201, "会员不存在");
        }

        userInfo.setStatus(status);
        memberService.updateMember(userInfo, false);

        // 触发会员状态变更回调
        Map<String, Object> callbackData = new HashMap<>();
        callbackData.put("action", "UPDATE_STATUS");
        callbackData.put("userId", userId);
        callbackData.put("status", status);
        eventCallbackService.sendMemberEventCallback(userInfo.getMerchantId(), callbackData);

        return getSuccessResult(true);
    }

    /**
     * 删除会员
     *
     * @param request
     * @return
     */
    @ApiOperation(value = "删除会员")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('member:index')")
    public ResponseObject delete(HttpServletRequest request, @PathVariable("id") Integer id) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        String operator = accountInfo.getAccountName();
        MtUser userInfo = memberService.queryMemberById(id);
        memberService.deleteMember(id, operator);

        if (userInfo != null) {
            // 触发会员删除回调
            Map<String, Object> callbackData = new HashMap<>();
            callbackData.put("action", "DELETE");
            callbackData.put("userId", id);
            callbackData.put("mobile", userInfo.getMobile());
            eventCallbackService.sendMemberEventCallback(userInfo.getMerchantId(), callbackData);
        }

        return getSuccessResult(true);
    }

    /**
     * 保存会员信息
     *
     * @param request HttpServletRequest对象
     * @return
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('member:add')")
    public ResponseObject save(HttpServletRequest request, @RequestBody Map<String, Object> param) throws BusinessCheckException, ParseException {
        String token = request.getHeader("Access-Token");
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        String id = param.get("id").toString();
        String name = param.get("name") == null ? "" : param.get("name").toString();
        String gradeId = param.get("gradeId") == null ? "0" :param.get("gradeId").toString();
        String groupId = param.get("groupId") == null ? "0" :param.get("groupId").toString();
        String storeId = param.get("storeId") == null ? "0" :param.get("storeId").toString();
        String userNo = param.get("userNo") == null ? "" : param.get("userNo").toString();
        String mobile = param.get("mobile") == null ? "" : param.get("mobile").toString();
        String sex = param.get("sex") == null ? "0" : param.get("sex").toString();
        String idCard = param.get("idcard") == null ? "" : param.get("idcard").toString();
        String birthday = param.get("birthday") == null ? "" : param.get("birthday").toString();
        String address = param.get("address") == null ? "" : param.get("address").toString();
        String description = param.get("description") == null ? "" : param.get("description").toString();
        String status = param.get("status") == null ? StatusEnum.ENABLED.getKey() : param.get("status").toString();
        String startTime = param.get("startTime") == null ? "" : param.get("startTime").toString();
        String endTime = param.get("endTime") == null ? "" : param.get("endTime").toString();

        if (PhoneFormatCheckUtils.isChinaPhoneLegal(mobile)) {
            // 重置该手机号
            memberService.resetMobile(mobile, StringUtils.isEmpty(id) ? 0 : Integer.parseInt(id));
        }

        MtUser memberInfo;
        if (StringUtils.isEmpty(id)) {
            memberInfo = new MtUser();
        } else {
            memberInfo = memberService.queryMemberById(Integer.parseInt(id));
        }

        memberInfo.setMerchantId(accountInfo.getMerchantId());
        memberInfo.setName(name);
        memberInfo.setStatus(status);
        if (StringUtils.isNotEmpty(groupId)) {
            memberInfo.setGroupId(Integer.parseInt(groupId));
        }
        memberInfo.setGradeId(gradeId);
        memberInfo.setUserNo(userNo);
        if (PhoneFormatCheckUtils.isChinaPhoneLegal(mobile)) {
            memberInfo.setMobile(mobile);
        }
        memberInfo.setSex(Integer.parseInt(sex));
        memberInfo.setIdcard(idCard);
        memberInfo.setBirthday(birthday);
        memberInfo.setAddress(address);
        memberInfo.setDescription(description);
        memberInfo.setStartTime(DateUtil.parseDate(startTime));
        memberInfo.setEndTime(DateUtil.parseDate(endTime));
        memberInfo.setIsStaff(YesOrNoEnum.NO.getKey());
        if (StringUtils.isNotEmpty(storeId)) {
            memberInfo.setStoreId(Integer.parseInt(storeId));
        }
        TAccount account = accountService.getAccountInfoById(accountInfo.getId());
        Integer myStoreId = account.getStoreId();
        if (myStoreId != null && myStoreId > 0) {
            memberInfo.setStoreId(myStoreId);
        }
        if (StringUtils.isEmpty(id)) {
            memberService.addMember(memberInfo);
        } else {
            memberService.updateMember(memberInfo, false);
        }

        // 触发会员保存/更新回调
        Map<String, Object> callbackData = new HashMap<>();
        callbackData.put("action", StringUtils.isEmpty(id) ? "ADD" : "UPDATE");
        callbackData.put("userId", memberInfo.getId());
        callbackData.put("mobile", memberInfo.getMobile());
        callbackData.put("name", memberInfo.getName());
        callbackData.put("gradeId", memberInfo.getGradeId());
        eventCallbackService.sendMemberEventCallback(memberInfo.getMerchantId(), callbackData);

        return getSuccessResult(true);
    }

    /**
     * 获取会员详情
     *
     * @param request
     * @return
     */
    @ApiOperation(value = "获取会员详情")
    @RequestMapping(value = "/info/{id}", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('member:index')")
    public ResponseObject info(HttpServletRequest request, @PathVariable("id") Integer id) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        MtUser mtUser = memberService.queryMemberById(id);
        if (mtUser == null) {
            return getFailureResult(201, "会员信息有误");
        }

        UserDto memberInfo = new UserDto();
        BeanUtils.copyProperties(mtUser, memberInfo);

        MtUserGroup mtUserGroup = memberGroupService.queryMemberGroupById(memberInfo.getGroupId());
        if (mtUserGroup != null) {
            UserGroupDto userGroupDto = new UserGroupDto();
            BeanUtils.copyProperties(mtUserGroup, userGroupDto);
            memberInfo.setGroupInfo(userGroupDto);
        }

        // 隐藏手机号中间四位
        String phone = memberInfo.getMobile();
        if (phone != null && StringUtils.isNotEmpty(phone) && phone.length() == 11) {
            memberInfo.setMobile(phone.substring(0, 3) + "****" + phone.substring(7));
        }

        Map<String, Object> param = new HashMap<>();
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            param.put("MERCHANT_ID", accountInfo.getMerchantId());
        }
        param.put("STATUS", StatusEnum.ENABLED.getKey());
        List<MtUserGrade> userGradeList = memberService.queryMemberGradeByParams(param);

        Map<String, Object> result = new HashMap<>();
        result.put("userGradeList", userGradeList);
        result.put("memberInfo", memberInfo);

        return getSuccessResult(result);
    }

    /**
     * 获取会员设置
     *
     * @param request
     * @return
     */
    @ApiOperation(value = "获取会员设置")
    @RequestMapping(value = "/setting", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('member:setting')")
    public ResponseObject setting(HttpServletRequest request) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        List<MtSetting> settingList = settingService.getSettingList(accountInfo.getMerchantId(), SettingTypeEnum.USER.getKey());

        String getCouponNeedPhone = YesOrNoEnum.FALSE.getKey();
        String submitOrderNeedPhone = YesOrNoEnum.FALSE.getKey();
        String loginNeedPhone = YesOrNoEnum.FALSE.getKey();
        String openWxCard = YesOrNoEnum.FALSE.getKey();
        WxCardDto wxMemberCard = null;
        for (MtSetting setting : settingList) {
            if (StringUtils.isNotEmpty(setting.getValue())) {
                if (setting.getName().equals(UserSettingEnum.GET_COUPON_NEED_PHONE.getKey())) {
                    getCouponNeedPhone = setting.getValue();
                } else if (setting.getName().equals(UserSettingEnum.GET_COUPON_NEED_PHONE.getKey())) {
                    submitOrderNeedPhone = setting.getValue();
                } else if (setting.getName().equals(UserSettingEnum.LOGIN_NEED_PHONE.getKey())) {
                    loginNeedPhone = setting.getValue();
                } else if (setting.getName().equals(UserSettingEnum.OPEN_WX_CARD.getKey())) {
                    openWxCard = setting.getValue();
                } else if (setting.getName().equals(UserSettingEnum.WX_MEMBER_CARD.getKey())) {
                    wxMemberCard = JsonUtil.parseObject(setting.getValue(), WxCardDto.class);
                }
            }
        }

        String imagePath = settingService.getUploadBasePath();
        Map<String, Object> result = new HashMap<>();
        result.put("getCouponNeedPhone", getCouponNeedPhone);
        result.put("submitOrderNeedPhone", submitOrderNeedPhone);
        result.put("loginNeedPhone", loginNeedPhone);
        result.put("openWxCard", openWxCard);
        result.put("wxMemberCard", wxMemberCard);
        result.put("imagePath", imagePath);

        return getSuccessResult(result);
    }

    /**
     * 保存会员设置
     *
     * @param request HttpServletRequest对象
     * @return
     */
    @ApiOperation(value = "保存会员设置")
    @RequestMapping(value = "/saveSetting", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('member:setting')")
    public ResponseObject saveSetting(HttpServletRequest request, @RequestBody Map<String, Object> param) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        String getCouponNeedPhone = param.get("getCouponNeedPhone") != null ? param.get("getCouponNeedPhone").toString() : null;
        String submitOrderNeedPhone = param.get("submitOrderNeedPhone") != null ? param.get("submitOrderNeedPhone").toString() : null;
        String loginNeedPhone = param.get("loginNeedPhone") != null ? param.get("loginNeedPhone").toString() : null;
        String openWxCard = param.get("openWxCard") != null ? param.get("openWxCard").toString() : null;
        String wxMemberCard = param.get("wxMemberCard") != null ? param.get("wxMemberCard").toString() : null;

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        UserSettingEnum[] settingList = UserSettingEnum.values();
        for (UserSettingEnum setting : settingList) {
            MtSetting mtSetting = new MtSetting();
            mtSetting.setType(SettingTypeEnum.USER.getKey());
            mtSetting.setName(setting.getKey());
            if (setting.getKey().equals(UserSettingEnum.GET_COUPON_NEED_PHONE.getKey())) {
                mtSetting.setValue(getCouponNeedPhone);
            } else if (setting.getKey().equals(UserSettingEnum.SUBMIT_ORDER_NEED_PHONE.getKey())) {
                mtSetting.setValue(submitOrderNeedPhone);
            } else if (setting.getKey().equals(UserSettingEnum.LOGIN_NEED_PHONE.getKey())) {
                mtSetting.setValue(loginNeedPhone);
            } else if (setting.getKey().equals(UserSettingEnum.OPEN_WX_CARD.getKey())) {
                mtSetting.setValue(openWxCard);
            } else if (setting.getKey().equals(UserSettingEnum.WX_MEMBER_CARD.getKey())) {
                mtSetting.setValue(wxMemberCard);
            }
            mtSetting.setDescription(setting.getValue());
            mtSetting.setOperator(accountInfo.getAccountName());
            mtSetting.setUpdateTime(new Date());
            mtSetting.setMerchantId(accountInfo.getMerchantId());
            mtSetting.setStoreId(0);
            settingService.saveSetting(mtSetting);
        }

        MtSetting openCardSetting = settingService.querySettingByName(accountInfo.getMerchantId(), SettingTypeEnum.USER.getKey(), UserSettingEnum.OPEN_WX_CARD.getKey());
        MtSetting cardSetting = settingService.querySettingByName(accountInfo.getMerchantId(), SettingTypeEnum.USER.getKey(), UserSettingEnum.WX_MEMBER_CARD.getKey());
        MtSetting cardIdSetting = settingService.querySettingByName(accountInfo.getMerchantId(), SettingTypeEnum.USER.getKey(), UserSettingEnum.WX_MEMBER_CARD_ID.getKey());
        if (openCardSetting != null && openCardSetting.getValue().equals(YesOrNoEnum.TRUE.getKey()) && cardSetting != null && accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            String wxCardId = "";
            if (cardIdSetting != null) {
                wxCardId = cardIdSetting.getValue();
            }
            String cardId = weixinService.createWxCard(accountInfo.getMerchantId(), wxCardId);
            if (StringUtils.isNotEmpty(cardId)) {
                MtSetting mtSetting = new MtSetting();
                mtSetting.setType(SettingTypeEnum.USER.getKey());
                mtSetting.setName(UserSettingEnum.WX_MEMBER_CARD_ID.getKey());
                mtSetting.setValue(cardId);
                mtSetting.setOperator(accountInfo.getAccountName());
                mtSetting.setUpdateTime(new Date());
                mtSetting.setMerchantId(accountInfo.getMerchantId());
                mtSetting.setStoreId(0);
                settingService.saveSetting(mtSetting);
            }
        }

        return getSuccessResult(true);
    }

    /**
     * 重置会员密码
     *
     * @return
     */
    @ApiOperation(value = "重置会员密码")
    @RequestMapping(value = "/resetPwd", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('member:add')")
    public ResponseObject resetPwd(HttpServletRequest request, @RequestBody Map<String, Object> param) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        Integer userId = param.get("userId") == null ? 0 : Integer.parseInt(param.get("userId").toString());
        String password = param.get("password") == null ? "" : param.get("password").toString();

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        if (StringUtils.isEmpty(password)) {
            return getFailureResult(1001, "密码格式有误");
        }

        MtUser userInfo = memberService.queryMemberById(userId);
        if (userInfo == null) {
            return getFailureResult(201, "会员不存在");
        }
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0 && !accountInfo.getMerchantId().equals(userInfo.getMerchantId())) {
            return getFailureResult(201, "您没有操作权限");
        }
        if (accountInfo.getStoreId() != null && accountInfo.getStoreId() > 0 && !accountInfo.getStoreId().equals(userInfo.getStoreId())) {
            return getFailureResult(201, "您没有操作权限");
        }

        userInfo.setPassword(password);
        memberService.updateMember(userInfo, true);

        return getSuccessResult(true);
    }

    /**
     * 获取会员分组
     *
     * @param request
     * @return
     */
    @ApiOperation(value = "获取会员分组")
    @RequestMapping(value = "/groupList", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('member:group:index')")
    public ResponseObject groupList(HttpServletRequest request) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        // 会员分组
        List<UserGroupDto> groupList = new ArrayList<>();
        Map<String, Object> searchParams = new HashMap<>();
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            searchParams.put("merchantId", accountInfo.getMerchantId());
        }
        PaginationRequest groupRequest = new PaginationRequest();
        groupRequest.setCurrentPage(1);
        groupRequest.setPageSize(Constants.ALL_ROWS);
        groupRequest.setSearchParams(searchParams);
        PaginationResponse<UserGroupDto> groupResponse = memberGroupService.queryMemberGroupListByPagination(groupRequest);
        if (groupResponse != null && groupResponse.getContent() != null) {
            groupList = groupResponse.getContent();
        }

        return getSuccessResult(groupList);
    }

    /**
     * 查找会员列表
     *
     * @param request
     * @return
     */
    @ApiOperation(value = "查找会员列表")
    @RequestMapping(value = "/searchMembers", method = RequestMethod.GET)
    @CrossOrigin
    public ResponseObject searchMembers(HttpServletRequest request) {
        String token = request.getHeader("Access-Token");
        String groupIds = request.getParameter("groupIds") != null ? request.getParameter("groupIds") : "";
        String keyword = request.getParameter("keyword") != null ? request.getParameter("keyword") : "";
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }
        List<GroupMemberDto> memberList = memberService.searchMembers(accountInfo.getMerchantId(), keyword, groupIds,1, Constants.MAX_ROWS);
        return getSuccessResult(memberList);
    }
}
