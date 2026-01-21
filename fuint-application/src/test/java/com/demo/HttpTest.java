package com.demo;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Miao
 * @date 2026/1/19
 */
public class HttpTest {

    public static void main(String[] args) {

        String appId = "1768548176";
        String appSecret = "vxmHbFs6HehTX7gLM2w";

        Long timestamp = System.currentTimeMillis();
        String nonceStr = "61bc32210a6e42fdb52886bbde996cf2";

        String str = "appId=" + appId + "&nonce=" + nonceStr + "&timestamp=" + timestamp + appSecret;

        String sign = DigestUtil.sha256Hex(str);

        Map<String, Object> header = new HashMap<>();
        header.put("appId", appId);
        header.put("timestamp", timestamp);
        header.put("nonce", nonceStr);
        header.put("signature", sign);

        String body = HttpRequest.get("http://localhost:7800/api/v1/goods/page?pageSize=10&page=1")
                .header("appId", appId)
                .header("timestamp", String.valueOf(timestamp))
                .header("nonce", nonceStr)
                .header("signature", sign)
                .execute()
                .body();
        System.out.println(body);
    }
}
