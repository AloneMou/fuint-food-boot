package com.fuint.framework.util.encrypt;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Foot-Fuint-Backend-master
 *
 * @author mjw
 * @since 2026/2/12 0:23
 */
public class AESUtils {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String IV = "1234567890123456"; // 16字节初始化向量

    /**
     * AES加密
     *
     * @param content   明文内容
     * @param secretKey 密钥(必须16/24/32字节)
     * @return Base64编码的密文
     */
    public static String encrypt(String content, String secretKey) {
        if (content == null || secretKey == null) {
            throw new IllegalArgumentException("Content and secretKey cannot be null");
        }
        if (secretKey.length() != 16 && secretKey.length() != 24 && secretKey.length() != 32) {
            throw new IllegalArgumentException("SecretKey must be 16/24/32 bytes");
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("AES加密失败", e);
        }
    }

    /**
     * AES解密
     *
     * @param content   Base64编码的密文
     * @param secretKey 密钥(必须16/24/32字节)
     * @return 解密后的明文
     */
    public static String decrypt(String content, String secretKey) {
        if (content == null || secretKey == null) {
            throw new IllegalArgumentException("Content and secretKey cannot be null");
        }
        if (secretKey.length() != 16 && secretKey.length() != 24 && secretKey.length() != 32) {
            throw new IllegalArgumentException("SecretKey must be 16/24/32 bytes");
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decoded = Base64.getDecoder().decode(content);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES解密失败", e);
        }
    }
}
