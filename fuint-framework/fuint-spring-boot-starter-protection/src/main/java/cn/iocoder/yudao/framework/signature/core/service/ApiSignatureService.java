package cn.iocoder.yudao.framework.signature.core.service;

/**
 * Foot-Fuint-Backend-master
 *
 * @author mjw
 * @since 2026/1/15 20:57
 */
public interface ApiSignatureService {

    /**
     * 验证白名单
     *
     * @param appId 应用ID
     * @param ip    IP地址
     * @return 是否在白名单中
     */
    boolean checkIpWhiteList(String appId, String ip);
}
