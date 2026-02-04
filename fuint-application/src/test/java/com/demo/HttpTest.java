package com.demo;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fuint.common.enums.TakeStatusEnum;
import com.fuint.framework.util.SeqUtil;
import com.fuint.framework.util.json.JsonUtils;
import com.fuint.repository.model.MtWebhookLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Miao
 * @date 2026/1/19
 */
@Slf4j
public class HttpTest {

    public static void main(String[] args) {

        String url = "https://dining-uat.anker-in.com";



        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("orderId", "1234567890");
        dataMap.put("storeId", "1");
        dataMap.put("merchantId", "1");
        dataMap.put("orderSn", "1234567890");

        dataMap.put("previousStatus", TakeStatusEnum.PROCESSING.getKey());
        dataMap.put("currentStatus", TakeStatusEnum.COMPLETED.getKey());
        dataMap.put("statusTime",DateUtil.format(new Date(), DatePattern.NORM_DATETIME_PATTERN));
        dataMap.put("remark", "Customer requested");
        // 2. 构造通用请求体
        String eventId = "evt_" + DateUtil.format(new Date(), "yyyyMMddHHmmssSSS") + SeqUtil.getUUID().substring(0, 6);
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", eventId);
        payload.put("eventType", "ORDER_TAKE_STATUS_CHANGE");
        payload.put("eventTime", DateUtil.now());
        payload.put("data", dataMap);



        String payloadJson = JsonUtils.toJsonString(payload);


        MtWebhookLog webhookLog = new MtWebhookLog();
        webhookLog.setCallbackUrl(url + "/api/openapi/coffee/callback/order-status");
        webhookLog.setEventType("ORDER_TAKE_STATUS_CHANGE");
        doSend(webhookLog, payloadJson, "/api/openapi/coffee/callback/order-status");
    }


    /**
     * 执行发送操作
     */
    public static void doSend(MtWebhookLog webhookLog, String payloadJson, String path) {
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

            // 生成curl命令
            String curlCommand = generateCurlCommand(fullUrl, payloadJson, accessKey, timestamp, nonce, signature);
            log.info("Generated curl command: {}", curlCommand);

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
            webhookLog.setTraceId(responseBodyJson.getString("traceId"));

            if (httpResponse.isOk()) {
                // 检查响应体中的 code 是否为 00000 (根据文档)
                // 这里简单判断 HTTP 200 即视为发送成功，业务层面的成功由对方保证
                webhookLog.setStatus(1); // 成功
                webhookLog.setErrorMsg(null);
                webhookLog.setNextRetryTime(null);
            } else {
//                handleFailure(webhookLog, "HTTP状态码异常: " + statusCode);
            }
            webhookLog.setUpdateTime(new Date());
//            webhookLogMapper.updateById(webhookLog);
        } catch (Exception e) {
            log.error("Webhook回调发送失败: eventType={}, error={}", eventType, e.getMessage());
//            handleFailure(webhookLog, e.getMessage());
            webhookLog.setUpdateTime(new Date());
//            webhookLogMapper.updateById(webhookLog);
        }
    }

    /**
     * 生成curl命令
     */
    public static String generateCurlCommand(String url, String payloadJson, String accessKey, String timestamp, String nonce, String signature) {
        StringBuilder curlCommand = new StringBuilder();
        curlCommand.append("curl -X POST ");
        curlCommand.append("\"").append(url).append("\" ");
        curlCommand.append("-H \"Content-Type: application/json\" ");
        curlCommand.append("-H \"X-Access-Key: ").append(accessKey).append("\" ");
        curlCommand.append("-H \"X-Timestamp: ").append(timestamp).append("\" ");
        curlCommand.append("-H \"X-Nonce: ").append(nonce).append("\" ");
        curlCommand.append("-H \"X-Signature: ").append(signature).append("\" ");
        curlCommand.append("-d '").append(payloadJson.replace("'", "'\"'\"'")).append("'");

        return curlCommand.toString();
    }
}
