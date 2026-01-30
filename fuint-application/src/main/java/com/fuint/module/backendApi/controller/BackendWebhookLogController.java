package com.fuint.module.backendApi.controller;

import com.fuint.common.Constants;
import com.fuint.common.dto.AccountInfo;
import com.fuint.common.service.AppService;
import com.fuint.common.service.WebhookLogService;
import com.fuint.common.util.TokenUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.web.BaseController;
import com.fuint.framework.web.ResponseObject;
import com.fuint.openapi.service.EventCallbackService;
import com.fuint.repository.model.app.MtApp;
import com.fuint.repository.model.MtWebhookLog;
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
 * Webhook回调日志管理控制器
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Api(tags="管理端-Webhook回调日志管理相关接口")
@RestController
@AllArgsConstructor
@RequestMapping(value = "/backendApi/webhookLog")
public class BackendWebhookLogController extends BaseController {

    /**
     * Webhook回调日志服务接口
     */
    private WebhookLogService webhookLogService;

    /**
     * 事件回调服务接口
     */
    private EventCallbackService eventCallbackService;

    /**
     * APP应用服务接口
     */
    private AppService appService;

    /**
     * 分页查询Webhook回调日志列表
     *
     * @param request HttpServletRequest对象
     * @return Webhook回调日志列表
     */
    @ApiOperation(value = "分页查询Webhook回调日志列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('system:webhookLog:list')")
    public ResponseObject list(HttpServletRequest request) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        Integer page = request.getParameter("page") == null ? Constants.PAGE_NUMBER : Integer.parseInt(request.getParameter("page"));
        Integer pageSize = request.getParameter("pageSize") == null ? Constants.PAGE_SIZE : Integer.parseInt(request.getParameter("pageSize"));
        String eventId = request.getParameter("eventId");
        String eventType = request.getParameter("eventType");
        String appId = request.getParameter("appId");
        String status = request.getParameter("status");
        String callbackUrl = request.getParameter("callbackUrl");

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        PaginationRequest paginationRequest = new PaginationRequest();
        paginationRequest.setCurrentPage(page);
        paginationRequest.setPageSize(pageSize);

        Map<String, Object> params = new HashMap<>();
        if (StringUtils.isNotEmpty(eventId)) {
            params.put("eventId", eventId);
        }
        if (StringUtils.isNotEmpty(eventType)) {
            params.put("eventType", eventType);
        }
        if (StringUtils.isNotEmpty(appId)) {
            params.put("appId", appId);
        }
        if (StringUtils.isNotEmpty(status)) {
            params.put("status", status);
        }
        if (StringUtils.isNotEmpty(callbackUrl)) {
            params.put("callbackUrl", callbackUrl);
        }
        
        paginationRequest.setSearchParams(params);
        PaginationResponse<com.fuint.common.dto.WebhookLogDto> paginationResponse = webhookLogService.queryWebhookLogListByPagination(paginationRequest);

        Map<String, Object> result = new HashMap<>();
        result.put("paginationResponse", paginationResponse);

        return getSuccessResult(result);
    }

    /**
     * 获取Webhook回调日志详情
     *
     * @param id 日志ID
     * @return Webhook回调日志详情
     */
    @ApiOperation(value = "获取Webhook回调日志详情")
    @RequestMapping(value = "/info/{id}", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('system:webhookLog:list')")
    public ResponseObject info(HttpServletRequest request, @PathVariable("id") Long id) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        MtWebhookLog webhookLogInfo = webhookLogService.queryWebhookLogById(id);
        if (webhookLogInfo == null) {
            return getFailureResult(201, "日志不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("webhookLogInfo", webhookLogInfo);

        return getSuccessResult(result);
    }

    /**
     * 创建Webhook回调日志
     *
     * @param request HttpServletRequest对象
     * @param params 请求参数
     * @return 创建结果
     */
    @ApiOperation(value = "创建Webhook回调日志")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('system:webhookLog:add')")
    public ResponseObject create(HttpServletRequest request, @RequestBody Map<String, Object> params) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        String eventId = params.get("eventId") == null ? "" : params.get("eventId").toString();
        String eventType = params.get("eventType") == null ? "" : params.get("eventType").toString();
        String appId = params.get("appId") == null ? "" : params.get("appId").toString();
        String callbackUrl = params.get("callbackUrl") == null ? "" : params.get("callbackUrl").toString();
        String requestBody = params.get("requestBody") == null ? "" : params.get("requestBody").toString();
        String statusStr = params.get("status") == null ? "0" : params.get("status").toString();
        Integer status = StringUtils.isNotEmpty(statusStr) ? Integer.parseInt(statusStr) : 0;

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        if (StringUtils.isEmpty(eventId)) {
            return getFailureResult(400, "事件ID不能为空");
        }

        MtWebhookLog mtWebhookLog = new MtWebhookLog();
        mtWebhookLog.setEventId(eventId);
        mtWebhookLog.setEventType(eventType);
        mtWebhookLog.setAppId(appId);
        mtWebhookLog.setCallbackUrl(callbackUrl);
        mtWebhookLog.setRequestBody(requestBody);
        mtWebhookLog.setStatus(status);

        MtWebhookLog result = webhookLogService.addWebhookLog(mtWebhookLog);

        Map<String, Object> data = new HashMap<>();
        data.put("webhookLogInfo", result);

        return getSuccessResult(data);
    }

    /**
     * 更新Webhook回调日志
     *
     * @param request HttpServletRequest对象
     * @param params 请求参数
     * @return 更新结果
     */
    @ApiOperation(value = "更新Webhook回调日志")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('system:webhookLog:edit')")
    public ResponseObject update(HttpServletRequest request, @RequestBody Map<String, Object> params) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        String idStr = params.get("id") == null ? "" : params.get("id").toString();
        String eventId = params.get("eventId") == null ? "" : params.get("eventId").toString();
        String eventType = params.get("eventType") == null ? "" : params.get("eventType").toString();
        String appId = params.get("appId") == null ? "" : params.get("appId").toString();
        String callbackUrl = params.get("callbackUrl") == null ? "" : params.get("callbackUrl").toString();
        String requestBody = params.get("requestBody") == null ? "" : params.get("requestBody").toString();
        String statusStr = params.get("status") == null ? "" : params.get("status").toString();
        String responseCodeStr = params.get("responseCode") == null ? "" : params.get("responseCode").toString();
        String retryCountStr = params.get("retryCount") == null ? "" : params.get("retryCount").toString();
        String errorMsg = params.get("errorMsg") == null ? "" : params.get("errorMsg").toString();

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        if (StringUtils.isEmpty(idStr)) {
            return getFailureResult(400, "日志ID不能为空");
        }

        Long id = Long.parseLong(idStr);
        MtWebhookLog mtWebhookLog = webhookLogService.queryWebhookLogById(id);
        if (mtWebhookLog == null) {
            return getFailureResult(201, "日志不存在");
        }

        mtWebhookLog.setId(id);
        if (StringUtils.isNotEmpty(eventId)) {
            mtWebhookLog.setEventId(eventId);
        }
        if (StringUtils.isNotEmpty(eventType)) {
            mtWebhookLog.setEventType(eventType);
        }
        if (StringUtils.isNotEmpty(appId)) {
            mtWebhookLog.setAppId(appId);
        }
        if (StringUtils.isNotEmpty(callbackUrl)) {
            mtWebhookLog.setCallbackUrl(callbackUrl);
        }
        if (StringUtils.isNotEmpty(requestBody)) {
            mtWebhookLog.setRequestBody(requestBody);
        }
        if (StringUtils.isNotEmpty(statusStr)) {
            mtWebhookLog.setStatus(Integer.parseInt(statusStr));
        }
        if (StringUtils.isNotEmpty(responseCodeStr)) {
            mtWebhookLog.setResponseCode(Integer.parseInt(responseCodeStr));
        }
        if (StringUtils.isNotEmpty(retryCountStr)) {
            mtWebhookLog.setRetryCount(Integer.parseInt(retryCountStr));
        }
        if (params.containsKey("errorMsg")) {
            mtWebhookLog.setErrorMsg(errorMsg);
        }
        mtWebhookLog.setUpdateTime(new Date());

        MtWebhookLog result = webhookLogService.updateWebhookLog(mtWebhookLog);

        Map<String, Object> data = new HashMap<>();
        data.put("webhookLogInfo", result);

        return getSuccessResult(data);
    }

    /**
     * 删除Webhook回调日志
     *
     * @param request HttpServletRequest对象
     * @param params 请求参数
     * @return 删除结果
     */
    @ApiOperation(value = "删除Webhook回调日志")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('system:webhookLog:delete')")
    public ResponseObject delete(HttpServletRequest request, @RequestBody Map<String, Object> params) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        String idStr = params.get("id") == null ? "" : params.get("id").toString();

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        if (StringUtils.isEmpty(idStr)) {
            return getFailureResult(400, "日志ID不能为空");
        }

        Long id = Long.parseLong(idStr);
        MtWebhookLog mtWebhookLog = webhookLogService.queryWebhookLogById(id);
        if (mtWebhookLog == null) {
            return getFailureResult(201, "日志不存在");
        }

        webhookLogService.deleteWebhookLog(id, accountInfo.getAccountName());

        return getSuccessResult(true);
    }

    /**
     * 重新发送Webhook回调
     *
     * @param request HttpServletRequest对象
     * @param params 请求参数
     * @return 重新发送结果
     */
    @ApiOperation(value = "重新发送Webhook回调")
    @RequestMapping(value = "/resend", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('system:webhookLog:resend')")
    public ResponseObject resend(HttpServletRequest request, @RequestBody Map<String, Object> params) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        String idStr = params.get("id") == null ? "" : params.get("id").toString();

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        if (StringUtils.isEmpty(idStr)) {
            return getFailureResult(400, "日志ID不能为空");
        }

        Long id = Long.parseLong(idStr);
        MtWebhookLog mtWebhookLog = webhookLogService.queryWebhookLogById(id);
        if (mtWebhookLog == null) {
            return getFailureResult(201, "日志不存在");
        }

        // 获取应用信息用于重新发送
        MtApp app = appService.queryAppByAppId(mtWebhookLog.getAppId());
        if (app == null) {
            return getFailureResult(202, "应用不存在");
        }
        
        // 使用事件回调服务重新发送
        eventCallbackService.doSend(app, mtWebhookLog, mtWebhookLog.getRequestBody(), mtWebhookLog.getRequestPath());

        return getSuccessResult(true);
    }
}