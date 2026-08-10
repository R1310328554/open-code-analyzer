/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.client.auth.ram.utils;

import com.alibaba.nacos.client.auth.ram.RamConstants;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * CalculateV4SigningKeyUtil.
 * <p>阿里云 V4 签名派生密钥工具：按 aliyun_v4 规范对 Secret 做多轮 HMAC-SHA256 派生，输出 Base64 编码的最终签名密钥，供 MSE/Nacos RAM V4 鉴权使用。</p>
 *
 * @author xiweng.yy
 */
public class CalculateV4SigningKeyUtil {
    
    /** V4 签名密钥派生前缀 */
    private static final String PREFIX = "aliyun_v4";
    
    /** V4 派生链最后一轮 HMAC 的固定 payload */
    private static final String CONSTANT = "aliyun_v4_request";
    
    /** UTC 日期格式化器，格式 yyyyMMdd */
    private static final DateTimeFormatter V4_SIGN_DATE_FORMATTER =
        DateTimeFormatter.ofPattern("yyyyMMdd");
    
    /** V4 签名使用的 UTC 时区 */
    private static final ZoneId UTC_0 = ZoneId.of("GMT+00:00");
    
    /** 第一轮派生：HMAC(PREFIX+secret, date) */
    private static byte[] firstSigningKey(String secret, String date, String signMethod)
        throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(signMethod);
        mac.init(new SecretKeySpec((PREFIX + secret).getBytes(StandardCharsets.UTF_8), signMethod));
        return mac.doFinal(date.getBytes(StandardCharsets.UTF_8));
    }
    
    /** 第二轮派生：HMAC(firstKey, region) */
    private static byte[] regionSigningKey(String secret, String date, String region,
        String signMethod)
        throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] firstSignkey = firstSigningKey(secret, date, signMethod);
        Mac mac = Mac.getInstance(signMethod);
        mac.init(new SecretKeySpec(firstSignkey, signMethod));
        return mac.doFinal(region.getBytes(StandardCharsets.UTF_8));
    }
    
    /** 第三、四轮派生：经 productCode 与 aliyun_v4_request 得到最终签名密钥字节 */
    private static byte[] finalSigningKey(String secret, String date, String region,
        String productCode,
        String signMethod) {
        try {
            byte[] secondSignkey = regionSigningKey(secret, date, region, signMethod);
            Mac mac = Mac.getInstance(signMethod);
            mac.init(new SecretKeySpec(secondSignkey, signMethod));
            byte[] thirdSigningKey = mac.doFinal(productCode.getBytes(StandardCharsets.UTF_8));
            // 计算最终派生秘钥
            mac = Mac.getInstance(signMethod);
            mac.init(new SecretKeySpec(thirdSigningKey, signMethod));
            return mac.doFinal(CONSTANT.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("unsupported Algorithm:" + signMethod);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("InvalidKey");
        }
    }
    
    /**
     * Return V4 signature key with base64 encode.
     * <p>按完整参数派生 V4 签名密钥并 Base64 编码返回。</p>
     *
     * @param secret      secret key
     * @param date        date  with utc format, like 20211222
     * @param region      region id
     * @param productCode cloud product code
     * @param signMethod  sign method
     * @return V4 signature key with base64 encode
     */
    public static String finalSigningKeyString(String secret, String date, String region,
        String productCode,
        String signMethod) {
        return Base64.getEncoder()
            .encodeToString(finalSigningKey(secret, date, region, productCode, signMethod));
    }
    
    /**
     * Return V4 signature key with base64 encode for some default information.
     * <p>使用当前 UTC 日期、产品码 mse 与 HMAC-SHA256 的便捷派生入口。</p>
     *
     * <li>
     *     <ul>date = current date</ul>
     *     <ul>produceCode = mse</ul>
     *     <ul>signMethod = HMAC-SHA256</ul>
     * </li>
     *
     * @param secret secret key
     * @param region region id
     * @return V4 signature key with base64 encode
     */
    public static String finalSigningKeyStringWithDefaultInfo(String secret, String region) {
        String signDate = LocalDateTime.now(UTC_0).format(V4_SIGN_DATE_FORMATTER);
        return finalSigningKeyString(secret, signDate, region, RamConstants.SIGNATURE_V4_PRODUCE,
            RamConstants.SIGNATURE_V4_METHOD);
    }
}
