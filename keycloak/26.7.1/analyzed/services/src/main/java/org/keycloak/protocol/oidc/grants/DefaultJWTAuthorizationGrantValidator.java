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

package org.keycloak.protocol.oidc.grants;

import java.util.Set;

import org.keycloak.OAuth2Constants;
import org.keycloak.authentication.authenticators.client.AbstractBaseJWTValidator;
import org.keycloak.authentication.authenticators.client.ClientAssertionState;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.OIDCConfigAttributes;
import org.keycloak.representations.IDToken;
import org.keycloak.representations.JsonWebToken;

/**
 * JWT 授权断言默认校验器：继承 {@link AbstractBaseJWTValidator}，实现 {@link JWTAuthorizationGrantValidator}（扩展 {@link JWTAuthorizationGrantValidationContext}）。
 *
 * @author rmartinc
 */
public class DefaultJWTAuthorizationGrantValidator extends AbstractBaseJWTValidator implements JWTAuthorizationGrantValidator {

    private final String scope;
    private Set<String> restrictedScopes;

    /** 创建默认 JWT 授权断言校验器 */
    public static DefaultJWTAuthorizationGrantValidator createValidator(KeycloakSession session, String scope, ClientAssertionState clientAssertionState) {
        return new DefaultJWTAuthorizationGrantValidator(session, scope, clientAssertionState);
    }

    protected DefaultJWTAuthorizationGrantValidator(KeycloakSession session, String scope, ClientAssertionState clientAssertionState) {
        super(session, clientAssertionState);
        this.scope = scope;
    }

    /** 校验客户端必须为机密客户端且已启用 JWT 授权模式 */
    public void validateClient() {
        if (clientAssertionState.getClient().isPublicClient()) {
            failureCallback("Public client not allowed to use authorization grant");
        }

        String val = clientAssertionState.getClient().getAttribute(OIDCConfigAttributes.JWT_AUTHORIZATION_GRANT_ENABLED);
        if (!Boolean.parseBoolean(val)) {
            throw new RuntimeException("JWT Authorization Grant is not supported for the requested client");
        }
    }

    /** 校验 JWT 必须包含 iss 声明 */
    public void validateIssuer() {
        if (getJWT().getIssuer() == null) {
            failureCallback("Missing claim: " + OAuth2Constants.ISSUER);
        }
    }

    /** 校验 JWT 必须包含 sub 声明 */
    public void validateSubject() {
        if (getJWT().getSubject() == null) {
            failureCallback("Missing claim: " + IDToken.SUBJECT);
        }
    }

    /** @return 当前断言 JWT */
    @Override
    public JsonWebToken getJWT() {
        return clientAssertionState.getToken();
    }

    /** @return 原始断言字符串 */
    @Override
    public String getAssertion() {
        return clientAssertionState.getClientAssertion();
    }

    /** @return 请求中的 scope 参数 */
    @Override
    public String getScopeParam() {
        return scope;
    }

    /** @return 受限 scope 集合 */
    @Override
    public Set<String> getRestrictedScopes() {
        return restrictedScopes;
    }

    /** 设置受限 scope 集合 @param restrictedScopes scope 集合 */
    @Override
    public void setRestrictedScopes(Set<String> restrictedScopes) {
        this.restrictedScopes = restrictedScopes;
    }

    /** 校验失败时抛出运行时异常 @param errorDescription 错误描述 */
    @Override
    protected void failureCallback(String errorDescription) {
        throw new RuntimeException(errorDescription);
    }
}
