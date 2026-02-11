package com.fuint.framework.util.encrypt;

import cn.hutool.core.util.StrUtil;

/**
 * Foot-Fuint-Backend-master
 *
 * @author mjw
 * @since 2026/2/12 0:23
 */
public class PhoneUtils {
    private static final String PHONE_REGEX = "\\d{11}";

    /**
     * 加密手机号
     *
     * @param phone 明文手机号
     * @param key   AES密钥(16字节)
     * @return Base64编码的密文
     */
    public static String encrypt(String phone, String key) {
        if (phone == null || StrUtil.isBlank(phone)) {
            return phone;
        }
        return AESUtils.encrypt(phone, key);
    }

    /**
     * 解密手机号
     *
     * @param encryptedPhone Base64编码的密文
     * @param key            AES密钥(16字节)
     * @return 解密后的明文手机号，失败返回null
     */
    public static String decrypt(String encryptedPhone, String key) {
        if (encryptedPhone == null || StrUtil.isBlank(encryptedPhone)) {
            return null;
        }
        if (key == null || StrUtil.isBlank(key)) {
            return encryptedPhone;
        }
        try {
            return AESUtils.decrypt(encryptedPhone, key);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 标准化手机号
     * 去除格式符号和国际区号，提取标准11位手机号
     *
     * @param phone 原始手机号（可能包含区号、格式符号）
     * @return 标准化后的11位手机号
     */
    public static String normalize(String phone) {
        if (phone == null || StrUtil.isBlank(phone)) {
            return null;
        }
        // 1. 去除格式符号: 空格、-、(、)
        String cleaned = phone.replace(" ", "")
                .replace("-", "")
                .replace("(", "")
                .replace(")", "");

        // 2. 去除国际区号
        if (cleaned.startsWith("+86")) {
            cleaned = cleaned.substring(3);
        } else if (cleaned.startsWith("86") && cleaned.length() > 11) {
            cleaned = cleaned.substring(2);
        } else if (cleaned.startsWith("0086")) {
            cleaned = cleaned.substring(4);
        }

        cleaned = cleaned.trim();

        // 3. 校验是否为11位数字手机号
        if (cleaned.matches(PHONE_REGEX)) {
            return cleaned;
        }

        // 4. 尝试从字符串中提取11位数字
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(PHONE_REGEX).matcher(cleaned);
        if (matcher.find()) {
            return matcher.group();
        }
        return cleaned;
    }
}
