/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.util;

import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;

import org.keycloak.OAuth2Constants;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.common.util.Time;
import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.crypto.SignatureSignerContext;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.jose.jwk.JWKBuilder;
import org.keycloak.jose.jws.JWSBuilder;
import org.keycloak.jose.jws.JWSHeader;
import org.keycloak.jose.jws.crypto.HashUtils;
import org.keycloak.representations.dpop.DPoP;

import static org.keycloak.OAuth2Constants.DPOP_DEFAULT_ALGORITHM;
import static org.keycloak.OAuth2Constants.DPOP_JWT_HEADER_TYPE;

/**
 * DPoP（Demonstrating Proof of Possession）证明生成工具。
 *
 * <p>
 * 用于构造并签名符合 RFC 9449 的 DPoP JWT，证明客户端持有访问令牌对应的私钥。
 * </p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc9449">OAuth 2.0 Demonstrating Proof of Possession (DPoP) specification</a>
 *
 */
public class DPoPGenerator {

    // TODO: 补充 EC 与 EdDSA 的类似便捷方法
    /**
     * 使用 RSA 密钥对生成已签名的 DPoP 证明。
     *
     * @param rsaKeyPair RSA 密钥对
     * @param httpMethod HTTP 方法（htm）
     * @param endpointURL 请求 URI（htu）
     * @param accessToken 可选的访问令牌（用于计算 ath 哈希）
     * @return 紧凑序列化的 DPoP JWT 字符串
     */
    public static String generateRsaSignedDPoPProof(KeyPair rsaKeyPair, String httpMethod, String endpointURL, String accessToken) {
        JWK jwkRsa = createRsaJwk(rsaKeyPair.getPublic());
        JWSHeader jwsRsaHeader = new JWSHeader(DPOP_DEFAULT_ALGORITHM, DPOP_JWT_HEADER_TYPE, jwkRsa.getKeyId(), jwkRsa);
        return new DPoPGenerator().generateSignedDPoPProof(SecretGenerator.getInstance().generateSecureID(), httpMethod, endpointURL, (long) Time.currentTime(),
                jwsRsaHeader, rsaKeyPair.getPrivate(), accessToken);
    }


    /** 从 RSA 公钥创建用于 DPoP 的 JWK。 */
    public static JWK createRsaJwk(Key publicKey) {
        return JWKBuilder.create().rsa(publicKey, KeyUse.SIG);
    }

    /** 从 EC 公钥创建用于 DPoP 的 JWK。 */
    public static JWK createEcJwk(Key publicKey) {
        return JWKBuilder.create().ec(publicKey);
    }

    /**
     * 构建 DPoP 载荷并签名，返回紧凑 JWT 字符串。
     *
     * @param jti JWT ID
     * @param htm HTTP 方法
     * @param htu HTTP URI
     * @param iat 签发时间戳
     * @param jwsHeader JWS 头部（含 jwk）
     * @param keyWrapper 含私钥的密钥包装器
     * @param accessToken 可选访问令牌
     * @return 已签名的 DPoP JWT
     */
    public static String generateSignedDPoPProof(String jti, String htm, String htu, Long iat, JWSHeader jwsHeader, KeyWrapper keyWrapper, String accessToken) {
        DPoP dpop = generateDPoP(jti, htm, htu, iat, accessToken);
        return sign(jwsHeader, dpop, keyWrapper);
    }

    /**
     * 使用 {@link PrivateKey} 构建并签名 DPoP 证明。
     */
    public String generateSignedDPoPProof(String jti, String htm, String htu, Long iat, JWSHeader jwsHeader, PrivateKey privateKey, String accessToken) {
        KeyWrapper keyWrapper = getKeyWrapper(jwsHeader, privateKey);
        return generateSignedDPoPProof(jti, htm, htu, iat, jwsHeader, keyWrapper, accessToken);
    }

    private static DPoP generateDPoP(String jti, String htm, String htu, Long iat, String accessToken) {
        DPoP dpop = new DPoP();
        dpop.id(jti);
        dpop.setHttpMethod(htm);
        dpop.setHttpUri(htu);
        dpop.iat(iat);
        if (accessToken != null) {
            dpop.setAccessTokenHash(HashUtils.accessTokenHash(OAuth2Constants.DPOP_DEFAULT_ALGORITHM.toString(), accessToken, true));
        }
        return dpop;
    }

    /**
     * 从 JWS 头部的 jwk 声明与私钥构造 {@link KeyWrapper}。
     */
    protected KeyWrapper getKeyWrapper(JWSHeader jwsHeader, PrivateKey privateKey) {
        JWK jwkKey = jwsHeader.getKey();
        if (jwkKey == null) {
            throw new IllegalArgumentException("The JWSHeader does not have key in the 'jwk' claim");
        }
        KeyWrapper keyWrapper = JWKSUtils.getKeyWrapper(jwkKey, true);
        keyWrapper.setPrivateKey(privateKey);
        return keyWrapper;
    }

    private static String sign(JWSHeader jwsHeader, DPoP dpop, KeyWrapper keyWrapper) {
        SignatureSignerContext sigCtx = KeyWrapperUtil.createSignatureSignerContext(keyWrapper);
        return new JWSBuilder()
                .header(jwsHeader)
                .jsonContent(dpop)
                .sign(sigCtx);
    }
}
