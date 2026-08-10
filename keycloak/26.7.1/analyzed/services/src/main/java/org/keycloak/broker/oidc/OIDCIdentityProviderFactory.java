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
package org.keycloak.broker.oidc;

import java.io.IOException;
import java.util.Map;

import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.representations.OIDCConfigurationRepresentation;
import org.keycloak.util.JsonSerialization;

/**
 * OpenID Connect 身份代理工厂，provider id 为 oidc。
 * @author Pedro Igor
 */
public class OIDCIdentityProviderFactory extends AbstractIdentityProviderFactory<OIDCIdentityProvider> {

    /** 身份代理提供者标识符。 */
    public static final String PROVIDER_ID = "oidc";

    /** @return 管理控制台显示名称 */
    @Override
    public String getName() {
        return "OpenID Connect v1.0";
    }

    /** @return {@link OIDCIdentityProvider} 实例 */
    @Override
    public OIDCIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new OIDCIdentityProvider(session, new OIDCIdentityProviderConfig(model));
    }

    @Override
    public OIDCIdentityProviderConfig createConfig() {
        return new OIDCIdentityProviderConfig();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public Map<String, String> parseConfig(KeycloakSession session, String config) {
        return parseOIDCConfig(session, config);
    }

    /** 从 OIDC 发现文档解析 issuer、端点与 JWKS 配置。 */
    protected static Map<String, String> parseOIDCConfig(KeycloakSession session, String configString) {
        OIDCConfigurationRepresentation rep;
        try {
            rep = JsonSerialization.readValue(configString, OIDCConfigurationRepresentation.class);
        } catch (IOException e) {
            throw new RuntimeException("failed to load openid connect metadata", e);
        }
        OIDCIdentityProviderConfig config = new OIDCIdentityProviderConfig();
        config.setIssuer(rep.getIssuer());
        config.setLogoutUrl(rep.getLogoutEndpoint());
        config.setAuthorizationUrl(rep.getAuthorizationEndpoint());
        config.setTokenUrl(rep.getTokenEndpoint());
        config.setUserInfoUrl(rep.getUserinfoEndpoint());
        if (rep.getJwksUri() != null) {
            config.setValidateSignature(true);
            config.setUseJwksUrl(true);
            config.setJwksUrl(rep.getJwksUri());
        }

        // 自省 URL 在 RFC8414 中定义但 OIDC 发现规范未强制
        // 部分 IdP 的 well-known 响应可能不包含该字段
        if (rep.getIntrospectionEndpoint() != null) {
            config.setTokenIntrospectionUrl(rep.getIntrospectionEndpoint());
        }
        return config.getConfig();
    }

}
