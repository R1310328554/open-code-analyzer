/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.testsuite.broker.oidc;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import jakarta.ws.rs.core.UriBuilder;

import org.keycloak.broker.oidc.KeycloakOIDCIdentityProvider;
import org.keycloak.broker.oidc.KeycloakOIDCIdentityProviderFactory;
import org.keycloak.broker.oidc.OIDCIdentityProviderConfig;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.util.JsonSerialization;

/**
 * 可配置的 Keycloak OIDC 身份提供者工厂，支持忽略 max_age、单 refresh token 及自定义用户名等测试场景。
 */
public class TestKeycloakOidcIdentityProviderFactory extends KeycloakOIDCIdentityProviderFactory {

    /** 提供商标识符。 */
    public static final String ID = "test-keycloak-oidc";
    /** 配置项：是否在授权 URL 中忽略 max_age 参数。 */
    public static final String IGNORE_MAX_AGE_PARAM = "ignore-max-age-param";
    /** 配置项：是否仅首次登录返回 refresh token。 */
    public static final String USE_SINGLE_REFRESH_TOKEN = "use-single-refresh-token";
    /** 配置项：覆盖联合身份的用户名。 */
    public static final String PREFERRED_USERNAME = "preferred-username";

    /**
     * 在身份提供者表示中启用忽略 max_age 参数。
     *
     * @param rep 身份提供者表示对象
     */
    public static void setIgnoreMaxAgeParam(IdentityProviderRepresentation rep) {
        rep.getConfig().put(IGNORE_MAX_AGE_PARAM, Boolean.TRUE.toString());
    }

    /** {@inheritDoc} 返回 {@link #ID}。 */
    @Override
    public String getId() {
        return ID;
    }

    /**
     * {@inheritDoc}
     * 创建匿名子类，实现 max_age 忽略、refresh token 去重及 preferred_username 覆盖逻辑。
     */
    @Override
    public KeycloakOIDCIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new KeycloakOIDCIdentityProvider(session, new OIDCIdentityProviderConfig(model)) {

            /** 已登录过的用户名集合，用于单 refresh token 测试。 */
            private static final Set<String> usernames = new HashSet<>();

            @Override
            public BrokeredIdentityContext getFederatedIdentity(String response) {
                BrokeredIdentityContext context = super.getFederatedIdentity(response);
                String preferredUsername = getPreferredUsername();

                if (preferredUsername != null) {
                    context.setUsername(preferredUsername);
                }
                if (Boolean.valueOf(model.getConfig().get(USE_SINGLE_REFRESH_TOKEN))) {
                    // 仅首次登录保留 refresh token，后续登录将其移除
                    if (!usernames.add(context.getUsername())) {
                        try {
                            AccessTokenResponse tokenResponse = JsonSerialization.readValue(context.getToken(), AccessTokenResponse.class);
                            tokenResponse.setRefreshToken(null);
                            context.setToken(JsonSerialization.writeValueAsString(tokenResponse));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                return context;
            }

            @Override
            protected UriBuilder createAuthorizationUrl(AuthenticationRequest request) {
                AuthenticationSessionModel authSession = request.getAuthenticationSession();
                String maxAge = authSession.getClientNote(OIDCLoginProtocol.MAX_AGE_PARAM);

                try {
                    if (isIgnoreMaxAgeParam()) {
                        authSession.removeClientNote(OIDCLoginProtocol.MAX_AGE_PARAM);
                    }
                    return super.createAuthorizationUrl(request);
                } finally {
                    // 恢复 max_age 客户端备注，避免影响后续流程
                    authSession.setClientNote(OIDCLoginProtocol.MAX_AGE_PARAM, maxAge);
                }
            }

            /** 是否配置为忽略 max_age 参数。 */
            private boolean isIgnoreMaxAgeParam() {
                return Boolean.parseBoolean(model.getConfig().getOrDefault(IGNORE_MAX_AGE_PARAM, Boolean.FALSE.toString()));
            }

            /** 从配置读取 preferred_username 覆盖值。 */
            private String getPreferredUsername() {
                return model.getConfig().get(PREFERRED_USERNAME);
            }
        };
    }

    /** {@inheritDoc} 返回带自定义图标 CSS 类的配置对象。 */
    @Override
    public OIDCIdentityProviderConfig createConfig() {
        return new OIDCIdentityProviderConfig(super.createConfig()) {
            @Override
            public String getDisplayIconClasses() {
                return "my-custom-idp-icon";
            }
        };
    }
}
