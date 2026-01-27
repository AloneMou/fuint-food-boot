package com.fuint.openapi.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fuint.common.enums.OrderStatusEnum;
import com.fuint.common.enums.TakeStatusEnum;
import com.fuint.common.service.AppService;
import com.fuint.framework.util.SeqUtil;
import com.fuint.framework.util.collection.MapUtils;
import com.fuint.framework.util.json.JsonUtils;
import com.fuint.repository.mapper.MtWebhookLogMapper;
import com.fuint.repository.model.MtOrder;
import com.fuint.repository.model.MtRefund;
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
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 事件回调服务
 * 统一管理所有OpenAPI事件回调
 * 根据《咖啡系统事件回调接入文档》重构
 * <p>
 * Created by FSQ
 * Refactored for Coffee System Integration
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
        // 原始数据对象，用于后续构建不同的 payload
        private final Object sourceData;
        private final Map<String, Object> extraData;

        public BaseCallbackEvent(Object source, Integer merchantId, String eventType, Object sourceData, Map<String, Object> extraData) {
            super(source);
            this.merchantId = merchantId;
            this.eventType = eventType;
            this.sourceData = sourceData;
            this.extraData = extraData;
        }
    }

    /**
     * 3.1 订单取餐状态变更 (ORDER_TAKE_STATUS_CHANGE)
     */
    public static class OrderTakeStatusEvent extends BaseCallbackEvent {
        public OrderTakeStatusEvent(Object source, Integer merchantId, MtOrder order, String previousStatus) {
            super(source, merchantId, "ORDER_TAKE_STATUS_CHANGE", order, MapUtil.of("previousStatus", previousStatus));
        }
    }

    /**
     * 3.1 订单状态变更 (ORDER_STATUS_CHANGE) - 文档中两个3.1，此为生命周期状态
     */
    public static class OrderStatusEvent extends BaseCallbackEvent {
        public OrderStatusEvent(Object source, Integer merchantId, MtOrder order, String previousStatus) {
            super(source, merchantId, "ORDER_STATUS_CHANGE", order, MapUtil.of("previousStatus", previousStatus));
        }
    }

    /**
     * 3.2 可取餐通知 (ORDER_READY)
     */
    public static class OrderReadyEvent extends BaseCallbackEvent {
        public OrderReadyEvent(Object source, Integer merchantId, MtOrder order) {
            super(source, merchantId, "ORDER_READY", order, null);
        }
    }

    /**
     * 3.3 退款状态变更 (PAY_STATUS_CHANGE)
     */
    public static class PayStatusEvent extends BaseCallbackEvent {
        public PayStatusEvent(Object source, Integer merchantId, MtRefund refund, MtOrder order) {
            super(source, merchantId, "REFUND_STATUS_CHANGE", refund, MapUtil.of("order", order));
        }
    }

    /**
     * 3.4 优惠券事件 (COUPON_EVENT)
     */
    public static class CouponEvent extends BaseCallbackEvent {
        public CouponEvent(Object source, Integer merchantId, MtUserCoupon userCoupon, String action, String orderId) {
            super(source, merchantId, "COUPON_EVENT", userCoupon, MapUtils.of("action", action, "orderId", orderId));
        }
    }

    // ==========================================
    // 公共触发方法
    // ==========================================

    /**
     * 发送订单取餐状态变更回调
     */
    public void sendOrderTakeStatusCallback(MtOrder order, String previousStatus) {
        if (order == null) return;
        if (StringUtils.isNotBlank(order.getTakeStatus())) {
            if (!order.getTakeStatus().equals(previousStatus)) {
                applicationEventPublisher.publishEvent(new OrderTakeStatusEvent(this, order.getMerchantId(), order, previousStatus));
            }
        }

    }

    /**
     * 发送订单生命周期状态变更回调
     */
    public void sendOrderStatusCallback(MtOrder order, String previousStatus) {
        if (order == null) return;
        if (!order.getStatus().equals(previousStatus)) {
            applicationEventPublisher.publishEvent(new OrderStatusEvent(this, order.getMerchantId(), order, previousStatus));
        }
    }

    /**
     * 发送订单可取餐状态通知回调
     */
    public void sendOrderReadyCallback(MtOrder order) {
        if (order == null) return;
        if (TakeStatusEnum.READY.getKey().equals(order.getTakeStatus())) {
            applicationEventPublisher.publishEvent(new OrderReadyEvent(this, order.getMerchantId(), order));
        }
    }

    /**
     * 发送退款状态变更回调
     */
    public void sendPayStatusCallback(MtOrder order, MtRefund refund) {
        if (order == null || refund == null) return;
        applicationEventPublisher.publishEvent(new PayStatusEvent(this, order.getMerchantId(), refund, order));
    }

    /**
     * 发送用户优惠券事件回调
     */
    public void sendCouponEventCallback(MtUserCoupon userCoupon, String action, String orderId) {
        if (userCoupon == null) return;
        applicationEventPublisher.publishEvent(new CouponEvent(this, userCoupon.getMerchantId(), userCoupon, action, orderId));
    }

    // ==========================================
    // 事件监听与处理
    // ==========================================

    /**
     * 监听并处理所有回调事件
     */
    @EventListener
    public void handleCallbackEvent(BaseCallbackEvent event) {
        // 提交到线程池异步执行
        callbackExecutor.submit(() -> processEvent(event));
    }

    /**
     * 实际处理事件逻辑
     */
    private void processEvent(BaseCallbackEvent event) {
        Integer merchantId = event.getMerchantId();
        String eventType = event.getEventType();

        try {
            List<MtApp> appList = appService.getAvailableAppList();
            if (appList == null || appList.isEmpty()) {
                return;
            }

            // 1. 根据事件类型构造具体的 data 数据
            Map<String, Object> dataMap = buildEventData(event);
            if (dataMap == null) {
                log.warn("构建事件数据失败，忽略事件: eventType={}, merchantId={}", eventType, merchantId);
                return;
            }

            // 2. 构造通用请求体
            String eventId = "evt_" + DateUtil.format(new Date(), "yyyyMMddHHmmssSSS") + SeqUtil.getUUID().substring(0, 6);
            Map<String, Object> payload = new HashMap<>();
            payload.put("eventId", eventId);
            payload.put("eventType", eventType);
            payload.put("eventTime", DateUtil.now());
            payload.put("data", dataMap);

            String payloadJson = JsonUtils.toJsonString(payload);

            for (MtApp app : appList) {
                String baseUrl = app.getCallbackUrl();
                if (StringUtils.isBlank(baseUrl)) {
                    continue;
                }

                // 确定具体的 API 路径
                String path = getPathByEventType(eventType);
                String fullUrl = buildFullUrl(baseUrl, path);

                // 3. 预先创建日志记录
                MtWebhookLog webhookLog = new MtWebhookLog();
                webhookLog.setEventId(eventId);
                webhookLog.setEventType(eventType);
                webhookLog.setMerchantId(merchantId);
                webhookLog.setAppId(app.getAppId());
                webhookLog.setCallbackUrl(fullUrl);
                webhookLog.setRequestBody(payloadJson);
                webhookLog.setStatus(0); // 进行中
                webhookLog.setRetryCount(0);
                webhookLog.setCreateTime(new Date());
                webhookLogMapper.insert(webhookLog);

                // 4. 执行发送
                doSend(app, webhookLog, payloadJson, path);
            }
        } catch (Exception e) {
            log.error("处理回调事件失败: eventType={}, error={}", eventType, e.getMessage(), e);
        }
    }

    /**
     * 根据事件类型构建 data 数据
     */
    private Map<String, Object> buildEventData(BaseCallbackEvent event) {
        Map<String, Object> data = new HashMap<>();
        String eventType = event.getEventType();
        Object source = event.getSourceData();
        Map<String, Object> extra = event.getExtraData();

        if (source instanceof MtOrder) {
            MtOrder order = (MtOrder) source;
            // 校验必要字段
            if (order.getId() == null || order.getMerchantId() == null) {
                log.warn("EventCallbackService: Order data incomplete, missing ID or MerchantID");
                return null;
            }

            data.put("orderId", order.getId());
            data.put("storeId", order.getStoreId());
            data.put("merchantId", order.getMerchantId());
            data.put("orderSn", order.getOrderSn());

            if ("ORDER_TAKE_STATUS_CHANGE".equals(eventType)) {
                data.put("previousStatus", extra.get("previousStatus"));
                data.put("currentStatus", order.getTakeStatus());
                data.put("statusTime", DateUtil.now());
                data.put("remark", order.getRemark());
            } else if ("ORDER_STATUS_CHANGE".equals(eventType)) {
                String prevStatusKey = (String) extra.get("previousStatus");
                data.put("previousStatus", getOrderStatusName(prevStatusKey));
                data.put("currentStatus", getOrderStatusName(order.getStatus()));
                data.put("statusTime", DateUtil.now());
                data.put("remark", order.getRemark());
            } else if ("ORDER_READY".equals(eventType)) {
                data.put("readyTime", DateUtil.now());
                data.put("pickupCode", order.getVerifyCode());
                data.put("estimatedWaitMinutes", 0); // 暂无数据，默认为0
            }
        } else if (source instanceof MtRefund) {
            MtRefund refund = (MtRefund) source;
            MtOrder order = (MtOrder) extra.get("order");

            if (refund.getId() == null || order == null || order.getId() == null) {
                log.warn("EventCallbackService: Refund data incomplete");
                return null;
            }

            data.put("orderId", order.getId());
            data.put("storeId", order.getStoreId());
            data.put("merchantId", order.getMerchantId());
            data.put("refundId", refund.getId());
            data.put("refundStatus", getRefundStatusName(refund.getStatus()));
            data.put("refundAmount", refund.getAmount());
            data.put("refundType", refund.getType());
            data.put("refundTime", DateUtil.formatDateTime(refund.getCreateTime()));
            data.put("reason", refund.getRemark());
        } else if (source instanceof MtUserCoupon) {
            MtUserCoupon coupon = (MtUserCoupon) source;

            if (coupon.getUserId() == null || coupon.getCouponId() == null) {
                log.warn("EventCallbackService: Coupon data incomplete");
                return null;
            }

            data.put("userId", coupon.getUserId());
            data.put("couponId", coupon.getCouponId());
            data.put("userCouponId", coupon.getId());
            data.put("action", extra.get("action"));
            data.put("orderId", extra.get("orderId"));
            data.put("actionTime", DateUtil.now());
        }

        return data.isEmpty() ? null : data;
    }

    private String getOrderStatusName(String key) {
        if (key == null) return null;
        OrderStatusEnum statusEnum = OrderStatusEnum.getEnum(key);
        return statusEnum != null ? statusEnum.name() : key;
    }

    private String getRefundStatusName(String key) {
        if (key == null) return "UNKNOWN";
        switch (key) {
            case "A":
                return "CREATED";
            case "B":
                return "APPROVED";
            case "C":
                return "REJECT";
            case "D":
                return "CANCEL";
            case "E":
                return "FAILED";
            default:
                return key;
        }
    }

    private String getPathByEventType(String eventType) {
        switch (eventType) {
            case "ORDER_TAKE_STATUS_CHANGE":
            case "ORDER_STATUS_CHANGE":
                return "/api/openapi/coffee/callback/order-status";
            case "ORDER_READY":
                return "/api/openapi/coffee/callback/order-ready";
            case "PAY_STATUS_CHANGE":
                return "/api/openapi/coffee/callback/pay-status";
            case "COUPON_EVENT":
                return "/api/openapi/coffee/callback/coupon-event";
//            case "INVOICE_RESULT":
//                return "/api/openapi/coffee/callback/invoice-result";
            default:
                return "";
        }
    }

    private String buildFullUrl(String baseUrl, String path) {
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return baseUrl + path;
    }

    /**
     * 执行发送操作
     */
    public void doSend(MtApp app, MtWebhookLog webhookLog, String payloadJson, String path) {
        String fullUrl = webhookLog.getCallbackUrl();
        String eventType = webhookLog.getEventType();

        try {
            log.info("开始发送Webhook回调: url={}, eventType={}", fullUrl, eventType);

            String nonce = IdUtil.simpleUUID(); // 32位
            String timestamp = String.valueOf(System.currentTimeMillis());
            String method = "POST";

            String accessKey = "GH";
            String secretKey = "wLiArcTnfH2N";
            // 签名算法：HMAC-SHA256
            // 签名字符串：{method}\n{path}\n{timestamp}\n{nonce}
            String signString = method + "\n" + path + "\n" + timestamp + "\n" + nonce;

            HMac mac = new HMac(HmacAlgorithm.HmacSHA256, secretKey.getBytes(StandardCharsets.UTF_8));
            String signature = mac.digestBase64(signString, false);

            HttpRequest request = HttpRequest.post(fullUrl)
                    .body(payloadJson)
                    .header("Content-Type", "application/json")
                    .header("X-Access-Key", accessKey)
                    .header("X-Timestamp", timestamp)
                    .header("X-Nonce", nonce)
                    .header("X-Signature", signature)
                    .timeout(10000);

            HttpResponse httpResponse = request.execute();
            String responseBody = httpResponse.body();
            int statusCode = httpResponse.getStatus();

            log.info("Webhook回调发送结果: eventType={}, status={}", eventType, statusCode);

            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("X-Access-Key", accessKey);
            headerMap.put("X-Timestamp", timestamp);
            headerMap.put("X-Nonce", nonce);
            headerMap.put("X-Signature", signature);

            webhookLog.setRequestHeaders(JSON.toJSONString(headerMap));
            webhookLog.setResponseCode(statusCode);
            webhookLog.setResponseBody(StringUtils.substring(responseBody, 0, 2000));

            JSONObject responseBodyJson = JSON.parseObject(responseBody);

            if (responseBodyJson.getBoolean("success") && "00000".equals(responseBodyJson.getString("code"))) {
                // 检查响应体中的 code 是否为 00000 (根据文档)
                // 这里简单判断 HTTP 200 即视为发送成功，业务层面的成功由对方保证
                webhookLog.setStatus(1); // 成功
                webhookLog.setErrorMsg(null);
                webhookLog.setNextRetryTime(null);
            } else {
                webhookLog.setTraceId(responseBodyJson.getString("traceId"));
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
            // 设置下次重试时间：1s, 5s, 30s (根据文档 5.2 重试策略建议)
            long delay;
            switch (retryCount) {
                case 0:
                    delay = 1000;
                    break;
                case 1:
                    delay = 5000;
                    break;
                case 2:
                    delay = 30000;
                    break;
                default:
                    delay = 30000;
            }
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
        try {
            log.info("开始执行重试回调任务");
            Date now = new Date();
            LambdaQueryWrapper<MtWebhookLog> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MtWebhookLog::getStatus, 2)
                    .le(MtWebhookLog::getNextRetryTime, now)
                    .orderByAsc(MtWebhookLog::getNextRetryTime)
                    .last("LIMIT 50");

            List<MtWebhookLog> retryLogs = webhookLogMapper.selectList(queryWrapper);
            if (retryLogs == null || retryLogs.isEmpty()) {
                log.info("没有需要重试的回调任务");
                return;
            }

            log.info("开始处理重试回调任务，数量: {}", retryLogs.size());

            for (MtWebhookLog logEntry : retryLogs) {
                callbackExecutor.submit(() -> doRetry(logEntry));
            }
        } catch (Exception e) {
            log.error("重试任务执行异常", e);
        }
    }

    private void doRetry(MtWebhookLog webhookLog) {
        try {
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

            webhookLog.setRetryCount(webhookLog.getRetryCount() + 1);

            // 重新计算 path
            String path = getPathByEventType(webhookLog.getEventType());

            doSend(app, webhookLog, webhookLog.getRequestBody(), path);

        } catch (Exception e) {
            log.error("执行重试失败: id={}", webhookLog.getId(), e);
        }
    }
}
