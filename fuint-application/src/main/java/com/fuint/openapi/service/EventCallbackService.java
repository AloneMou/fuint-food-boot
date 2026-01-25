package com.fuint.openapi.service;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fuint.common.service.AppService;
import com.fuint.framework.util.json.JsonUtils;
import com.fuint.framework.util.SeqUtil;
import com.fuint.repository.mapper.MtWebhookLogMapper;
import com.fuint.repository.model.MtOrder;
import com.fuint.repository.model.MtUserCoupon;
import com.fuint.repository.model.MtWebhookLog;
import com.fuint.repository.model.app.MtApp;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 事件回调服务
 * 统一管理所有OpenAPI事件回调
 * <p>
 * 改进：
 * 1. 引入 Spring Event 事件驱动机制，解耦业务触发与回调执行。
 * 2. 使用自定义线程池替代 ThreadUtil，提供更好的并发控制。
 * 3. 增加重试机制，通过定时任务处理失败的回调。
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Service
public class EventCallbackService implements ApplicationEventPublisherAware, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(EventCallbackService.class);

    @Resource
    private AppService appService;

    @Resource
    private MtWebhookLogMapper webhookLogMapper;

    private ApplicationEventPublisher applicationEventPublisher;

    private ThreadPoolExecutor callbackExecutor;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @PostConstruct
    public void init() {
        // 初始化自定义线程池
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
        int maxPoolSize = corePoolSize * 2;
        int queueCapacity = 1000;
        
        callbackExecutor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                new ThreadFactory() {
                    private final AtomicInteger counter = new AtomicInteger(1);
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "event-callback-pool-" + counter.getAndIncrement());
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy() // 队列满时由调用者执行，防止丢失
        );
        log.info("EventCallbackService 线程池初始化完成: core={}, max={}, queue={}", corePoolSize, maxPoolSize, queueCapacity);
    }

    @Override
    public void destroy() {
        if (callbackExecutor != null) {
            callbackExecutor.shutdown();
            try {
                if (!callbackExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    callbackExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                callbackExecutor.shutdownNow();
            }
        }
    }

    // ==========================================
    // 事件定义 (Inner Classes)
    // ==========================================

    @Getter
    public static abstract class BaseCallbackEvent extends ApplicationEvent {
        private final Integer merchantId;
        private final String eventType;
        private final Map<String, Object> data;

        public BaseCallbackEvent(Object source, Integer merchantId, String eventType, Map<String, Object> data) {
            super(source);
            this.merchantId = merchantId;
            this.eventType = eventType;
            this.data = data;
        }
    }

    public static class OrderStatusChangedEvent extends BaseCallbackEvent {
        public OrderStatusChangedEvent(Object source, Integer merchantId, Map<String, Object> data) {
            super(source, merchantId, "ORDER_STATUS_CHANGED", data);
        }
    }

    public static class PaymentStatusChangedEvent extends BaseCallbackEvent {
        public PaymentStatusChangedEvent(Object source, Integer merchantId, Map<String, Object> data) {
            super(source, merchantId, "PAYMENT_STATUS_CHANGED", data);
        }
    }

    public static class OrderReadyEvent extends BaseCallbackEvent {
        public OrderReadyEvent(Object source, Integer merchantId, Map<String, Object> data) {
            super(source, merchantId, "ORDER_READY", data);
        }
    }

    public static class CouponEvent extends BaseCallbackEvent {
        public CouponEvent(Object source, Integer merchantId, Map<String, Object> data) {
            super(source, merchantId, "COUPON_EVENT", data);
        }
    }

    public static class CommentEvent extends BaseCallbackEvent {
        public CommentEvent(Object source, Integer merchantId, Map<String, Object> data) {
            super(source, merchantId, "COMMENT_EVENT", data);
        }
    }

    public static class MemberEvent extends BaseCallbackEvent {
        public MemberEvent(Object source, Integer merchantId, Map<String, Object> data) {
            super(source, merchantId, "MEMBER_EVENT", data);
        }
    }

    public static class BalanceEvent extends BaseCallbackEvent {
        public BalanceEvent(Object source, Integer merchantId, Map<String, Object> data) {
            super(source, merchantId, "BALANCE_EVENT", data);
        }
    }

    public static class PointEvent extends BaseCallbackEvent {
        public PointEvent(Object source, Integer merchantId, Map<String, Object> data) {
            super(source, merchantId, "POINT_EVENT", data);
        }
    }

    // ==========================================
    // 公共触发方法 (保持兼容性，改为发布事件)
    // ==========================================

    /**
     * 发送订单状态变更回调
     */
    public void sendOrderStatusChangedCallback(MtOrder order, String oldStatus, String newStatus) {
        if (order == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("orderNo", order.getOrderSn());
        data.put("userId", order.getUserId());
        data.put("merchantId", order.getMerchantId());
        data.put("storeId", order.getStoreId());
        data.put("newStatus", newStatus);
        if (oldStatus != null) {
            data.put("oldStatus", oldStatus);
        }
        
        applicationEventPublisher.publishEvent(new OrderStatusChangedEvent(this, order.getMerchantId(), data));
    }

    /**
     * 发送订单支付状态变更回调
     */
    public void sendPaymentStatusChangedCallback(MtOrder order, String payStatus) {
        if (order == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("orderNo", order.getOrderSn());
        data.put("userId", order.getUserId());
        data.put("merchantId", order.getMerchantId());
        data.put("storeId", order.getStoreId());
        data.put("payStatus", payStatus);
        data.put("payAmount", order.getPayAmount());

        applicationEventPublisher.publishEvent(new PaymentStatusChangedEvent(this, order.getMerchantId(), data));
    }

    /**
     * 发送订单可取餐状态通知回调
     */
    public void sendOrderReadyCallback(MtOrder order, Object items) {
        if (order == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("orderNo", order.getOrderSn());
        data.put("userId", order.getUserId());
        data.put("merchantId", order.getMerchantId());
        data.put("storeId", order.getStoreId());
        if (order.getVerifyCode() != null) {
            data.put("pickupCode", order.getVerifyCode());
        }
        if (items != null) {
            data.put("items", items);
        }
        
        applicationEventPublisher.publishEvent(new OrderReadyEvent(this, order.getMerchantId(), data));
    }

    /**
     * 发送用户优惠券事件回调
     */
    public void sendCouponEventCallback(MtUserCoupon userCoupon, String event, String orderNo) {
        if (userCoupon == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userCoupon.getUserId());
        data.put("couponId", userCoupon.getCouponId());
        data.put("couponCode", userCoupon.getCode());
        data.put("event", event);
        data.put("occurTime", new Date());
        if (orderNo != null) {
            data.put("orderNo", orderNo);
        }
        
        applicationEventPublisher.publishEvent(new CouponEvent(this, userCoupon.getMerchantId(), data));
    }

    /**
     * 发送评价事件回调
     */
    public void sendCommentEventCallback(Integer merchantId, Map<String, Object> data) {
        if (merchantId == null || data == null) return;
        applicationEventPublisher.publishEvent(new CommentEvent(this, merchantId, data));
    }

    /**
     * 发送会员事件回调
     */
    public void sendMemberEventCallback(Integer merchantId, Map<String, Object> data) {
        if (merchantId == null || data == null) return;
        applicationEventPublisher.publishEvent(new MemberEvent(this, merchantId, data));
    }

    /**
     * 发送余额变动事件回调
     */
    public void sendBalanceEventCallback(Integer merchantId, Map<String, Object> data) {
        if (merchantId == null || data == null) return;
        applicationEventPublisher.publishEvent(new BalanceEvent(this, merchantId, data));
    }

    /**
     * 发送积分变动事件回调
     */
    public void sendPointEventCallback(Integer merchantId, Map<String, Object> data) {
        if (merchantId == null || data == null) return;
        applicationEventPublisher.publishEvent(new PointEvent(this, merchantId, data));
    }

    // ==========================================
    // 事件监听与处理
    // ==========================================

    /**
     * 监听并处理所有回调事件
     */
    @EventListener
    public void handleCallbackEvent(BaseCallbackEvent event) {
        // 提交到线程池异步执行，避免阻塞发布线程
        callbackExecutor.submit(() -> processEvent(event));
    }

    /**
     * 实际处理事件逻辑
     */
    private void processEvent(BaseCallbackEvent event) {
        Integer merchantId = event.getMerchantId();
        String eventType = event.getEventType();
        Map<String, Object> data = event.getData();

        try {
            List<MtApp> appList = appService.getAvailableAppList();
            if (appList == null || appList.isEmpty()) {
                return;
            }

            // 1. 构造回调报文
            String eventId = SeqUtil.getUUID();
            Map<String, Object> payload = new HashMap<>();
            payload.put("eventId", eventId);
            payload.put("eventType", eventType);
            payload.put("eventTime", new Date());
            payload.put("merchantId", merchantId);
            payload.put("data", data);

            String payloadJson = JsonUtils.toJsonString(payload);

            for (MtApp app : appList) {
                String callbackUrl = app.getCallbackUrl();
                if (StringUtils.isBlank(callbackUrl)) {
                    continue;
                }

                // 2. 预先创建日志记录
                MtWebhookLog webhookLog = new MtWebhookLog();
                webhookLog.setEventId(eventId);
                webhookLog.setEventType(eventType);
                webhookLog.setMerchantId(merchantId);
                webhookLog.setAppId(app.getAppId());
                webhookLog.setCallbackUrl(callbackUrl);
                webhookLog.setRequestBody(payloadJson);
                webhookLog.setStatus(0); // 进行中
                webhookLog.setRetryCount(0);
                webhookLog.setCreateTime(new Date());
                webhookLogMapper.insert(webhookLog);

                // 3. 执行发送
                doSend(app, webhookLog, payloadJson);
            }
        } catch (Exception e) {
            log.error("处理回调事件失败: eventType={}, error={}", eventType, e.getMessage(), e);
        }
    }

    /**
     * 执行发送操作
     */
    public void doSend(MtApp app, MtWebhookLog webhookLog, String payloadJson) {
        String callbackUrl = app.getCallbackUrl();
        String eventType = webhookLog.getEventType();

        try {
            log.info("开始发送Webhook回调: url={}, eventType={}", callbackUrl, eventType);

            String nonce = IdUtil.fastSimpleUUID();
            String timestamp = String.valueOf(System.currentTimeMillis());

            SortedMap<String, String> headerMap = new TreeMap<>();
            headerMap.put("appId", app.getAppId());
            headerMap.put("timestamp", timestamp);
            headerMap.put("nonce", nonce);

            String serverSignatureString = MapUtil.join(headerMap, "&", "=") + app.getAppSecret();
            String signature = DigestUtil.sha256Hex(serverSignatureString);

            headerMap.put("signature", signature);

            HttpRequest request = HttpRequest.post(callbackUrl)
                    .body(payloadJson)
                    .header("appId", app.getAppId())
                    .header("timestamp", timestamp)
                    .header("nonce", nonce)
                    .header("signature", signature)
                    .timeout(10000); // 10秒超时

            cn.hutool.http.HttpResponse httpResponse = request.execute();
            String responseBody = httpResponse.body();
            int statusCode = httpResponse.getStatus();

            log.info("Webhook回调发送结果: eventType={}, status={}", eventType, statusCode);

            webhookLog.setRequestHeaders(JSON.toJSONString(headerMap));
            webhookLog.setResponseCode(statusCode);
            webhookLog.setResponseBody(StringUtils.substring(responseBody, 0, 2000)); // 截断避免过长

            if (httpResponse.isOk()) {
                webhookLog.setStatus(1); // 成功
                webhookLog.setErrorMsg(null);
                webhookLog.setNextRetryTime(null);
            } else {
                handleFailure(webhookLog, "HTTP状态码异常: " + statusCode);
            }
            webhookLog.setUpdateTime(new Date());
            webhookLogMapper.updateById(webhookLog);

        } catch (Exception e) {
            log.error("Webhook回调发送失败: eventType={}, error={}", eventType, e.getMessage());
            handleFailure(webhookLog, e.getMessage());
            webhookLog.setUpdateTime(new Date());
            webhookLogMapper.updateById(webhookLog);
        }
    }

    /**
     * 处理失败逻辑，计算下次重试时间
     */
    private void handleFailure(MtWebhookLog webhookLog, String errorMsg) {
        webhookLog.setErrorMsg(StringUtils.substring(errorMsg, 0, 500));
        webhookLog.setStatus(2); // 失败

        int retryCount = webhookLog.getRetryCount();
        if (retryCount < 3) {
            // 设置下次重试时间：1分钟, 5分钟, 15分钟
            long delay = (retryCount == 0) ? 60000 : (retryCount == 1 ? 300000 : 900000);
            webhookLog.setNextRetryTime(new Date(System.currentTimeMillis() + delay));
        } else {
            webhookLog.setNextRetryTime(null);
        }
    }

    // ==========================================
    // 定时任务 (重试机制)
    // ==========================================

    /**
     * 定时重试失败的回调
     * 每分钟执行一次
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void retryFailedCallbacks() {
        // 查询待重试的记录：status=2 AND next_retry_time <= now
        try {
            Date now = new Date();
            LambdaQueryWrapper<MtWebhookLog> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MtWebhookLog::getStatus, 2)
                    .le(MtWebhookLog::getNextRetryTime, now)
                    .orderByAsc(MtWebhookLog::getNextRetryTime)
                    .last("LIMIT 50"); // 每次处理50条，防止积压

            List<MtWebhookLog> retryLogs = webhookLogMapper.selectList(queryWrapper);
            if (retryLogs == null || retryLogs.isEmpty()) {
                return;
            }

            log.info("开始处理重试回调任务，数量: {}", retryLogs.size());

            for (MtWebhookLog logEntry : retryLogs) {
                // 提交到线程池执行
                callbackExecutor.submit(() -> doRetry(logEntry));
            }
        } catch (Exception e) {
            log.error("重试任务执行异常", e);
        }
    }

    private void doRetry(MtWebhookLog webhookLog) {
        try {
            // 获取App信息以获取密钥（如果需要重新签名，虽然payloadJson已经生成，但header需要重新生成）
            // 这里简化处理，直接获取App信息
            MtApp app = appService.getAvailableAppList().stream()
                    .filter(a -> a.getAppId().equals(webhookLog.getAppId()))
                    .findFirst()
                    .orElse(null);

            if (app == null) {
                webhookLog.setStatus(2);
                webhookLog.setErrorMsg("应用不存在或不可用，停止重试");
                webhookLog.setNextRetryTime(null);
                webhookLogMapper.updateById(webhookLog);
                return;
            }

            // 增加重试次数
            webhookLog.setRetryCount(webhookLog.getRetryCount() + 1);
            
            // 执行发送
            doSend(app, webhookLog, webhookLog.getRequestBody());

        } catch (Exception e) {
            log.error("执行重试失败: id={}", webhookLog.getId(), e);
        }
    }
}
