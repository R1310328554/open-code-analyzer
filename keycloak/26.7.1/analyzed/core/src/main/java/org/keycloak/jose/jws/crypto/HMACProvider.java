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

package org.keycloak.jose.jws.crypto;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.keycloak.common.util.Base64Url;
import org.keycloak.jose.jws.Algorithm;
import org.keycloak.jose.jws.JWSInput;

/**
 * HMAC 算法 JWS 签名与验签提供者：支持 HS256/HS384/HS512。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class HMACProvider implements SignatureProvider {
    /** 将 JWS HMAC 算法映射为 JCA Mac 算法名。 */
    private static String getJavaAlgorithm(Algorithm alg) {
        switch (alg) {
            case HS256:
                return "HMACSHA256";
            case HS384:
                return "HMACSHA384";
            case HS512:
                return "HMACSHA512";
            default:
                throw new IllegalArgumentException("Not a MAC Algorithm");
        }
    }

    /** 获取指定算法的 {@link Mac} 实例。 */
    private static Mac getMAC(final Algorithm alg) {

        try {
            return Mac.getInstance(getJavaAlgorithm(alg));

        } catch (NoSuchAlgorithmException e) {

            throw new RuntimeException("Unsupported HMAC algorithm: " + e.getMessage(), e);
        }
    }

    /**
     * 使用共享密钥字节对数据做 HMAC 签名。
     *
     * @param data 待签名数据
     * @param algorithm HMAC 算法
     * @param sharedSecret 共享密钥
     * @return MAC 输出
     */
    public static byte[] sign(byte[] data, Algorithm algorithm, byte[] sharedSecret) {
        try {
            Mac mac = getMAC(algorithm);
            mac.init(new SecretKeySpec(sharedSecret, mac.getAlgorithm()));
            mac.update(data);
            return mac.doFinal();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 使用 {@link SecretKey} 对数据做 HMAC 签名。
     *
     * @param data 待签名数据
     * @param algorithm HMAC 算法
     * @param key 对称密钥
     * @return MAC 输出
     */
    public static byte[] sign(byte[] data, Algorithm algorithm, SecretKey key) {
        try {
            Mac mac = getMAC(algorithm);
            mac.init(key);
            mac.update(data);
            return mac.doFinal();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 使用 {@link SecretKey} 验证 JWS HMAC 签名（常量时间比较）。
     *
     * @param input JWS 输入
     * @param key 对称密钥
     * @return 验签是否通过
     */
    public static boolean verify(JWSInput input, SecretKey key) {
        try {
            byte[] signature = sign(input.getEncodedSignatureInput().getBytes(StandardCharsets.UTF_8), input.getHeader().getAlgorithm(), key);
            return MessageDigest.isEqual(signature, Base64Url.decode(input.getEncodedSignature()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 使用共享密钥字节验证 JWS HMAC 签名。
     *
     * @param input JWS 输入
     * @param sharedSecret 共享密钥
     * @return 验签是否通过
     */
    public static boolean verify(JWSInput input, byte[] sharedSecret) {
        try {
            byte[] signature = sign(input.getEncodedSignatureInput().getBytes(StandardCharsets.UTF_8), input.getHeader().getAlgorithm(), sharedSecret);
            return MessageDigest.isEqual(signature, Base64Url.decode(input.getEncodedSignature()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@link SignatureProvider} 接口实现：HMAC 不支持字符串密钥验签，始终返回 false。
     *
     * @param input JWS 输入
     * @param key 密钥字符串（未使用）
     * @return false
     */
    @Override
    public boolean verify(JWSInput input, String key) {
        return false;
    }
}
