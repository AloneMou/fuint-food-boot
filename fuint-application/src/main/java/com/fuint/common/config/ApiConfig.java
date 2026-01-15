package com.fuint.common.config;

import cn.iocoder.yudao.framework.signature.core.service.ApiSignatureService;
import com.fuint.common.service.AppService;
import com.fuint.framework.exception.BusinessCheckException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * Foot-Fuint-Backend-master
 *
 * @author mjw
 * @since 2026/1/15 21:06
 */
@Configuration
public class ApiConfig {

    @Resource
    private AppService appService;

    @Bean
    public ApiSignatureService apiSignatureService() {
        return (appId, ip) -> {
            try {
                return appService.checkIpWhiteList(appId, ip);
            } catch (BusinessCheckException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
