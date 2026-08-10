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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

import org.keycloak.common.util.Base64Url;
import org.keycloak.crypto.HashException;
import org.keycloak.crypto.JavaAlgorithm;

/**
 * 令牌哈希工具：生成 OIDC {@code at_hash}/{@code c_hash} 及 DPoP {@code ath} 等 Base64URL 哈希值。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class HashUtils {

    /**
     * 计算访问令牌哈希（OIDC 半长哈希，{@code full=false}）。
     *
     * @param jwtAlgorithmName JWT 签名算法名
     * @param input 待哈希字符串（通常为 access token）
     * @param full 是否使用完整哈希长度（DPPoP ath 为 true，OIDC at_hash 为 false）
     * @return Base64URL 编码的哈希
     */
    public static String accessTokenHash(String jwtAlgorithmName, String input, boolean full) {
        return accessTokenHash(jwtAlgorithmName, null, input, full);
    }

    /**
     * 计算访问令牌哈希，可指定 EC 曲线。
     *
     * @param jwtAlgorithmName JWT 签名算法名
     * @param curve EC 曲线（可为 null）
     * @param input 待哈希字符串
     * @param full 是否使用完整哈希长度
     * @return Base64URL 编码的哈希
     */
    public static String accessTokenHash(String jwtAlgorithmName, String curve, String input, boolean full) {
        byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);
        String javaAlgName = JavaAlgorithm.getJavaAlgorithmForHash(jwtAlgorithmName, curve);
        byte[] hash = hash(javaAlgName, inputBytes);

        return encodeHashToOIDC(hash, full);
    }

    /** @param jwtAlgorithmName JWT 算法名 @param input 待哈希字符串 @return OIDC 半长 at_hash */
    public static String accessTokenHash(String jwtAlgorithmName, String input) {
        return HashUtils.accessTokenHash(jwtAlgorithmName, null, input, false);
    }

    /** @param jwtAlgorithmName JWT 算法名 @param curve EC 曲线 @param input 待哈希字符串 @return OIDC 半长 at_hash */
    public static String accessTokenHash(String jwtAlgorithmName, String curve, String input) {
        return HashUtils.accessTokenHash(jwtAlgorithmName, curve, input, false);
    }

    /**
     * 使用指定 JCA 算法计算消息摘要。
     *
     * @param javaAlgorithmName JCA 哈希算法名
     * @param inputBytes 输入字节
     * @return 摘要字节
     */
    public static byte[] hash(String javaAlgorithmName, byte[] inputBytes) {
        try {
            MessageDigest md = MessageDigest.getInstance(javaAlgorithmName);
            md.update(inputBytes);
            return md.digest();
        } catch (Exception e) {
            throw new HashException("Error when creating token hash", e);
        }
    }

    /** @param hash 完整哈希 @return OIDC 半长 Base64URL 编码 */
    public static String encodeHashToOIDC(byte[] hash) {
        return encodeHashToOIDC(hash, false);
    }

    /**
     * 将哈希截断（或保留全长）后 Base64URL 编码。
     *
     * @param hash 完整哈希字节
     * @param full true 使用全长，false 取左半（OIDC 规范）
     * @return Base64URL 编码
     */
    public static String encodeHashToOIDC(byte[] hash, boolean full) {
        int hashLength = full ? hash.length : hash.length / 2;
        byte[] hashInput = Arrays.copyOf(hash, hashLength);

        return Base64Url.encode(hashInput);
    }

    /**
     * SHA-256 哈希并 Base64URL 编码。
     *
     * @param input 输入字符串
     * @param charset 字符集
     * @return Base64URL 编码的 SHA-256 摘要
     */
    public static String sha256UrlEncodedHash(String input, Charset charset) {
        byte[] inputBytes = input.getBytes(charset);
        byte[] hashedOutput = hash(JavaAlgorithm.SHA256, inputBytes);
        return Base64Url.encode(hashedOutput);
    }

    /**
     * SHA-384 哈希并 Base64URL 编码。
     *
     * @param input 输入字符串
     * @param charset 字符集
     * @return Base64URL 编码的 SHA-384 摘要
     */
    public static String sha384UrlEncodedHash(String input, Charset charset) {
        byte[] inputBytes = input.getBytes(charset);
        byte[] hashedOutput = hash(JavaAlgorithm.SHA384, inputBytes);
        return Base64Url.encode(hashedOutput);
    }

}
