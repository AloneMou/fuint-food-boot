package com.fuint.module.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fuint.common.service.AppService;
import com.fuint.openapi.service.EventCallbackService;
import com.fuint.repository.mapper.MtWebhookLogMapper;
import com.fuint.repository.model.MtWebhookLog;
import com.fuint.repository.model.app.MtApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Webhook重试任务
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@EnableScheduling
@Component("webhookRetryJob")
public class WebhookRetryJob {

    private static final Logger log = LoggerFactory.getLogger(WebhookRetryJob.class);

    @Autowired
    private MtWebhookLogMapper webhookLogMapper;

    @Autowired
    private EventCallbackService eventCallbackService;

    @Autowired
    private AppService appService;

    /**
     * 定时重试失败的Webhook回调
     * 每分钟执行一次
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void retryWebhooks() {
        log.info("WebhookRetryJob Start!!!");

        // 查询状态为失败（2）且重试次数小于3，且到了重试时间的记录
        LambdaQueryWrapper<MtWebhookLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MtWebhookLog::getStatus, 2)
                .lt(MtWebhookLog::getRetryCount, 3)
                .le(MtWebhookLog::getNextRetryTime, new Date());

        List<MtWebhookLog> logs = webhookLogMapper.selectList(queryWrapper);

        if (logs == null || logs.isEmpty()) {
            log.info("No webhooks need retry.");
            return;
        }

        for (MtWebhookLog webhookLog : logs) {
            try {
                log.info("Retrying Webhook: id={}, eventId={}, retryCount={}",
                        webhookLog.getId(), webhookLog.getEventId(), webhookLog.getRetryCount());

                MtApp app = appService.queryAppByAppId(webhookLog.getAppId());
                if (app == null) {
                    log.error("App not found for appId: {}", webhookLog.getAppId());
                    webhookLog.setStatus(2);
                    webhookLog.setRetryCount(3); // 停止重试
                    webhookLog.setErrorMsg("应用不存在");
                    webhookLogMapper.updateById(webhookLog);
                    continue;
                }

                // 增加重试次数
                webhookLog.setRetryCount(webhookLog.getRetryCount() + 1);
                webhookLogMapper.updateById(webhookLog);

                // 执行发送
                eventCallbackService.doSend(app, webhookLog, webhookLog.getRequestBody());

            } catch (Exception e) {
                log.error("Error retrying webhook {}: {}", webhookLog.getId(), e.getMessage());
            }
        }

        log.info("WebhookRetryJob End!!!");
    }
}
