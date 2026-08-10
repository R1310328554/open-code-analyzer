/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.models.utils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * HMAC 一次性密码（OTP）工具：实现 RFC 4226 HOTP 的生成与验证。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class HmacOTP {
    /** HMAC-SHA1 算法标识。 */
    public static final String HMAC_SHA1 = "HmacSHA1";
    /** HMAC-SHA256 算法标识。 */
    public static final String HMAC_SHA256 = "HmacSHA256";
    /** HMAC-SHA512 算法标识。 */
    public static final String HMAC_SHA512 = "HmacSHA512";
    /** 默认 HMAC 算法。 */
    public static final String DEFAULT_ALGORITHM = HMAC_SHA1;
    /** 默认 OTP 位数。 */
    public static final int DEFAULT_NUMBER_DIGITS = 6;
    // 0 1 2 3 4 5 6 7 8
    private static final int[] DIGITS_POWER = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000};
    protected final String algorithm;
    protected final int numberDigits;
    protected final int lookAroundWindow;

    /** @param numberDigits OTP 位数
     * @param algorithm HMAC 算法
     * @param delayWindow 验证窗口大小 */
    public HmacOTP(int numberDigits, String algorithm, int delayWindow) {
        this.numberDigits = numberDigits;
        this.algorithm = algorithm;
        this.lookAroundWindow = delayWindow;
    }

    /** 生成随机 OTP 共享密钥字符串。
     * @param length 密钥长度 */
    public static String generateSecret(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVW1234567890";
        SecureRandom r = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char c = chars.charAt(r.nextInt(chars.length()));
            sb.append(c);
        }
        return sb.toString();
    }

    /** 基于字节密钥与计数器生成 HOTP。
     * @param key 共享密钥
     * @param counter 计数器值 */
    public String generateHOTP(byte[] key, int counter) {
        String steps = Integer.toHexString(counter).toUpperCase();

        // 补齐为 16 位十六进制字符串
        // Just get a 16 digit string
        while (steps.length() < 16)
            steps = "0" + steps;

        return generateOTP(key, steps, numberDigits, algorithm);

    }

    /** 基于字符串密钥与计数器生成 HOTP。 */
    public String generateHOTP(String key, int counter) {
        return generateHOTP(key.getBytes(), counter);
    }

    /**
     *
     * @param token
     * @param key
     * @param counter
     * @return -1 if not a match.  A positive number means successful validation.  This positive number is also the new value of the counter
     */
    /** 验证 HOTP 令牌（含窗口）。
     * @return 验证成功返回新计数器值，失败返回 -1 */
    public int validateHOTP(String token, byte[] key, int counter) {

        int newCounter = counter;
        for (newCounter = counter; newCounter <= counter + lookAroundWindow; newCounter++) {
            String candidate = generateHOTP(key, newCounter);
            if (token != null && MessageDigest.isEqual(candidate.getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8))) {
                return newCounter + 1;
            }

        }
        return -1;
    }

    /** 验证 HOTP 令牌（字符串密钥）。 */
    public int validateHOTP(String token, String key, int counter) {
        return validateHOTP(token, key.getBytes(), counter);
    }

    /**
     * 为给定参数生成 OTP 数值。
     * This method generates an OTP value for the given set of parameters.
     *
     * @param key          the shared secret, HEX encoded
     * @param counter         a value that reflects a time
     * @param returnDigits number of digits to return
     * @param crypto       the crypto function to use
     * @return A numeric String in base 10 that includes return digits
     * @throws java.security.GeneralSecurityException
     *
     */
    public String generateOTP(byte[] key, String counter, int returnDigits, String crypto) {
        String result = null;
        byte[] hash;

        // 使用计数器作为 moving factor
        // Using the counter
        // 前 8 字节为 moving factor
        // First 8 bytes are for the movingFactor
        // 符合 RFC 4226 HOTP 规范
        // Complaint with base RFC 4226 (HOTP)
        while (counter.length() < 16)
            counter = "0" + counter;

        // 将十六进制转为字节数组
        // Get the HEX in a Byte[]
        byte[] msg = hexStr2Bytes(counter);

        // 补字节以确保正确转换
        // Adding one byte to get the right conversion
        // byte[] k = hexStr2Bytes(key);

        hash = hmac_sha1(crypto, key, msg);

        // 从哈希中提取 4 字节生成 OTP
        // put selected bytes into result int
        int offset = hash[hash.length - 1] & 0xf;

        int binary = ((hash[offset] & 0x7f) << 24) | ((hash[offset + 1] & 0xff) << 16) | ((hash[offset + 2] & 0xff) << 8)
                | (hash[offset + 3] & 0xff);

        int otp = binary % DIGITS_POWER[returnDigits];

        result = Integer.toString(otp);

        while (result.length() < returnDigits) {
            result = "0" + result;
        }
        return result;
    }

    /**
     * 使用 JCE 计算 HMAC 消息认证码。
     * This method uses the JCE to provide the crypto algorithm. HMAC computes a Hashed Message Authentication Code with the
     * crypto hash algorithm as a parameter.
     *
     * @param crypto   the crypto algorithm (HmacSHA1, HmacSHA256, HmacSHA512)
     * @param keyBytes the bytes to use for the HMAC key
     * @param text     the message or text to be authenticated.
     * @throws java.security.NoSuchAlgorithmException
     *
     * @throws java.security.InvalidKeyException
     *
     */
    private byte[] hmac_sha1(String crypto, byte[] keyBytes, byte[] text) {
        byte[] value;

        try {
            Mac hmac = Mac.getInstance(crypto);
            SecretKeySpec macKey = new SecretKeySpec(keyBytes, "RAW");

            hmac.init(macKey);

            value = hmac.doFinal(text);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return value;
    }

    /**
     * 将十六进制字符串转换为字节数组。
     * This method converts HEX string to Byte[]
     *
     * @param hex the HEX string
     * @return A byte array
     */
    private byte[] hexStr2Bytes(String hex) {
        // Adding one byte to get the right conversion
        // values starting with "0" can be converted
        byte[] bArray = new BigInteger("10" + hex, 16).toByteArray();

        // Copy all the REAL bytes, not the "first"
        byte[] ret = new byte[bArray.length - 1];
        System.arraycopy(bArray, 1, ret, 0, ret.length);
        return ret;
    }
}
