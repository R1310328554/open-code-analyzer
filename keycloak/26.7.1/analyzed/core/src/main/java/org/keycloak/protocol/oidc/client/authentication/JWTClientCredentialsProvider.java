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

package org.keycloak.protocol.oidc.client.authentication;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Map;

import org.keycloak.OAuth2Constants;
import org.keycloak.common.util.KeyUtils;
import org.keycloak.common.util.KeystoreUtil;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.common.util.Time;
import org.keycloak.crypto.Algorithm;
import org.keycloak.crypto.AsymmetricSignatureSignerContext;
import org.keycloak.crypto.ECDSASignatureSignerContext;
import org.keycloak.crypto.JavaAlgorithm;
import org.keycloak.crypto.KeyType;
import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.crypto.SignatureSignerContext;
import org.keycloak.jose.jws.JWSBuilder;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.representations.adapters.config.AdapterConfig;

/**
 * 基于客户端私钥签名的 JWT 客户端认证（{@code private_key_jwt}）。
 * 详见 <a href="https://tools.ietf.org/html/rfc7519">RFC 7519</a>。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class JWTClientCredentialsProvider implements ClientCredentialsProvider {

    /** 提供者 ID：{@code jwt}。 */
    public static final String PROVIDER_ID = "jwt";

    /** 客户端密钥对。 */
    private KeyPair keyPair;
    /** 签名上下文。 */
    private SignatureSignerContext sigCtx;

    /** 断言 JWT 有效期（秒）。 */
    private int tokenTimeout;

    /** @return {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** 使用默认 RS256 算法配置密钥对。 */
    public void setupKeyPair(KeyPair keyPair) {
        setupKeyPair(keyPair, Algorithm.RS256);
    }

    /**
     * 配置密钥对并创建对应算法的签名上下文。
     *
     * @param keyPair 客户端密钥对
     * @param algorithm JWS 签名算法
     */
    public void setupKeyPair(KeyPair keyPair, String algorithm) {
        // 为密钥对创建 KeyWrapper
        KeyWrapper keyWrapper = new KeyWrapper();
        keyWrapper.setKid(KeyUtils.createKeyId(keyPair.getPublic()));
        keyWrapper.setAlgorithm(algorithm);
        keyWrapper.setPrivateKey(keyPair.getPrivate());
        keyWrapper.setPublicKey(keyPair.getPublic());
        keyWrapper.setType(keyPair.getPublic().getAlgorithm());
        keyWrapper.setUse(KeyUse.SIG);

        // 校验算法与密钥类型匹配
        switch (JavaAlgorithm.getKeyType(keyPair.getPublic().getAlgorithm())) {
            case KeyType.RSA:
                if (!JavaAlgorithm.isRSAJavaAlgorithm(algorithm)) {
                    throw new RuntimeException("Invalid algorithm for a RSA KeyPair: " + algorithm);
                }
                this.sigCtx = new AsymmetricSignatureSignerContext(keyWrapper);
                break;
            case KeyType.EC:
                if (!JavaAlgorithm.isECJavaAlgorithm(algorithm)) {
                    throw new RuntimeException("Invalid algorithm for a EC KeyPair: " + algorithm);
                }
                this.sigCtx = new ECDSASignatureSignerContext(keyWrapper);
                break;
            case KeyType.OKP:
                if (!JavaAlgorithm.isEddsaJavaAlgorithm(algorithm)) {
                    throw new RuntimeException("Invalid algorithm for a EdDSA KeyPair: " + algorithm);
                }
                this.sigCtx = new AsymmetricSignatureSignerContext(keyWrapper);
                break;
            default:
                throw new RuntimeException("Invalid KeyPair algorithm: " + keyPair.getPublic().getAlgorithm());
        }
        this.keyPair = keyPair;
    }

    /** 设置断言 JWT 过期时间（秒）。 */
    public void setTokenTimeout(int tokenTimeout) {
        this.tokenTimeout = tokenTimeout;
    }

    /** @return 断言 JWT 有效期（秒） */
    protected int getTokenTimeout() {
        return tokenTimeout;
    }

    /** @return 客户端公钥 */
    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    /**
     * 从 keycloak.json 的 jwt 凭据配置加载密钥库并初始化签名上下文。
     *
     * @param deployment 适配器配置
     * @param config jwt 凭据 Map（含 keystore 路径、密码、alias 等）
     */
    @Override
    public void init(AdapterConfig deployment, Object config) {
        if (!(config instanceof Map)) {
            throw new RuntimeException("Configuration of jwt credentials is missing or incorrect for client '" + deployment.getResource() + "'. Check your adapter configuration");
        }

        Map<String, Object> cfg = (Map<String, Object>) config;

        String clientKeystoreFile =  (String) cfg.get("client-keystore-file");
        if (clientKeystoreFile == null) {
            throw new RuntimeException("Missing parameter client-keystore-file in configuration of jwt for client " + deployment.getResource());
        }

        String clientKeystoreType = (String) cfg.get("client-keystore-type");
        KeystoreUtil.KeystoreFormat clientKeystoreFormat = clientKeystoreType==null ? KeystoreUtil.KeystoreFormat.JKS : Enum.valueOf(KeystoreUtil.KeystoreFormat.class, clientKeystoreType.toUpperCase());

        String clientKeystorePassword =  (String) cfg.get("client-keystore-password");
        if (clientKeystorePassword == null) {
            throw new RuntimeException("Missing parameter client-keystore-password in configuration of jwt for client " + deployment.getResource());
        }

        String clientKeyPassword = (String) cfg.get("client-key-password");
        if (clientKeyPassword == null) {
            clientKeyPassword = clientKeystorePassword;
        }

        String clientKeyAlias =  (String) cfg.get("client-key-alias");
        if (clientKeyAlias == null) {
            clientKeyAlias = deployment.getResource();
        }

        String algorithm = (String) cfg.getOrDefault("algorithm", Algorithm.RS256);

        KeyPair keyPair = KeystoreUtil.loadKeyPairFromKeystore(clientKeystoreFile, clientKeystorePassword, clientKeyPassword, clientKeyAlias, clientKeystoreFormat);
        setupKeyPair(keyPair, algorithm);

        this.tokenTimeout = asInt(cfg, "token-timeout", 10);
    }

    /** 从配置 Map 读取整型值，支持 String 与 Number。 */
    private Integer asInt(Map<String, Object> cfg, String cfgKey, int defaultValue) {
        Object cfgObj = cfg.get(cfgKey);
        if (cfgObj == null) {
            return defaultValue;
        }

        if (cfgObj instanceof String) {
            return Integer.parseInt(cfgObj.toString());
        } else if (cfgObj instanceof Number) {
            return ((Number) cfgObj).intValue();
        } else {
            throw new IllegalArgumentException("Can't parse " + cfgKey + " from the config. Value is " + cfgObj);
        }
    }

    /**
     * 生成 signed JWT 并写入 client_assertion 表单参数。
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
     * 构造并签名客户端断言 JWT。
     *
     * @param clientId 客户端 ID
     * @param realmInfoUrl 领域信息 URL（作为 aud）
     * @return Compact 序列化的 signed JWT
     */
    public String createSignedRequestToken(String clientId, String realmInfoUrl) {
        JsonWebToken jwt = createRequestToken(clientId, realmInfoUrl);
        return new JWSBuilder()
                .jsonContent(jwt)
                .sign(sigCtx);
    }

    /**
     * 构造 client_assertion 载荷（iss/sub/aud/iat/exp/nbf/jti）。
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
        reqToken.exp(now + this.tokenTimeout);
        reqToken.nbf(now);

        return reqToken;
    }
}
