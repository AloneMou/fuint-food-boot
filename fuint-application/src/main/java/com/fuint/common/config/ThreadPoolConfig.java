package com.fuint.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.*;

/**
 * 线程池配置类
 * 用于优化批量同步等耗时操作的性能
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Slf4j
@Configuration
@EnableAsync
public class ThreadPoolConfig {

    /**
     * 用户同步线程池
     * 用于批量用户数据同步操作
     *
     * @return 线程池执行器
     */
    @Bean(name = "userSyncExecutor")
    public ThreadPoolExecutor userSyncExecutor() {
        int corePoolSize = 5;  // 核心线程数
        int maximumPoolSize = 10;  // 最大线程数
        long keepAliveTime = 60L;  // 线程空闲时间
        TimeUnit unit = TimeUnit.SECONDS;  // 时间单位
        int queueCapacity = 200;  // 队列容量

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                unit,
                new LinkedBlockingQueue<>(queueCapacity),
                new ThreadFactory() {
                    private int threadNumber = 1;

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r, "user-sync-thread-" + threadNumber++);
                        thread.setDaemon(false);
                        return thread;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy()  // 拒绝策略：调用者运行
        );

        log.info("用户同步线程池初始化完成: corePoolSize={}, maximumPoolSize={}, queueCapacity={}",
                corePoolSize, maximumPoolSize, queueCapacity);

        return executor;
    }

    /**
     * 商品价格计算线程池
     * 用于优化商品列表动态价格计算性能
     *
     * @return 线程池执行器
     */
    @Bean(name = "goodsPriceExecutor")
    public ThreadPoolExecutor goodsPriceExecutor() {
        int corePoolSize = 10;  // 核心线程数
        int maximumPoolSize = 20;  // 最大线程数
        long keepAliveTime = 60L;  // 线程空闲时间
        TimeUnit unit = TimeUnit.SECONDS;  // 时间单位
        int queueCapacity = 500;  // 队列容量

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                unit,
                new LinkedBlockingQueue<>(queueCapacity),
                new ThreadFactory() {
                    private int threadNumber = 1;

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r, "goods-price-thread-" + threadNumber++);
                        thread.setDaemon(false);
                        return thread;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy()  // 拒绝策略：调用者运行
        );

        log.info("商品价格计算线程池初始化完成: corePoolSize={}, maximumPoolSize={}, queueCapacity={}",
                corePoolSize, maximumPoolSize, queueCapacity);

        return executor;
    }
}
