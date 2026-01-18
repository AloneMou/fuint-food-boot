package com.fuint.module.backendApi.controller;

import com.fuint.common.Constants;
import com.fuint.common.dto.AccountInfo;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.service.AppService;
import com.fuint.common.util.TokenUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.web.BaseController;
import com.fuint.framework.web.ResponseObject;
import com.fuint.repository.model.app.MtApp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * APP应用管理控制器
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Api(tags="管理端-APP应用管理相关接口")
@RestController
@AllArgsConstructor
@RequestMapping(value = "/backendApi/app")
public class BackendAppController extends BaseController {

    /**
     * APP应用服务接口
     */
    private AppService appService;

    /**
     * 分页查询APP应用列表
     *
     * @param request HttpServletRequest对象
     * @return APP应用列表
     */
    @ApiOperation(value = "分页查询APP应用列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('system:app:index')")
    public ResponseObject list(HttpServletRequest request) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        Integer page = request.getParameter("page") == null ? Constants.PAGE_NUMBER : Integer.parseInt(request.getParameter("page"));
        Integer pageSize = request.getParameter("pageSize") == null ? Constants.PAGE_SIZE : Integer.parseInt(request.getParameter("pageSize"));
        String appName = request.getParameter("appName");
        String appId = request.getParameter("appId");
        String status = request.getParameter("status");

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        PaginationRequest paginationRequest = new PaginationRequest();
        paginationRequest.setCurrentPage(page);
        paginationRequest.setPageSize(pageSize);

        Map<String, Object> params = new HashMap<>();
        if (StringUtils.isNotEmpty(appName)) {
            params.put("appName", appName);
        }
        if (StringUtils.isNotEmpty(appId)) {
            params.put("appId", appId);
        }
        if (StringUtils.isNotEmpty(status)) {
            params.put("status", status);
        }
        
        paginationRequest.setSearchParams(params);
        PaginationResponse<MtApp> paginationResponse = appService.queryAppListByPagination(paginationRequest);

        Map<String, Object> result = new HashMap<>();
        result.put("paginationResponse", paginationResponse);

        return getSuccessResult(result);
    }

    /**
     * 获取APP应用详情
     *
     * @param id 应用ID
     * @return APP应用详情
     */
    @ApiOperation(value = "获取APP应用详情")
    @RequestMapping(value = "/info/{id}", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('system:app:index')")
    public ResponseObject info(HttpServletRequest request, @PathVariable("id") Long id) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        MtApp appInfo = appService.queryAppById(id);
        if (appInfo == null) {
            return getFailureResult(201, "应用不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("appInfo", appInfo);

        return getSuccessResult(result);
    }

    /**
     * 创建APP应用
     *
     * @param request HttpServletRequest对象
     * @param params 请求参数
     * @return 创建结果
     */
    @ApiOperation(value = "创建APP应用")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('system:app:add')")
    public ResponseObject create(HttpServletRequest request, @RequestBody Map<String, Object> params) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        String appId = params.get("appId") == null ? "" : params.get("appId").toString();
        String appName = params.get("appName") == null ? "" : params.get("appName").toString();
        String callbackUrl = params.get("callbackUrl") == null ? "" : params.get("callbackUrl").toString();
        String whiteList = params.get("whiteList") == null ? "" : params.get("whiteList").toString();
        String status = params.get("status") == null ? StatusEnum.ENABLED.getKey() : params.get("status").toString();

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        if (StringUtils.isEmpty(appName)) {
            return getFailureResult(400, "应用名称不能为空");
        }

        MtApp mtApp = new MtApp();
        if (StringUtils.isNotEmpty(appId)) {
            mtApp.setAppId(appId);
        }
        mtApp.setAppName(appName);
        mtApp.setCallbackUrl(callbackUrl);
        mtApp.setWhiteList(whiteList);
        mtApp.setStatus(status);

        MtApp result = appService.createApp(mtApp);

        Map<String, Object> data = new HashMap<>();
        data.put("appInfo", result);

        return getSuccessResult(data);
    }

    /**
     * 更新APP应用
     *
     * @param request HttpServletRequest对象
     * @param params 请求参数
     * @return 更新结果
     */
    @ApiOperation(value = "更新APP应用")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('system:app:edit')")
    public ResponseObject update(HttpServletRequest request, @RequestBody Map<String, Object> params) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        String id = params.get("id") == null ? "" : params.get("id").toString();
        String appId = params.get("appId") == null ? "" : params.get("appId").toString();
        String appName = params.get("appName") == null ? "" : params.get("appName").toString();
        String callbackUrl = params.get("callbackUrl") == null ? "" : params.get("callbackUrl").toString();
        String whiteList = params.get("whiteList") == null ? "" : params.get("whiteList").toString();
        String status = params.get("status") == null ? "" : params.get("status").toString();

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        if (StringUtils.isEmpty(id)) {
            return getFailureResult(400, "应用ID不能为空");
        }

        MtApp mtApp = appService.queryAppById(Long.parseLong(id));
        if (mtApp == null) {
            return getFailureResult(201, "应用不存在");
        }

        mtApp.setId(Long.parseLong(id));
        if (StringUtils.isNotEmpty(appId)) {
            mtApp.setAppId(appId);
        }
        if (StringUtils.isNotEmpty(appName)) {
            mtApp.setAppName(appName);
        }
        if (StringUtils.isNotEmpty(callbackUrl)) {
            mtApp.setCallbackUrl(callbackUrl);
        }
        if (params.containsKey("whiteList")) {
            mtApp.setWhiteList(whiteList);
        }
        if (StringUtils.isNotEmpty(status)) {
            mtApp.setStatus(status);
        }
        mtApp.setUpdateTime(new Date());

        MtApp result = appService.updateApp(mtApp);

        Map<String, Object> data = new HashMap<>();
        data.put("appInfo", result);

        return getSuccessResult(data);
    }

    /**
     * 更新APP应用状态
     *
     * @param request HttpServletRequest对象
     * @param params 请求参数
     * @return 更新结果
     */
    @ApiOperation(value = "更新APP应用状态")
    @RequestMapping(value = "/updateStatus", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('system:app:edit')")
    public ResponseObject updateStatus(HttpServletRequest request, @RequestBody Map<String, Object> params) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        String status = params.get("status") != null ? params.get("status").toString() : StatusEnum.ENABLED.getKey();
        String id = params.get("id") == null ? "" : params.get("id").toString();

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        if (StringUtils.isEmpty(id)) {
            return getFailureResult(400, "应用ID不能为空");
        }

        MtApp mtApp = appService.queryAppById(Long.parseLong(id));
        if (mtApp == null) {
            return getFailureResult(201, "应用不存在");
        }

        appService.updateStatus(Long.parseLong(id), status);

        return getSuccessResult(true);
    }

    /**
     * 删除/禁用APP应用
     *
     * @param request HttpServletRequest对象
     * @param params 请求参数
     * @return 删除结果
     */
    @ApiOperation(value = "删除/禁用APP应用")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('system:app:delete')")
    public ResponseObject delete(HttpServletRequest request, @RequestBody Map<String, Object> params) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        String id = params.get("id") == null ? "" : params.get("id").toString();

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        if (StringUtils.isEmpty(id)) {
            return getFailureResult(400, "应用ID不能为空");
        }

        MtApp mtApp = appService.queryAppById(Long.parseLong(id));
        if (mtApp == null) {
            return getFailureResult(201, "应用不存在");
        }

        Boolean result = appService.deleteApp(Long.parseLong(id));

        return getSuccessResult(result);
    }

    /**
     * 重置APP应用密钥
     *
     * @param request HttpServletRequest对象
     * @param params 请求参数
     * @return 新的密钥
     */
    @ApiOperation(value = "重置APP应用密钥")
    @RequestMapping(value = "/resetSecret", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('system:app:edit')")
    public ResponseObject resetSecret(HttpServletRequest request, @RequestBody Map<String, Object> params) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        String id = params.get("id") == null ? "" : params.get("id").toString();

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        if (StringUtils.isEmpty(id)) {
            return getFailureResult(400, "应用ID不能为空");
        }

        MtApp mtApp = appService.queryAppById(Long.parseLong(id));
        if (mtApp == null) {
            return getFailureResult(201, "应用不存在");
        }

        String newSecret = appService.resetAppSecret(Long.parseLong(id));

        Map<String, Object> result = new HashMap<>();
        result.put("appSecret", newSecret);

        return getSuccessResult(result);
    }

    /**
     * 更新白名单IP
     *
     * @param request HttpServletRequest对象
     * @param params 请求参数
     * @return 更新结果
     */
    @ApiOperation(value = "更新白名单IP")
    @RequestMapping(value = "/updateWhiteList", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('system:app:edit')")
    public ResponseObject updateWhiteList(HttpServletRequest request, @RequestBody Map<String, Object> params) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        String id = params.get("id") == null ? "" : params.get("id").toString();
        String whiteList = params.get("whiteList") == null ? "" : params.get("whiteList").toString();

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        if (StringUtils.isEmpty(id)) {
            return getFailureResult(400, "应用ID不能为空");
        }

        MtApp mtApp = appService.queryAppById(Long.parseLong(id));
        if (mtApp == null) {
            return getFailureResult(201, "应用不存在");
        }

        Boolean result = appService.updateWhiteList(Long.parseLong(id), whiteList);

        return getSuccessResult(result);
    }
}
