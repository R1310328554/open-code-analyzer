/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.protocol.oidc.client.authentication;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.keycloak.OAuth2Constants;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.common.util.Time;
import org.keycloak.crypto.Algorithm;
import org.keycloak.crypto.JavaAlgorithm;
import org.keycloak.jose.jws.JWSBuilder;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.representations.adapters.config.AdapterConfig;

import org.jboss.logging.Logger;

/**
 * 基于客户端密钥（HMAC）签名的 JWT 客户端认证（{@code client_secret_jwt}）。
 * 详见 <a href="http://openid.net/specs/openid-connect-core-1_0.html#ClientAuthentication">OIDC 客户端认证规范</a>。
 */
public class JWTClientSecretCredentialsProvider implements ClientCredentialsProvider {

    private static final Logger logger = Logger.getLogger(JWTClientSecretCredentialsProvider.class);

    /** 提供者 ID：{@code secret-jwt}。 */
    public static final String PROVIDER_ID = "secret-jwt";

    /** 由 client_secret 派生的 HMAC 密钥。 */
    private SecretKey clientSecret;

    /** 断言 JWT 签名算法，默认 HS256。 */
    private String clientSecretJwtAlg = Algorithm.HS256;

    /** @return {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /**
     * 从 keycloak.json 读取 secret 与可选 algorithm 并初始化 HMAC 密钥。
     *
     * @param deployment 适配器配置
     * @param config secret-jwt 凭据 Map
     */
    @Override
    public void init(AdapterConfig deployment, Object config) {
        if (!(config instanceof Map)) {
            throw new RuntimeException("Configuration of jwt credentials by client secret is missing or incorrect for client '" + deployment.getResource() + "'. Check your adapter configuration");
        }

        Map<String, Object> cfg = (Map<String, Object>) config;
        String clientSecretString = (String) cfg.get("secret");
        if (clientSecretString == null) {
            throw new RuntimeException("Missing parameter secret-jwt in configuration of jwt for client " + deployment.getResource());
        }

        String clientSecretJwtAlg = (String) cfg.get("algorithm");
        if (clientSecretJwtAlg == null) {
            // algorithm 可选，缺省 HS256
            setClientSecret(clientSecretString); 
        } else if (isValidClientSecretJwtAlg(clientSecretJwtAlg)) {
            setClientSecret(clientSecretString, clientSecretJwtAlg); 
        } else {
            throw new RuntimeException("Invalid parameter secret-jwt in configuration of jwt for client " + deployment.getResource());
        }
    }

    /** 校验 algorithm 是否为支持的 HMAC 算法（HS256/384/512）。 */
    private boolean isValidClientSecretJwtAlg(String clientSecretJwtAlg) {
        boolean ret = false;
        if (Algorithm.HS256.equals(clientSecretJwtAlg) || Algorithm.HS384.equals(clientSecretJwtAlg) || Algorithm.HS512.equals(clientSecretJwtAlg))
            ret = true;
        return ret;
    }

    /**
     * 生成 HMAC 签名的 client_assertion 并写入表单参数。
     *
     * @param deployment 适配器配置
     * @param requestHeaders HTTP 请求头（本实现未使用）
     * @param formParams 表单参数
     */
    @Override
    public void setClientCredentials(AdapterConfig deployment, Map<String, String> requestHeaders, Map<String, String> formParams) {
        String signedToken = createSignedRequestToken(deployment.getResource(), deployment.getRealmInfoUrl());
        formParams.put(OAuth2Constants.CLIENT_ASSERTION_TYPE, OAuth2Constants.CLIENT_ASSERTION_TYPE_JWT);
        formParams.put(OAuth2Constants.CLIENT_ASSERTION, signedToken);
    }

    /**
     * 使用 UTF-8 编码的 client_secret 作为 HMAC 共享密钥，默认 HS256。
     * OIDC 规范要求以 client_secret 的 UTF-8 八位组作为 HMAC 密钥。
     */
    public void setClientSecret(String clientSecretString) {
        setClientSecret(clientSecretString, Algorithm.HS256);
    }

    /**
     * 指定 HMAC 算法并构造 {@link SecretKeySpec}。
     *
     * @param clientSecretString 客户端密钥明文
     * @param algorithm HMAC 算法（HS256/384/512）
     */
    public void setClientSecret(String clientSecretString, String algorithm) {
        clientSecret = new SecretKeySpec(clientSecretString.getBytes(StandardCharsets.UTF_8), JavaAlgorithm.getJavaAlgorithm(algorithm));
        clientSecretJwtAlg = algorithm;
    }

    /** @param clientId 客户端 ID @param realmInfoUrl 受众 URL @return 签名的 client_assertion JWT */
    public String createSignedRequestToken(String clientId, String realmInfoUrl) {
        return createSignedRequestToken(clientId, realmInfoUrl, clientSecretJwtAlg);
    }

    /**
     * 按指定 HMAC 算法签名 client_assertion JWT。
     *
     * @param clientId 客户端 ID
     * @param realmInfoUrl 受众 URL
     * @param algorithm HMAC 算法
     * @return Compact 序列化的 signed JWT
     */
    public String createSignedRequestToken(String clientId, String realmInfoUrl, String algorithm) {
        JsonWebToken jwt = createRequestToken(clientId, realmInfoUrl);
        String signedRequestToken = null;
        if (Algorithm.HS512.equals(algorithm)) {
            signedRequestToken = new JWSBuilder().jsonContent(jwt).hmac512(clientSecret);
        } else if (Algorithm.HS384.equals(algorithm)) {
            signedRequestToken = new JWSBuilder().jsonContent(jwt).hmac384(clientSecret);
        } else {
            signedRequestToken = new JWSBuilder().jsonContent(jwt).hmac256(clientSecret);
        }
        return signedRequestToken;
    }

    /**
     * 构造 client_assertion 载荷；声明集与 private_key_jwt 相同（参见 KEYCLOAK-2986，exp 固定 +10 秒）。
     *
     * @param clientId 客户端 ID
     * @param realmInfoUrl 受众 URL
     * @return 未签名的 {@link JsonWebToken}
     */
    protected JsonWebToken createRequestToken(String clientId, String realmInfoUrl) {
        JsonWebToken reqToken = new JsonWebToken();
        reqToken.id(SecretGenerator.getInstance().generateSecureID());
        reqToken.issuer(clientId);
        reqToken.subject(clientId);
        reqToken.audience(realmInfoUrl);

        long now = Time.currentTime();
        reqToken.iat(now);
        reqToken.exp(now + 10);
        reqToken.nbf(now);
        return reqToken;
    }

}
