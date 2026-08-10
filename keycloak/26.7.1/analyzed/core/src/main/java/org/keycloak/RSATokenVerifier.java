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

package org.keycloak;

import java.security.PublicKey;

import org.keycloak.common.VerificationException;
import org.keycloak.jose.jws.JWSHeader;
import org.keycloak.representations.AccessToken;

/**
 * 基于 RSA 公钥验证 {@link AccessToken} 的流式构建器（已弃用，请使用 {@link TokenVerifier}）。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 * @deprecated 请改用 {@link TokenVerifier}
 */
@Deprecated
public class RSATokenVerifier {

    /** 底层 {@link TokenVerifier} 实例。 */
    private final TokenVerifier<AccessToken> tokenVerifier;

    private RSATokenVerifier(String tokenString) {
        this.tokenVerifier = TokenVerifier.create(tokenString, AccessToken.class).withDefaultChecks();
    }

    /** 从 JWT 字符串创建验证器。 */
    public static RSATokenVerifier create(String tokenString) {
        return new RSATokenVerifier(tokenString);
    }

    /**
     * 一步完成 RSA 访问令牌验证。
     *
     * @param tokenString JWT 字符串
     * @param publicKey RSA 公钥
     * @param realmUrl 期望的 realm issuer URL
     * @return 验证通过的访问令牌
     * @throws VerificationException 验证失败时抛出
     */
    public static AccessToken verifyToken(String tokenString, PublicKey publicKey, String realmUrl) throws VerificationException {
        return RSATokenVerifier.create(tokenString).publicKey(publicKey).realmUrl(realmUrl).verify().getToken();
    }

    /**
     * 一步完成 RSA 访问令牌验证，可控制 active 与 token type 检查。
     *
     * @param tokenString JWT 字符串
     * @param publicKey RSA 公钥
     * @param realmUrl 期望的 realm issuer URL
     * @param checkActive 是否检查 exp/nbf
     * @param checkTokenType 是否检查 typ 声明
     * @return 验证通过的访问令牌
     * @throws VerificationException 验证失败时抛出
     */
    public static AccessToken verifyToken(String tokenString, PublicKey publicKey, String realmUrl, boolean checkActive, boolean checkTokenType) throws VerificationException {
        return RSATokenVerifier.create(tokenString).publicKey(publicKey).realmUrl(realmUrl).checkActive(checkActive).checkTokenType(checkTokenType).verify().getToken();
    }

    /** 设置用于验签的 RSA 公钥。 */
    public RSATokenVerifier publicKey(PublicKey publicKey) {
        tokenVerifier.publicKey(publicKey);
        return this;
    }

    /** 设置期望的 realm issuer URL。 */
    public RSATokenVerifier realmUrl(String realmUrl) {
        tokenVerifier.realmUrl(realmUrl);
        return this;
    }

    /** 是否校验令牌类型（typ）声明。 */
    public RSATokenVerifier checkTokenType(boolean checkTokenType) {
        tokenVerifier.checkTokenType(checkTokenType);
        return this;
    }

    /** 是否校验令牌是否在有效期内（exp/nbf）。 */
    public RSATokenVerifier checkActive(boolean checkActive) {
        tokenVerifier.checkActive(checkActive);
        return this;
    }

    /** 是否校验 issuer 与 realm URL 一致。 */
    public RSATokenVerifier checkRealmUrl(boolean checkRealmUrl) {
        tokenVerifier.checkRealmUrl(checkRealmUrl);
        return this;
    }

    /** 解析 JWT 但不执行验签。 */
    public RSATokenVerifier parse() throws VerificationException {
        tokenVerifier.parse();
        return this;
    }

    /** 返回已解析的访问令牌（需先 parse 或 verify）。 */
    public AccessToken getToken() throws VerificationException {
        return tokenVerifier.getToken();
    }

    /** 返回 JWS 头。 */
    public JWSHeader getHeader() throws VerificationException {
        return tokenVerifier.getHeader();
    }

    /** 执行完整验证（含签名与已启用的检查项）。 */
    public RSATokenVerifier verify() throws VerificationException {
        tokenVerifier.verify();
        return this;
    }

}
