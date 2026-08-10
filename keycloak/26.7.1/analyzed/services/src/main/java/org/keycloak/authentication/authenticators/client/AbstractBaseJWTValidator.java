/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.keycloak.authentication.authenticators.client;

import java.util.List;

import org.keycloak.common.util.Time;
import org.keycloak.crypto.ClientSignatureVerifierProvider;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.SingleUseObjectProvider;
import org.keycloak.representations.JsonWebToken;

import org.jboss.logging.Logger;

/**
 * Base validator for JWT authorization grant and JWT client validators.
 *
 * @author rmartinc
 */
public abstract class AbstractBaseJWTValidator {

    private static final Logger logger = Logger.getLogger(AbstractBaseJWTValidator.class);

    /** 客户端断言解析状态。 */
    protected final ClientAssertionState clientAssertionState;
    /** 当前 Keycloak 会话。 */
    protected final KeycloakSession session;
    /** 校验时的当前 Unix 时间戳（秒）。 */
    protected final int currentTime;

    /**
     * @param session 当前 Keycloak 会话
     * @param clientAssertionState 已解析的客户端断言状态
     */
    public AbstractBaseJWTValidator(KeycloakSession session, ClientAssertionState clientAssertionState) {
        this.session = session;
        this.clientAssertionState = clientAssertionState;
        this.currentTime = Time.currentTime();
    }

    /** @return 客户端断言状态 */
    public ClientAssertionState getState() {
        return clientAssertionState;
    }

    /** @return 原始 client_assertion 字符串 */
    public String getClientAssertion() {
        return clientAssertionState.getClientAssertion();
    }

    /** @return 解析后的 JWS 输入 */
    public JWSInput getJws() {
        return clientAssertionState.getJws();
    }

    /** 校验 exp/iat 时效、最大寿命及 jti 是否允许重用。 */
    public boolean validateTokenActive(int allowedClockSkew, int maxExp, boolean reusePermitted) {
        JsonWebToken token = clientAssertionState.getToken();
        long lifespan;

        if (token.getExp() == null) {
            return failure("Token exp claim is required");
        }

        if (!token.isActive(allowedClockSkew)) {
            return failure("Token is not active");
        }

        lifespan = token.getExp() - currentTime;

        if (token.getIat() == null) {
            if (lifespan > maxExp) {
                return failure("Token expiration is too far in the future and iat claim not present in token");
            }
        } else {
            if (token.getIat() - allowedClockSkew > currentTime) {
                return failure("Token was issued in the future");
            }
            lifespan = Math.min(lifespan, maxExp);
            if (lifespan <= 0) {
                return failure("Token is not active");
            }
            if (currentTime > token.getIat() + maxExp) {
                return failure("Token was issued too far in the past to be used now");
            }
        }

        if (!reusePermitted) {
            if (token.getId() == null) {
                return failure("Token jti claim is required");
            }

            if (!validateTokenReuse(lifespan)) {
                return false;
            }
        }

        return true;
    }

    /** 将 jti 写入单次使用缓存，检测令牌重放。 */
    protected boolean validateTokenReuse(long lifespanInSecs) {
        final JsonWebToken token = clientAssertionState.getToken();
        final String tokenId = token.getId();
        final String namespacePrefix = getJtiCacheKeyPrefix();
        final String cacheKey = namespacePrefix + ":" + tokenId;
        SingleUseObjectProvider singleUseCache = session.singleUseObjects();
        if (singleUseCache.putIfAbsent(cacheKey, lifespanInSecs)) {
            logger.tracef("Added token '%s' to single-use cache with key '%s'. Lifespan: %d seconds, issuedFor: %s", tokenId, cacheKey, lifespanInSecs, token.getIssuedFor());
        } else {
            logger.warnf("Token '%s' already used when for issuedFor '%s'.", tokenId, token.getIssuedFor());
            return failure("Token reuse detected");
        }
        return true;
    }

    /** @return jti 缓存键前缀（默认为类名小写） */
    protected String getJtiCacheKeyPrefix() {
        return getClass().getSimpleName().toLowerCase();
    }

    /** 校验 aud 声明是否匹配预期受众。 */
    public boolean validateTokenAudience(List<String> expectedAudiences, boolean multipleAudienceAllowed) {
        JsonWebToken token = clientAssertionState.getToken();
        if (!token.hasAnyAudience(expectedAudiences)) {
            return failure("Invalid token audience");
        }

        if (!multipleAudienceAllowed && token.getAudience().length > 1) {
            return failure("Multiple audiences not allowed");
        }

        return true;
    }

    /**
     * By default, symmetric algorithms are not allowed
     * @return false by default
     */
    /** @return 是否允许对称签名算法（默认 false） */
    protected boolean isSymmetricAlgorithmAllowed() {
        return false;
    }

    /** 校验 JWS 头中的 alg 是否为允许的（非 none）非对称算法。 */
    public boolean validateSignatureAlgorithm(String expectedSignatureAlg) {
        JWSInput jws = clientAssertionState.getJws();

        if (jws.getHeader().getAlgorithm() == null) {
            return failure("Invalid signature algorithm");
        }

        String algorithmName = jws.getHeader().getAlgorithm().name();

        if ("none".equalsIgnoreCase(algorithmName)) {
            return failure("Invalid signature algorithm");
        }

        if (!isSymmetricAlgorithmAllowed()) {
            ClientSignatureVerifierProvider signatureProvider = session.getProvider(ClientSignatureVerifierProvider.class, algorithmName);
            if (signatureProvider == null || !signatureProvider.isAsymmetricAlgorithm()) {
                return failure("Invalid signature algorithm");
            }
        }

        if (expectedSignatureAlg != null) {
            if (!expectedSignatureAlg.equals(algorithmName)) {
                return failure("Invalid signature algorithm");
            }
        }

        return true;
    }

    private boolean failure(String errorDescription) {
        failureCallback(errorDescription);
        return false;
    }

    /** 校验失败时的回调（由子类实现具体错误响应）。 */
    protected abstract void failureCallback(String errorDescription);
}
