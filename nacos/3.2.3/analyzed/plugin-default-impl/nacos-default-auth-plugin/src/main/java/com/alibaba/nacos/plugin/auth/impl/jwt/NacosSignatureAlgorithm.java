/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.auth.impl.jwt;

import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.auth.exception.AccessException;
import com.alibaba.nacos.plugin.auth.impl.users.NacosUser;

import javax.crypto.Mac;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Nacos JWT HMAC 签名算法实现（HS256/HS384/HS512）。
 *
 * <p>负责 JWT 三段式结构的签名、验签与过期校验，不依赖第三方 JWT 库。</p>
 *
 * @author Weizhan▪Yun
 * @date 2023/1/15 16:42
 */
public final class NacosSignatureAlgorithm {
    
    /** JWT 各段分隔符。 */
    private static final String JWT_SEPERATOR = ".";
    
    private static final int HEADER_POSITION = 0;
    
    private static final int PAYLOAD_POSITION = 1;
    
    private static final int SIGNATURE_POSITION = 2;
    
    private static final int JWT_PARTS = 3;
    
    private static final String HS256_JWT_HEADER = "eyJhbGciOiJIUzI1NiJ9";
    
    private static final String HS384_JWT_HEADER = "eyJhbGciOiJIUzM4NCJ9";
    
    private static final String HS512_JWT_HEADER = "eyJhbGciOiJIUzUxMiJ9";
    
    private static final Base64.Encoder URL_BASE64_ENCODER =
        Base64.getUrlEncoder().withoutPadding();
    
    private static final Base64.Decoder URL_BASE64_DECODER = Base64.getUrlDecoder();
    
    private static final Map<String, NacosSignatureAlgorithm> MAP = new HashMap<>(4);
    
    /** HMAC-SHA256 签名算法实例。 */
    public static final NacosSignatureAlgorithm HS256 =
        new NacosSignatureAlgorithm("HS256", "HmacSHA256",
            HS256_JWT_HEADER);
    
    /** HMAC-SHA384 签名算法实例。 */
    public static final NacosSignatureAlgorithm HS384 =
        new NacosSignatureAlgorithm("HS384", "HmacSHA384",
            HS384_JWT_HEADER);
    
    /** HMAC-SHA512 签名算法实例。 */
    public static final NacosSignatureAlgorithm HS512 =
        new NacosSignatureAlgorithm("HS512", "HmacSHA512",
            HS512_JWT_HEADER);
    
    private final String algorithm;
    
    private final String jcaName;
    
    private final String header;
    
    static {
        MAP.put(HS256_JWT_HEADER, HS256);
        MAP.put(HS384_JWT_HEADER, HS384);
        MAP.put(HS512_JWT_HEADER, HS512);
    }
    
    /**
     * 校验完整 JWT 字符串并解析为 {@link NacosUser}。
     *
     * @param jwt complete jwt string
     * @param key for signature
     * @return object for payload
     * @throws AccessException access exception
     */
    public static NacosUser verify(String jwt, Key key) throws AccessException {
        if (StringUtils.isBlank(jwt)) {
            throw new AccessException("user not found!");
        }
        String[] split = jwt.split("\\.");
        if (split.length != JWT_PARTS) {
            throw new AccessException("token invalid!");
        }
        String header = split[HEADER_POSITION];
        String payload = split[PAYLOAD_POSITION];
        String signature = split[SIGNATURE_POSITION];
        
        NacosSignatureAlgorithm signatureAlgorithm = MAP.get(header);
        if (signatureAlgorithm == null) {
            throw new AccessException("unsupported signature algorithm");
        }
        NacosUser user = signatureAlgorithm.verify(header, payload, signature, key);
        user.setToken(jwt);
        return user;
    }
    
    /**
     * 校验 JWT 各段 HMAC 签名并检查 exp 是否过期。
     *
     * @param header    header of jwt
     * @param payload   payload of jwt
     * @param signature signature of jwt
     * @param key       for signature
     * @return object for payload
     * @throws AccessException access exception
     */
    public NacosUser verify(String header, String payload, String signature, Key key)
        throws AccessException {
        Mac macInstance = getMacInstance(key);
        byte[] bytes = macInstance
            .doFinal((header + JWT_SEPERATOR + payload).getBytes(StandardCharsets.US_ASCII));
        if (!URL_BASE64_ENCODER.encodeToString(bytes).equals(signature)) {
            throw new AccessException("Invalid signature");
        }
        NacosJwtPayload nacosJwtPayload =
            JacksonUtils.toObj(URL_BASE64_DECODER.decode(payload), NacosJwtPayload.class);
        if (nacosJwtPayload.getExp() >= TimeUnit.MILLISECONDS
            .toSeconds(System.currentTimeMillis())) {
            return new NacosUser(nacosJwtPayload.getSub());
        }
        
        throw new AccessException("token expired!");
    }
    
    /**
     * 从完整 JWT 读取过期时间戳（秒）。
     *
     * @param jwt complete jwt string
     * @param key for signature
     * @return expire time in seconds
     * @throws AccessException access exception
     */
    public static long getExpiredTimeInSeconds(String jwt, Key key) throws AccessException {
        if (StringUtils.isBlank(jwt)) {
            throw new AccessException("user not found!");
        }
        String[] split = jwt.split("\\.");
        if (split.length != JWT_PARTS) {
            throw new AccessException("token invalid!");
        }
        String header = split[HEADER_POSITION];
        String payload = split[PAYLOAD_POSITION];
        String signature = split[SIGNATURE_POSITION];
        
        NacosSignatureAlgorithm signatureAlgorithm = MAP.get(header);
        if (signatureAlgorithm == null) {
            throw new AccessException("unsupported signature algorithm");
        }
        return signatureAlgorithm.getExpireTimeInSeconds(header, payload, signature, key);
    }
    
    /**
     * get jwt expire time in seconds.
     *
     * @param header    header of jwt
     * @param payload   payload of jwt
     * @param signature signature of jwt
     * @param key       for signature
     * @return expire time in seconds
     * @throws AccessException access exception
      * <p>JWT HMAC 签名算法实现。</p>
     */
    public long getExpireTimeInSeconds(String header, String payload, String signature, Key key)
        throws AccessException {
        Mac macInstance = getMacInstance(key);
        byte[] bytes = macInstance
            .doFinal((header + JWT_SEPERATOR + payload).getBytes(StandardCharsets.US_ASCII));
        if (!URL_BASE64_ENCODER.encodeToString(bytes).equals(signature)) {
            throw new AccessException("Invalid signature");
        }
        NacosJwtPayload nacosJwtPayload =
            JacksonUtils.toObj(URL_BASE64_DECODER.decode(payload), NacosJwtPayload.class);
        return nacosJwtPayload.getExp();
    }
    
    private NacosSignatureAlgorithm(String alg, String jcaName, String header) {
        this.algorithm = alg;
        this.jcaName = jcaName;
        this.header = header;
    }
    
    /** 对载荷签名，返回 header.payload.signature 格式 JWT。 */
    String sign(NacosJwtPayload nacosJwtPayload, Key key) {
        String jwtWithoutSign = header + JWT_SEPERATOR + URL_BASE64_ENCODER.encodeToString(
            nacosJwtPayload.toString().getBytes(StandardCharsets.UTF_8));
        Mac macInstance = getMacInstance(key);
        byte[] bytes = jwtWithoutSign.getBytes(StandardCharsets.US_ASCII);
        String signature = URL_BASE64_ENCODER.encodeToString(macInstance.doFinal(bytes));
        return jwtWithoutSign + JWT_SEPERATOR + signature;
    }
    
    /** 创建并初始化 HMAC Mac 实例。 */
    private Mac getMacInstance(Key key) {
        try {
            Mac instance = Mac.getInstance(jcaName);
            instance.init(key);
            return instance;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("No Such Algorithm: " + jcaName);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid key: " + key);
        }
    }
    
    /** 返回算法名称（如 HS256）。 */
    public String getAlgorithm() {
        return algorithm;
    }
    
    /** 返回 JCA 算法名（如 HmacSHA256）。 */
    public String getJcaName() {
        return jcaName;
    }
    
    /** 返回预编码的 JWT Header 段（Base64URL）。 */
    public String getHeader() {
        return header;
    }
}
