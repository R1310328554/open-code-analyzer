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
package org.keycloak.broker.oauth;

import java.io.IOException;
import java.util.Map;

import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.oidc.OIDCIdentityProviderConfig;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.protocol.oidc.representations.OIDCConfigurationRepresentation;
import org.keycloak.util.JsonSerialization;
import org.keycloak.utils.StringUtil;

/**
 * OAuth 2.0 身份代理工厂：provider id 为 oauth2。
 * <p>支持从 OIDC 发现文档导入端点配置，并校验 UserInfo 与 claim 必填项。</p>
 */
public class OAuth2IdentityProviderFactory extends AbstractIdentityProviderFactory<OAuth2IdentityProvider> {

    /** 身份代理提供者标识符。 */
    public static final String PROVIDER_ID = "oauth2";

    /** @return provider id oauth2 */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** @return 管理控制台显示名称 */
    @Override
    public String getName() {
        return "OAuth v2";
    }

    /** @return {@link OAuth2IdentityProvider} 实例 */
    @Override
    public OAuth2IdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new OAuth2IdentityProvider(session, createConfig(model));
    }

    @Override
    public IdentityProviderModel createConfig() {
        return createConfig(null);
    }

    /** 创建配置并校验 UserInfo URL 与必填 claim 名称。 */
    private OAuth2IdentityProviderConfig createConfig(IdentityProviderModel model) {
        return new OAuth2IdentityProviderConfig(model) {
            @Override
            public void validate(RealmModel realm) {
                if (StringUtil.isBlank(getUserInfoUrl())) {
                    throw new IllegalArgumentException("User Info URL not provided");
                }

                if (StringUtil.isBlank(getUserIDClaim())) {
                    throw new IllegalArgumentException("User ID Claim not provided");
                }

                if (StringUtil.isBlank(getUserNameClaim())) {
                    throw new IllegalArgumentException("User Name Claim not provided");
                }
                if (StringUtil.isBlank(getEmailClaim())) {
                    throw new IllegalArgumentException("User Email Claim not provided");
                }

                super.validate(realm);
            }
        };
    }

    /** 从 OIDC 发现 JSON 解析 authorization/token/userinfo 等端点。 */
    @Override
    public Map<String, String> parseConfig(KeycloakSession session, String rawConfig) {
        OIDCConfigurationRepresentation rep;
        try {
            rep = JsonSerialization.readValue(rawConfig, OIDCConfigurationRepresentation.class);
        } catch (IOException e) {
            throw new RuntimeException("failed to load openid connect metadata", e);
        }
        OIDCIdentityProviderConfig config = new OIDCIdentityProviderConfig();
        config.setIssuer(rep.getIssuer());
        config.setAuthorizationUrl(rep.getAuthorizationEndpoint());
        config.setTokenUrl(rep.getTokenEndpoint());
        config.setUserInfoUrl(rep.getUserinfoEndpoint());

        // 自省 URL 在 RFC8414 中定义但 OIDC 发现规范未强制，部分服务器可能不提供
        // 因此 well-known 响应中可能缺失该字段
        if (rep.getIntrospectionEndpoint() != null) {
            config.setTokenIntrospectionUrl(rep.getIntrospectionEndpoint());
        }
        return config.getConfig();
    }
}
