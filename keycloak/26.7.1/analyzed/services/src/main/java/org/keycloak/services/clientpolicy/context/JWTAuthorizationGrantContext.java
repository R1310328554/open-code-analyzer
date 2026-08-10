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
package org.keycloak.services.clientpolicy.context;

import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.JWTAuthorizationGrantValidationContext;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;

/**
 * JWT 授权授予（JWT Authorization Grant）客户端策略上下文。
 * <p>在验证 JWT 授权授予请求时触发，暴露客户端、scope 参数、IdP 别名与验证上下文。</p>
 *
 * @author rmartinc
 */
public class JWTAuthorizationGrantContext implements ClientModelContext, ScopeParameterContext, IdentityProviderContext {

    /** Keycloak 会话 */
    private final KeycloakSession session;
    /** JWT 授权授予验证上下文 */
    private final JWTAuthorizationGrantValidationContext authorizationGrantContext;
    /** 关联身份提供者别名 */
    private final String identityProviderAlias;

    /**
     * @param session Keycloak 会话
     * @param authorizationGrantContext JWT 授权授予验证上下文
     * @param identityProviderAlias IdP 别名
     */
    public JWTAuthorizationGrantContext(KeycloakSession session, JWTAuthorizationGrantValidationContext authorizationGrantContext, String identityProviderAlias) {
        this.session = session;
        this.authorizationGrantContext = authorizationGrantContext;
        this.identityProviderAlias = identityProviderAlias;
    }

    /** {@inheritDoc} @return {@link ClientPolicyEvent#JWT_AUTHORIZATION_GRANT} */
    @Override
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.JWT_AUTHORIZATION_GRANT;
    }

    /** @return JWT 授权授予验证上下文 */
    public JWTAuthorizationGrantValidationContext getAuthorizationGrantContext() {
        return authorizationGrantContext;
    }

    /** {@inheritDoc} @return 当前请求上下文中的客户端 */
    @Override
    public ClientModel getClient() {
        return session.getContext().getClient();
    }

    /** {@inheritDoc} @return 授权授予请求中的 scope 参数 */
    @Override
    public String getScopeParameter() {
        return getAuthorizationGrantContext().getScopeParam();
    }

    /** {@inheritDoc} @return 身份提供者别名 */
    @Override
    public String getIdentityProviderAlias() {
        return identityProviderAlias;
    }
}
