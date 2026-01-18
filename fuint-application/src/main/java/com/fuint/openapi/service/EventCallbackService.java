package com.fuint.openapi.service;

import com.alibaba.fastjson.JSON;
import com.fuint.common.enums.SettingTypeEnum;
import com.fuint.common.service.SettingService;
import com.fuint.framework.util.HttpUtil;
import com.fuint.framework.util.SeqUtil;
import com.fuint.repository.model.MtOrder;
import com.fuint.repository.model.MtSetting;
import com.fuint.repository.model.MtUserCoupon;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 事件回调服务
 * 统一管理所有OpenAPI事件回调
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Service
public class EventCallbackService {

    private static final Logger log = LoggerFactory.getLogger(EventCallbackService.class);

    @Resource
    private SettingService settingService;

    /**
     * 发送订单状态变更回调
     *
     * @param order 订单对象
     * @param oldStatus 旧状态（可为null）
     * @param newStatus 新状态
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

        sendCallback(order.getMerchantId(), "ORDER_STATUS_CHANGED", data);
    }

    /**
     * 发送订单支付状态变更回调
     *
     * @param order 订单对象
     * @param payStatus 支付状态（SUCCESS, REFUNDING, REFUNDED, FAILED）
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

        sendCallback(order.getMerchantId(), "PAYMENT_STATUS_CHANGED", data);
    }

    /**
     * 发送订单可取餐状态通知回调
     *
     * @param order 订单对象
     * @param items 可取餐的商品列表（可为null）
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

        sendCallback(order.getMerchantId(), "ORDER_READY", data);
    }

    /**
     * 发送用户优惠券事件回调
     *
     * @param userCoupon 用户优惠券对象
     * @param event 事件类型（RECEIVED, USED, EXPIRED, REVOKED）
     * @param orderNo 关联订单号（可为null）
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

        sendCallback(userCoupon.getMerchantId(), "COUPON_EVENT", data);
    }

    /**
     * 发送回调的通用方法
     *
     * @param merchantId 商户ID
     * @param eventType 事件类型
     * @param data 业务数据
     */
    private void sendCallback(Integer merchantId, String eventType, Map<String, Object> data) {
        try {
            // 1. 获取商户配置的回调地址
            MtSetting setting = settingService.querySettingByName(merchantId, SettingTypeEnum.ORDER.getKey(), "callback_url");
            if (setting == null || StringUtils.isEmpty(setting.getValue())) {
                log.debug("未配置回调地址，跳过回调发送: merchantId={}, eventType={}", merchantId, eventType);
                return;
            }

            String callbackUrl = setting.getValue();

            // 2. 构造回调报文
            Map<String, Object> payload = new HashMap<>();
            payload.put("eventId", SeqUtil.getUUID());
            payload.put("eventType", eventType);
            payload.put("eventTime", new Date());
            payload.put("data", data);

            // 3. 发送异步请求
            new Thread(() -> {
                try {
                    log.info("开始发送Webhook回调: url={}, eventType={}, payload={}", callbackUrl, eventType, JSON.toJSONString(payload));
                    URL url = new URL(callbackUrl);
                    String response = HttpUtil.sendRequest(url, JSON.toJSONString(payload), HttpUtil.Method.POST);
                    log.info("Webhook回调发送成功: eventType={}, response={}", eventType, response);
                } catch (Exception e) {
                    log.error("Webhook回调发送失败: eventType={}, error={}", eventType, e.getMessage(), e);
                }
            }).start();

        } catch (Exception e) {
            log.error("构造回调报文失败: eventType={}, error={}", eventType, e.getMessage(), e);
        }
    }
}
