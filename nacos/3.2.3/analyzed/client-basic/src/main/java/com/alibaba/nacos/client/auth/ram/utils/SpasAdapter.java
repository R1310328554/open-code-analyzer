/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.client.auth.ram.identify.CredentialService;
import com.alibaba.nacos.common.codec.Base64;
import com.alibaba.nacos.common.utils.StringUtils;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * adapt spas interface.
 * <p>SPAS 签名适配器：根据资源串（tenant+group 等）与时间戳生成 Timestamp/Spas-Signature 请求头，并从 {@link com.alibaba.nacos.client.auth.ram.identify.CredentialService} 读取本地 AK/SK。</p>
 *
 * @author Nacos
 */
public class SpasAdapter {
    
    /** Spas 签名时间戳请求头名 */
    private static final String TIMESTAMP_HEADER = "Timestamp";
    
    /** Spas HMAC 签名请求头名 */
    private static final String SIGNATURE_HEADER = "Spas-Signature";
    
    /** 参数 Map 中 group 键名 */
    private static final String GROUP_KEY = "group";
    
    /** 参数 Map 中 tenant 键名 */
    public static final String TENANT_KEY = "tenant";
    
    /** HMAC-SHA1 算法标识 */
    private static final String SHA_ENCRYPT = "HmacSHA1";
    
    /** 按资源串与密钥生成 Timestamp + Spas-Signature 头；resource 为空时仅签时间戳 */
    public static Map<String, String> getSignHeaders(String resource, String secretKey) {
        Map<String, String> header = new HashMap<>(2);
        String timeStamp = String.valueOf(System.currentTimeMillis());
        header.put(TIMESTAMP_HEADER, timeStamp);
        if (secretKey != null) {
            String signature;
            if (StringUtils.isBlank(resource)) {
                signature = signWithHmacSha1Encrypt(timeStamp, secretKey);
            } else {
                signature = signWithHmacSha1Encrypt(resource + "+" + timeStamp, secretKey);
            }
            header.put(SIGNATURE_HEADER, signature);
        }
        return header;
    }
    
    /** 按 group/tenant 组合资源串后生成签名头；两者皆空返回 null */
    public static Map<String, String> getSignHeaders(String groupKey, String tenant,
        String secretKey) {
        if (StringUtils.isBlank(groupKey) && StringUtils.isBlank(tenant)) {
            return null;
        }
        
        String resource = "";
        if (StringUtils.isNotBlank(groupKey) && StringUtils.isNotBlank(tenant)) {
            resource = tenant + "+" + groupKey;
        } else {
            if (!StringUtils.isBlank(groupKey)) {
                resource = groupKey;
            }
        }
        return getSignHeaders(resource, secretKey);
    }
    
    /** 从参数 Map 提取 tenant/group 构造资源串并生成签名头 */
    public static Map<String, String> getSignHeaders(Map<String, String> paramValues,
        String secretKey) {
        if (null == paramValues) {
            return null;
        }
        
        String resource = "";
        if (paramValues.containsKey(TENANT_KEY) && paramValues.containsKey(GROUP_KEY)) {
            resource = paramValues.get(TENANT_KEY) + "+" + paramValues.get(GROUP_KEY);
        } else {
            if (!StringUtils.isBlank(paramValues.get(GROUP_KEY))) {
                resource = paramValues.get(GROUP_KEY);
            }
        }
        return getSignHeaders(resource, secretKey);
    }
    
    /** 从 CredentialService 获取 SecretKey */
    public static String getSk() {
        return CredentialService.getInstance().getCredential().getSecretKey();
    }
    
    /** 从 CredentialService 获取 AccessKey */
    public static String getAk() {
        return CredentialService.getInstance().getCredential().getAccessKey();
    }
    
    /** 释放 CredentialService 单例，便于测试或热更新凭证 */
    public static void freeCredentialInstance() {
        CredentialService.freeInstance();
    }
    
    /**
     * Sign with hmac SHA1 encrtpt.
     * <p>使用 HmacSHA1 对明文签名并 Base64 编码。</p>
     *
     * @param encryptText encrypt text
     * @param encryptKey  encrypt key
     * @return base64 string
     */
    public static String signWithHmacSha1Encrypt(String encryptText, String encryptKey) {
        try {
            byte[] data = encryptKey.getBytes(Constants.ENCODE);
            // 根据密钥字节与算法名构造 SecretKeySpec
            SecretKey secretKey = new SecretKeySpec(data, SHA_ENCRYPT);
            // 获取指定 HMAC 算法的 Mac 实例
            Mac mac = Mac.getInstance(SHA_ENCRYPT);
            // 使用密钥初始化 Mac
            mac.init(secretKey);
            byte[] text = encryptText.getBytes(Constants.ENCODE);
            byte[] textFinal = mac.doFinal(text);
            // 完成 HMAC 计算并 Base64 编码为字符串
            return new String(Base64.encodeBase64(textFinal), Constants.ENCODE);
        } catch (Exception e) {
            throw new RuntimeException("signWithhmacSHA1Encrypt fail", e);
        }
    }
}
