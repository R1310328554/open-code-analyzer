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
package org.keycloak.models;

import java.util.function.BiConsumer;

import org.keycloak.Token;
import org.keycloak.TokenCategory;
import org.keycloak.jose.JOSE;
import org.keycloak.jose.jws.Algorithm;
import org.keycloak.representations.LogoutToken;

/**
 * 令牌管理器：编码、解码与加密 JWT/Logout 等令牌。
 */
public interface TokenManager {

    /** 默认 JWT 校验器：拒绝 alg 为 none 的令牌。 */
    BiConsumer<JOSE, ClientModel> DEFAULT_VALIDATOR = (jwt, client) -> {
        String rawAlgorithm = jwt.getHeader().getRawAlgorithm();

        if (rawAlgorithm.equalsIgnoreCase(Algorithm.none.name())) {
            throw new RuntimeException("Algorithm none not supported");
        }
    };

    /**
     * 编码给定令牌。
     * Encodes the supplied token
     *
     * @param token the token to encode
     * @return The encoded token
     */
    String encode(Token token);

    /**
     * 解码并验证令牌；无效时返回 <code>null</code>。
     * Decodes and verifies the token, or <code>null</code> if the token was invalid
     *
     * @param token the token to decode
     * @param clazz the token type to return
     * @param <T>
     * @return The decoded token, or <code>null</code> if the token was not valid
     */
    <T extends Token> T decode(String token, Class<T> clazz);

    /** @param category 令牌类别
     * @return 签名算法名称 */
    String signatureAlgorithm(TokenCategory category);

    /**
     *
     *
     * @param token JWT token, which might be signed or encrypted by the keys of specified client. It cannot use "alg: none" in the header
     * @param client client, whose keys/secret might be used to decrypt the token or verify it's signatures
     * @param clazz class, which the provided token would be cast to
     * @return decoded java object from the provided token. If it returns null, then signature validation failed or provided token was not valid
     */
    /** 使用客户端密钥解码 JWT（默认拒绝 alg:none）。
     * @param token JWT 字符串
     * @param client 客户端
     * @param clazz 目标类型
     * @return 解码结果或 null */
    default <T> T decodeClientJWT(String token, ClientModel client, Class<T> clazz) {
        return decodeClientJWT(token, client, DEFAULT_VALIDATOR, clazz, false);
    }

    /**
     * 解码客户端 JWT；可按需允许 alg:none。
     * @param token JWT token, which might be signed or encrypted by the keys of specified client. It can use "alg: none" in the header just if parameter "allowAlgorithmNone" is true
     * @param client client, whose keys/secret might be used to decrypt the token or verify it's signatures
     * @param jwtValidator Additional validator
     * @param clazz class, which the provided token would be cast to
     * @param allowAlgorithmNone Whether the token using "alg: none" is allowed or not. If this parameter is false and "alg: none" is used, the {@link IllegalArgumentException} will be thrown
     * @return decoded java object from the provided token. If it returns null, then signature validation failed or provided token was not valid
     */
    <T> T decodeClientJWT(String token, ClientModel client, BiConsumer<JOSE, ClientModel> jwtValidator,
            Class<T> clazz, boolean allowAlgorithmNone);

    /** @param token 待加密编码的令牌
     * @return 加密后的令牌字符串 */
    String encodeAndEncrypt(Token token);
    /** @param category 令牌类别
     * @return CEK 管理算法 */
    String cekManagementAlgorithm(TokenCategory category);
    /** @param category 令牌类别
     * @return 内容加密算法 */
    String encryptAlgorithm(TokenCategory category);

    /** 初始化 Logout 令牌。
     * @param client 客户端
     * @param user 用户
     * @param clientSessionModel 已认证客户端会话
     * @return LogoutToken 实例 */
    LogoutToken initLogoutToken(ClientModel client, UserModel user, AuthenticatedClientSessionModel clientSessionModel);
}
