package com.fuint.common.service.impl;

import cn.iocoder.yudao.framework.signature.core.redis.ApiSignatureRedisDAO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.service.AppService;
import com.fuint.framework.annoation.OperationServiceLog;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.util.spring.SpringUtils;
import com.fuint.repository.mapper.MtAppMapper;
import com.fuint.repository.model.app.MtApp;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.AllArgsConstructor;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 应用管理业务实现类
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Service
@AllArgsConstructor
public class AppServiceImpl extends ServiceImpl<MtAppMapper, MtApp> implements AppService {

    private static final Logger logger = LoggerFactory.getLogger(AppServiceImpl.class);

    private MtAppMapper mtAppMapper;

    private ApiSignatureRedisDAO apiSignatureRedisDAO;

    @PostConstruct
    public void init() {
        try {
            List<MtApp> appList = mtAppMapper.selectList(null);
            for (MtApp app : appList) {
                apiSignatureRedisDAO.setAppSecret(app.getAppId(), app.getAppSecret());
            }
        } catch (Exception e) {
            logger.error("初始化应用信息异常", e);
        }
    }

    /**
     * 分页查询应用列表
     *
     * @param paginationRequest 分页请求对象
     * @return 分页响应对象
     * @throws BusinessCheckException 业务异常
     */
    @Override
    public PaginationResponse<MtApp> queryAppListByPagination(PaginationRequest paginationRequest) throws BusinessCheckException {
        Page<MtApp> pageHelper = PageHelper.startPage(paginationRequest.getCurrentPage(), paginationRequest.getPageSize());
        LambdaQueryWrapper<MtApp> lambdaQueryWrapper = Wrappers.lambdaQuery();

        // 不查询已删除的数据
        lambdaQueryWrapper.ne(MtApp::getStatus, StatusEnum.DISABLE.getKey());

        // 应用名称模糊查询
        String appName = paginationRequest.getSearchParams().get("appName") == null ? "" : paginationRequest.getSearchParams().get("appName").toString();
        if (StringUtils.isNotBlank(appName)) {
            lambdaQueryWrapper.like(MtApp::getAppName, appName);
        }

        // 应用ID精确查询
        String appId = paginationRequest.getSearchParams().get("appId") == null ? "" : paginationRequest.getSearchParams().get("appId").toString();
        if (StringUtils.isNotBlank(appId)) {
            lambdaQueryWrapper.eq(MtApp::getAppId, appId);
        }

        // 状态查询
        String status = paginationRequest.getSearchParams().get("status") == null ? "" : paginationRequest.getSearchParams().get("status").toString();
        if (StringUtils.isNotBlank(status)) {
            lambdaQueryWrapper.eq(MtApp::getStatus, status);
        }

        // 按ID倒序排列
        lambdaQueryWrapper.orderByDesc(MtApp::getId);

        List<MtApp> appList = mtAppMapper.selectList(lambdaQueryWrapper);

        PageRequest pageRequest = PageRequest.of(paginationRequest.getCurrentPage(), paginationRequest.getPageSize());
        PageImpl pageImpl = new PageImpl(appList, pageRequest, pageHelper.getTotal());
        PaginationResponse<MtApp> paginationResponse = new PaginationResponse(pageImpl, MtApp.class);
        paginationResponse.setTotalPages(pageHelper.getPages());
        paginationResponse.setTotalElements(pageHelper.getTotal());
        paginationResponse.setContent(appList);

        return paginationResponse;
    }

    /**
     * 根据ID获取应用信息
     *
     * @param id 应用ID
     * @return 应用信息
     * @throws BusinessCheckException 业务异常
     */
    @Override
    @Cacheable(value = "APP_CACHE", key = "#id")
    public MtApp queryAppById(Long id) throws BusinessCheckException {
        MtApp mtApp = mtAppMapper.selectById(id);
        if (mtApp == null) {
            throw new BusinessCheckException("该应用不存在");
        }
        return mtApp;
    }

    /**
     * 根据应用ID获取应用信息
     *
     * @param appId 应用ID
     * @return 应用信息
     * @throws BusinessCheckException 业务异常
     */
    @Override
    @Cacheable(value = "APP_CACHE", key = "'REQUEST_'+#appId")
    public MtApp queryAppByAppId(String appId) throws BusinessCheckException {
        if (StringUtils.isEmpty(appId)) {
            throw new BusinessCheckException("应用ID不能为空");
        }
        return mtAppMapper.findByAppId(appId);
    }

    /**
     * 创建应用
     *
     * @param mtApp 应用对象
     * @return 应用信息
     * @throws BusinessCheckException 业务异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "创建应用")
    @CacheEvict(value = "APP_CACHE", allEntries = true)
    public MtApp createApp(MtApp mtApp) throws BusinessCheckException {
        // 参数验证
        if (StringUtils.isEmpty(mtApp.getAppName())) {
            throw new BusinessCheckException("应用名称不能为空");
        }

        // 检查应用ID是否已存在
        if (StringUtils.isNotEmpty(mtApp.getAppId())) {
            MtApp existApp = mtAppMapper.findByAppId(mtApp.getAppId());
            if (existApp != null) {
                throw new BusinessCheckException("应用ID已存在");
            }
        } else {
            // 自动生成应用ID
            mtApp.setAppId(generateAppId());
        }

        // 生成应用密钥
        if (StringUtils.isEmpty(mtApp.getAppSecret())) {
            mtApp.setAppSecret(generateAppSecret());
        }

        // 设置创建时间和更新时间
        Date now = new Date();
        mtApp.setCreateTime(now);
        mtApp.setUpdateTime(now);

        // 设置默认状态
        if (mtApp.getStatus() == null) {
            mtApp.setStatus(StatusEnum.ENABLED.getKey());
        }

        mtAppMapper.insert(mtApp);
        apiSignatureRedisDAO.setAppSecret(mtApp.getAppId(), mtApp.getAppSecret());
        logger.info("创建应用成功，应用ID：{}", mtApp.getAppId());
        return mtApp;
    }

    /**
     * 更新应用
     *
     * @param mtApp 应用对象
     * @return 应用信息
     * @throws BusinessCheckException 业务异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "更新应用")
    @CacheEvict(value = "APP_CACHE", allEntries = true)
    public MtApp updateApp(MtApp mtApp) throws BusinessCheckException {
        if (mtApp.getId() == null) {
            throw new BusinessCheckException("应用ID不能为空");
        }

        MtApp existApp = mtAppMapper.selectById(mtApp.getId());
        if (existApp == null) {
            throw new BusinessCheckException("该应用不存在");
        }

        // 检查应用ID是否与其他应用重复
        if (StringUtils.isNotEmpty(mtApp.getAppId()) && !mtApp.getAppId().equals(existApp.getAppId())) {
            MtApp checkApp = mtAppMapper.findByAppId(mtApp.getAppId());
            if (checkApp != null && !checkApp.getId().equals(mtApp.getId())) {
                throw new BusinessCheckException("应用ID已被其他应用使用");
            }
        }

        // 更新时间
        mtApp.setUpdateTime(new Date());

        mtAppMapper.updateById(mtApp);
        if (!mtApp.getAppSecret().equals(existApp.getAppSecret())) {
            apiSignatureRedisDAO.refreshAppSecret(mtApp.getAppId(), mtApp.getAppSecret());
        }
        if (!mtApp.getAppId().equals(existApp.getAppId())) {
            apiSignatureRedisDAO.deleteAppSecret(existApp.getAppId());
            apiSignatureRedisDAO.setAppSecret(mtApp.getAppId(), mtApp.getAppSecret());
        }
        logger.info("更新应用成功，应用ID：{}", mtApp.getId());

        return mtAppMapper.selectById(mtApp.getId());
    }

    /**
     * 更新应用状态
     *
     * @param id     应用ID
     * @param status 状态
     * @return 是否成功
     * @throws BusinessCheckException 业务异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "更新应用状态")
    @CacheEvict(value = "APP_CACHE", allEntries = true)
    public Boolean updateStatus(Long id, String status) throws BusinessCheckException {
        MtApp mtApp = mtAppMapper.selectById(id);
        if (mtApp == null) {
            throw new BusinessCheckException("该应用不存在");
        }

        mtApp.setStatus(status);
        mtApp.setUpdateTime(new Date());

        int result = mtAppMapper.updateById(mtApp);
        if (status.equals(StatusEnum.ENABLED.getKey())) {
            apiSignatureRedisDAO.refreshAppSecret(mtApp.getAppId(), mtApp.getAppSecret());
        } else {
            apiSignatureRedisDAO.deleteAppSecret(mtApp.getAppId());
        }
        logger.info("更新应用状态成功，应用ID：{}，状态：{}", id, status);
        return result > 0;
    }

    /**
     * 删除应用（软删除）
     *
     * @param id 应用ID
     * @return 是否成功
     * @throws BusinessCheckException 业务异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "删除应用")
    @CacheEvict(value = "APP_CACHE", allEntries = true)
    public Boolean deleteApp(Long id) throws BusinessCheckException {
        MtApp mtApp = mtAppMapper.selectById(id);
        if (mtApp == null) {
            throw new BusinessCheckException("该应用不存在");
        }

        // 软删除：更新状态为禁用
        mtApp.setStatus(StatusEnum.DISABLE.getKey());
        mtApp.setUpdateTime(new Date());

        int result = mtAppMapper.updateById(mtApp);
        apiSignatureRedisDAO.deleteAppSecret(mtApp.getAppId());
        logger.info("删除应用成功，应用ID：{}", id);

        return result > 0;
    }

    /**
     * 重置应用密钥
     *
     * @param id 应用ID
     * @return 新的密钥
     * @throws BusinessCheckException 业务异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "重置应用密钥")
    @CacheEvict(value = "APP_CACHE", allEntries = true)
    public String resetAppSecret(Long id) throws BusinessCheckException {
        MtApp mtApp = mtAppMapper.selectById(id);
        if (mtApp == null) {
            throw new BusinessCheckException("该应用不存在");
        }

        // 生成新的密钥
        String newSecret = generateAppSecret();

        // 更新密钥
        mtAppMapper.resetAppSecret(id, newSecret);
        apiSignatureRedisDAO.setAppSecret(mtApp.getAppId(), newSecret);
        return newSecret;
    }

    /**
     * 更新白名单IP
     *
     * @param id        应用ID
     * @param whiteList 白名单IP（逗号分隔）
     * @return 是否成功
     * @throws BusinessCheckException 业务异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "更新白名单IP")
    @CacheEvict(value = "APP_CACHE", allEntries = true)
    public Boolean updateWhiteList(Long id, String whiteList) throws BusinessCheckException {
        MtApp mtApp = mtAppMapper.selectById(id);
        if (mtApp == null) {
            throw new BusinessCheckException("该应用不存在");
        }

        mtApp.setWhiteList(whiteList);
        mtApp.setUpdateTime(new Date());

        int result = mtAppMapper.updateById(mtApp);
        logger.info("更新白名单IP成功，应用ID：{}", id);

        return result > 0;
    }

    /**
     * 验证应用ID和密钥
     *
     * @param appId     应用ID
     * @param appSecret 应用密钥
     * @return 是否验证通过
     */
    @Override
    public Boolean validateApp(String appId, String appSecret) {
        if (StringUtils.isEmpty(appId) || StringUtils.isEmpty(appSecret)) {
            return false;
        }

        MtApp mtApp = mtAppMapper.findByAppId(appId);
        if (mtApp == null) {
            logger.warn("应用验证失败，应用不存在，appId：{}", appId);
            return false;
        }

        // 检查应用状态
        if (!mtApp.getStatus().equals(StatusEnum.ENABLED.getKey())) {
            logger.warn("应用验证失败，应用已禁用，appId：{}", appId);
            return false;
        }

        // 验证密钥
        if (!mtApp.getAppSecret().equals(appSecret)) {
            logger.warn("应用验证失败，密钥错误，appId：{}", appId);
            return false;
        }

        return true;
    }

    /**
     * 检查IP是否在白名单中
     *
     * @param appId 应用ID
     * @param ip    IP地址
     * @return 是否在白名单中
     */
    @Override
    public Boolean checkIpWhiteList(String appId, String ip) throws BusinessCheckException {
        if (StringUtils.isEmpty(appId) || StringUtils.isEmpty(ip)) {
            return false;
        }
        MtApp mtApp = SpringUtils.getBean(AppService.class).queryAppByAppId(appId);
        if (mtApp == null) {
            return false;
        }
        // 如果未设置白名单，则允许所有IP访问
        if (StringUtils.isEmpty(mtApp.getWhiteList())) {
            return true;
        }

        // 检查IP是否在白名单中
        String[] ipList = mtApp.getWhiteList().split(",");
        for (String whiteIp : ipList) {
            if (whiteIp.trim().equals(ip) || whiteIp.trim().equals("*")) {
                return true;
            }
        }

        logger.warn("IP不在白名单中，appId：{}，ip：{}", appId, ip);
        return false;
    }

    @Override
    public List<MtApp> getAvailableAppList() {
        return mtAppMapper.selectAvailableAppList();
    }

    /**
     * 生成应用ID
     *
     * @return 应用ID
     */
    private String generateAppId() {
        return "APP" + System.currentTimeMillis();
    }

    /**
     * 生成应用密钥
     *
     * @return 应用密钥
     */
    private String generateAppSecret() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}
