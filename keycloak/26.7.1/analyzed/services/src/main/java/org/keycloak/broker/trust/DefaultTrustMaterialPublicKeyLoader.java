/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.broker.trust;

import org.keycloak.crypto.PublicKeysWrapper;
import org.keycloak.jose.jwk.JSONWebKeySet;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.keys.PublicKeyLoader;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.utils.JWKSHttpUtils;
import org.keycloak.util.JWKSUtils;
import org.keycloak.util.JsonSerialization;
import org.keycloak.utils.StringUtil;

/**
 * 信任材料公钥加载器：从 JWKS URL 拉取或解析配置中的静态 JWKS，
 * 仅返回 {@link JWK.Use#SIG} 用途的 {@link KeyWrapper} 集合。
 */
public class DefaultTrustMaterialPublicKeyLoader implements PublicKeyLoader {

    /** Keycloak 会话，用于 HTTP JWKS 请求。 */
    private final KeycloakSession session;
    /** 信任 IdP 配置。 */
    private final DefaultTrustIdentityProviderConfig config;

    /** @param session 当前会话
     * @param config 信任材料配置 */
    public DefaultTrustMaterialPublicKeyLoader(KeycloakSession session, DefaultTrustIdentityProviderConfig config) {
        this.session = session;
        this.config = config;
    }

    /** 按配置从远程 JWKS 或内嵌 JSON 加载签名公钥；无配置时返回空包装。 */
    @Override
    public PublicKeysWrapper loadKeys() throws Exception {
        if (config.isUseJwksUrl() && StringUtil.isNotBlank(config.getTrustedJwksUrl())) {
            JSONWebKeySet jwks = JWKSHttpUtils.sendJwksRequest(session, config.getTrustedJwksUrl());
            return JWKSUtils.getKeyWrappersForUse(jwks, JWK.Use.SIG, true);
        }

        if (!config.isUseJwksUrl() && StringUtil.isNotBlank(config.getTrustedJwks())) {
            JSONWebKeySet jwks = JsonSerialization.readValue(config.getTrustedJwks(), JSONWebKeySet.class);
            return JWKSUtils.getKeyWrappersForUse(jwks, JWK.Use.SIG, true);
        }

        return PublicKeysWrapper.EMPTY;
    }
}
