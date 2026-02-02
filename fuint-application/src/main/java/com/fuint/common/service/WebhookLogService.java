package com.fuint.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fuint.common.dto.WebhookLogDto;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.repository.model.MtWebhookLog;
import java.util.Map;

/**
 * Webhook回调日志服务接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface WebhookLogService extends IService<MtWebhookLog> {

    /**
     * 分页查询Webhook回调日志列表
     *
     * @param paginationRequest
     * @return
     */
    PaginationResponse<WebhookLogDto> queryWebhookLogListByPagination(PaginationRequest paginationRequest) throws BusinessCheckException;

    /**
     * 添加Webhook回调日志
     *
     * @param mtWebhookLog
     * @throws BusinessCheckException
     */
    MtWebhookLog addWebhookLog(MtWebhookLog mtWebhookLog) throws BusinessCheckException;

    /**
     * 根据ID获取Webhook回调日志信息
     *
     * @param id ID
     * @throws BusinessCheckException
     */
    MtWebhookLog queryWebhookLogById(Long id) throws BusinessCheckException;

    /**
     * 更新Webhook回调日志
     *
     * @param mtWebhookLog
     * @throws BusinessCheckException
     */
    MtWebhookLog updateWebhookLog(MtWebhookLog mtWebhookLog) throws BusinessCheckException;

    /**
     * 删除Webhook回调日志
     *
     * @param id
     * @param operator
     * @return
     * */
    void deleteWebhookLog(Long id, String operator) throws BusinessCheckException;

    /**
     * 根据条件搜索Webhook回调日志
     * */
    java.util.List<MtWebhookLog> queryWebhookLogByParams(Map<String, Object> params) throws BusinessCheckException;
}