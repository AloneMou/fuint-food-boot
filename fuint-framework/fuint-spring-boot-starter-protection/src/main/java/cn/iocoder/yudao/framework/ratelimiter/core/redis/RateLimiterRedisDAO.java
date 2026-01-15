package cn.iocoder.yudao.framework.ratelimiter.core.redis;

import lombok.AllArgsConstructor;
import org.redisson.api.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.Duration;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 限流 Redis DAO
 *
 * @author 芋道源码
 */
@AllArgsConstructor
public class RateLimiterRedisDAO {

    /**
     * 限流操作
     * <p>
     * KEY 格式：rate_limiter:%s // 参数为 uuid
     * VALUE 格式：String
     * 过期时间：不固定
     */
    private static final String RATE_LIMITER = "rate_limiter:%s";

    private final RedissonClient redissonClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT;

    static {
        RATE_LIMIT_SCRIPT = new DefaultRedisScript<>();
        RATE_LIMIT_SCRIPT.setResultType(Long.class);
        RATE_LIMIT_SCRIPT.setScriptText(
                "local current = redis.call('incr', KEYS[1]);" +
                        "if current == 1 then redis.call('expire', KEYS[1], ARGV[2]); end;" +
                        "if current > tonumber(ARGV[1]) then return 0; end;" +
                        "return 1;"
        );
    }

    public Boolean tryAcquire(String key, int count, int time, TimeUnit timeUnit) {
        String redisKey = formatKey(key);
        long seconds = Math.max(1, timeUnit.toSeconds(time));

        try {
            Long result = redisTemplate.execute(
                    RATE_LIMIT_SCRIPT,
                    Collections.singletonList(redisKey),
                    count,
                    seconds
            );
            return result != null && result == 1;
        } catch (Exception e) {
            // Redis 异常 fail-open
            return true;
        }
    }

    private static String formatKey(String key) {
        return String.format(RATE_LIMITER, key);
    }

}
