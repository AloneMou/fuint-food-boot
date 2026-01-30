package com.fuint.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuint.common.dto.WebhookLogDto;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.service.WebhookLogService;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.repository.mapper.MtWebhookLogMapper;
import com.fuint.repository.model.MtWebhookLog;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Webhook回调日志服务实现类
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Service
@AllArgsConstructor
public class WebhookLogServiceImpl extends ServiceImpl<MtWebhookLogMapper, MtWebhookLog> implements WebhookLogService {

    private MtWebhookLogMapper mtWebhookLogMapper;

    /**
     * 分页查询Webhook回调日志列表
     *
     * @param paginationRequest
     * @return
     */
    @Override
    public PaginationResponse<WebhookLogDto> queryWebhookLogListByPagination(PaginationRequest paginationRequest) throws BusinessCheckException {
        Page<MtWebhookLog> pageHelper = PageHelper.startPage(paginationRequest.getCurrentPage(), paginationRequest.getPageSize());
        LambdaQueryWrapper<MtWebhookLog> lambdaQueryWrapper = Wrappers.lambdaQuery();
        
        // 构建查询条件
        Map<String, Object> params = paginationRequest.getSearchParams();
        if (params != null) {
            String eventId = (String) params.get("eventId");
            String eventType = (String) params.get("eventType");
            String appId = (String) params.get("appId");
            String status = (String) params.get("status");
            String callbackUrl = (String) params.get("callbackUrl");

            if (StringUtils.isNotBlank(eventId)) {
                lambdaQueryWrapper.like(MtWebhookLog::getEventId, eventId);
            }
            if (StringUtils.isNotBlank(eventType)) {
                lambdaQueryWrapper.eq(MtWebhookLog::getEventType, eventType);
            }
            if (StringUtils.isNotBlank(appId)) {
                lambdaQueryWrapper.eq(MtWebhookLog::getAppId, appId);
            }
            if (StringUtils.isNotBlank(status)) {
                lambdaQueryWrapper.eq(MtWebhookLog::getStatus, Integer.parseInt(status));
            }
            if (StringUtils.isNotBlank(callbackUrl)) {
                lambdaQueryWrapper.like(MtWebhookLog::getCallbackUrl, callbackUrl);
            }
        }

        lambdaQueryWrapper.orderByDesc(MtWebhookLog::getCreateTime);
        List<MtWebhookLog> dataList = mtWebhookLogMapper.selectList(lambdaQueryWrapper);

        // 转换为DTO
        List<WebhookLogDto> dtoList = dataList.stream().map(item -> {
            WebhookLogDto dto = new WebhookLogDto();
            dto.setId(item.getId());
            dto.setEventId(item.getEventId());
            dto.setTraceId(item.getTraceId());
            dto.setEventType(item.getEventType());
            dto.setMerchantId(item.getMerchantId());
            dto.setAppId(item.getAppId());
            dto.setCallbackUrl(item.getCallbackUrl());
            dto.setRequestPath(item.getRequestPath());
            dto.setRequestHeaders(item.getRequestHeaders());
            dto.setRequestBody(item.getRequestBody());
            dto.setResponseCode(item.getResponseCode());
            dto.setResponseBody(item.getResponseBody());
            dto.setStatus(item.getStatus());
            dto.setRetryCount(item.getRetryCount());
            dto.setNextRetryTime(item.getNextRetryTime());
            dto.setErrorMsg(item.getErrorMsg());
            dto.setCreateTime(item.getCreateTime());
            dto.setUpdateTime(item.getUpdateTime());
            return dto;
        }).collect(java.util.stream.Collectors.toList());

        PaginationResponse<WebhookLogDto> paginationResponse = new PaginationResponse<>();
        paginationResponse.setContent(dtoList);
        paginationResponse.setTotalElements(pageHelper.getTotal());
        paginationResponse.setTotalPages(pageHelper.getPages());
        paginationResponse.setCurrentPage(paginationRequest.getCurrentPage());
        paginationResponse.setPageSize(paginationRequest.getPageSize());

        return paginationResponse;
    }

    /**
     * 添加Webhook回调日志
     *
     * @param mtWebhookLog
     * @throws BusinessCheckException
     */
    @Override
    @Transactional
    public MtWebhookLog addWebhookLog(MtWebhookLog mtWebhookLog) throws BusinessCheckException {
        mtWebhookLog.setCreateTime(new Date());
        mtWebhookLog.setUpdateTime(new Date());
        mtWebhookLogMapper.insert(mtWebhookLog);
        return mtWebhookLog;
    }

    /**
     * 根据ID获取Webhook回调日志信息
     *
     * @param id ID
     * @throws BusinessCheckException
     */
    @Override
    public MtWebhookLog queryWebhookLogById(Long id) throws BusinessCheckException {
        if (id == null) {
            throw new BusinessCheckException("ID不能为空");
        }
        return mtWebhookLogMapper.selectById(id);
    }

    /**
     * 更新Webhook回调日志
     *
     * @param mtWebhookLog
     * @throws BusinessCheckException
     */
    @Override
    @Transactional
    public MtWebhookLog updateWebhookLog(MtWebhookLog mtWebhookLog) throws BusinessCheckException {
        mtWebhookLog.setUpdateTime(new Date());
        mtWebhookLogMapper.updateById(mtWebhookLog);
        return mtWebhookLog;
    }

    /**
     * 删除Webhook回调日志
     *
     * @param id
     * @param operator
     * @throws BusinessCheckException
     */
    @Override
    @Transactional
    public void deleteWebhookLog(Long id, String operator) throws BusinessCheckException {
        if (id == null) {
            throw new BusinessCheckException("ID不能为空");
        }
        MtWebhookLog mtWebhookLog = mtWebhookLogMapper.selectById(id);
        if (mtWebhookLog == null) {
            throw new BusinessCheckException("Webhook回调日志不存在");
        }
        mtWebhookLogMapper.deleteById(id);
    }

    /**
     * 根据条件搜索Webhook回调日志
     * */
    @Override
    public List<MtWebhookLog> queryWebhookLogByParams(Map<String, Object> params) throws BusinessCheckException {
        LambdaQueryWrapper<MtWebhookLog> lambdaQueryWrapper = Wrappers.lambdaQuery();

        if (params != null) {
            String eventId = (String) params.get("eventId");
            String eventType = (String) params.get("eventType");
            String appId = (String) params.get("appId");
            String status = (String) params.get("status");
            String callbackUrl = (String) params.get("callbackUrl");

            if (StringUtils.isNotBlank(eventId)) {
                lambdaQueryWrapper.like(MtWebhookLog::getEventId, eventId);
            }
            if (StringUtils.isNotBlank(eventType)) {
                lambdaQueryWrapper.eq(MtWebhookLog::getEventType, eventType);
            }
            if (StringUtils.isNotBlank(appId)) {
                lambdaQueryWrapper.eq(MtWebhookLog::getAppId, appId);
            }
            if (StringUtils.isNotBlank(status)) {
                lambdaQueryWrapper.eq(MtWebhookLog::getStatus, Integer.parseInt(status));
            }
            if (StringUtils.isNotBlank(callbackUrl)) {
                lambdaQueryWrapper.like(MtWebhookLog::getCallbackUrl, callbackUrl);
            }
        }

        lambdaQueryWrapper.orderByDesc(MtWebhookLog::getCreateTime);
        return mtWebhookLogMapper.selectList(lambdaQueryWrapper);
    }
}