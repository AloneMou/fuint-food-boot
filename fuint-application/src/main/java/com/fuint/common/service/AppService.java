package com.fuint.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.repository.model.app.MtApp;

/**
 * 应用管理业务接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface AppService extends IService<MtApp> {

    /**
     * 分页查询应用列表
     *
     * @param paginationRequest 分页请求对象
     * @return 分页响应对象
     * @throws BusinessCheckException 业务异常
     */
    PaginationResponse<MtApp> queryAppListByPagination(PaginationRequest paginationRequest) throws BusinessCheckException;

    /**
     * 根据ID获取应用信息
     *
     * @param id 应用ID
     * @return 应用信息
     * @throws BusinessCheckException 业务异常
     */
    MtApp queryAppById(Long id) throws BusinessCheckException;

    /**
     * 根据应用ID获取应用信息
     *
     * @param appId 应用ID
     * @return 应用信息
     * @throws BusinessCheckException 业务异常
     */
    MtApp queryAppByAppId(String appId) throws BusinessCheckException;

    /**
     * 创建应用
     *
     * @param mtApp 应用对象
     * @return 应用信息
     * @throws BusinessCheckException 业务异常
     */
    MtApp createApp(MtApp mtApp) throws BusinessCheckException;

    /**
     * 更新应用
     *
     * @param mtApp 应用对象
     * @return 应用信息
     * @throws BusinessCheckException 业务异常
     */
    MtApp updateApp(MtApp mtApp) throws BusinessCheckException;

    /**
     * 更新应用状态
     *
     * @param id 应用ID
     * @param status 状态
     * @return 是否成功
     * @throws BusinessCheckException 业务异常
     */
    Boolean updateStatus(Long id, String status) throws BusinessCheckException;

    /**
     * 删除应用（软删除）
     *
     * @param id 应用ID
     * @return 是否成功
     * @throws BusinessCheckException 业务异常
     */
    Boolean deleteApp(Long id) throws BusinessCheckException;

    /**
     * 重置应用密钥
     *
     * @param id 应用ID
     * @return 新的密钥
     * @throws BusinessCheckException 业务异常
     */
    String resetAppSecret(Long id) throws BusinessCheckException;

    /**
     * 更新白名单IP
     *
     * @param id 应用ID
     * @param whiteList 白名单IP（逗号分隔）
     * @return 是否成功
     * @throws BusinessCheckException 业务异常
     */
    Boolean updateWhiteList(Long id, String whiteList) throws BusinessCheckException;

    /**
     * 验证应用ID和密钥
     *
     * @param appId 应用ID
     * @param appSecret 应用密钥
     * @return 是否验证通过
     */
    Boolean validateApp(String appId, String appSecret);

    /**
     * 检查IP是否在白名单中
     *
     * @param appId 应用ID
     * @param ip IP地址
     * @return 是否在白名单中
     */
    Boolean checkIpWhiteList(String appId, String ip) throws BusinessCheckException;
}
