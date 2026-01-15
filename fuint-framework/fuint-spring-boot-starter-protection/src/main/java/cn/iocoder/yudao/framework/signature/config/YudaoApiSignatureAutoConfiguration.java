package cn.iocoder.yudao.framework.signature.config;

import cn.iocoder.yudao.framework.redis.config.YudaoRedisAutoConfiguration;
import cn.iocoder.yudao.framework.signature.core.aop.ApiSignatureAspect;
import cn.iocoder.yudao.framework.signature.core.redis.ApiSignatureRedisDAO;
import cn.iocoder.yudao.framework.signature.core.service.ApiSignatureService;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * HTTP API 签名的自动配置类
 *
 * @author Zhougang
 */
@Configuration
@AutoConfigureAfter(YudaoRedisAutoConfiguration.class)
public class YudaoApiSignatureAutoConfiguration {

    @Bean
    public ApiSignatureAspect signatureAspect(ApiSignatureRedisDAO signatureRedisDAO, ApiSignatureService signatureService) {
        return new ApiSignatureAspect(signatureRedisDAO, signatureService);
    }

    @Bean
    public ApiSignatureRedisDAO signatureRedisDAO(StringRedisTemplate stringRedisTemplate) {
        return new ApiSignatureRedisDAO(stringRedisTemplate);
    }

}
