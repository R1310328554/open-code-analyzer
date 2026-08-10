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
package org.keycloak.services.util;

import java.util.stream.Stream;

import org.keycloak.common.Profile;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.rar.AuthorizationRequestParserProvider;
import org.keycloak.protocol.oidc.rar.parsers.ClientScopeAuthorizationRequestParserProviderFactory;
import org.keycloak.rar.AuthorizationDetails;
import org.keycloak.rar.AuthorizationRequestContext;
import org.keycloak.rar.AuthorizationRequestSource;


/**
 * 授权请求上下文工具类。
 * <p>统一从 OAuth2 scope 参数构建 {@link AuthorizationRequestContext} 的入口，
 * 便于在任意位置引用参数化 scope 解析结果。</p>
 *
 * @author <a href="mailto:dgozalob@redhat.com">Daniel Gozalo</a>
 */
public class AuthorizationContextUtil {

    /**
     * 从 OAuth2 scope 参数构建仅含 scope 条目的 {@link AuthorizationRequestContext}。
     * @param session Keycloak 会话
     * @param client 客户端
     * @param scope scope 字符串
     * @return 授权请求上下文
     */
    public static AuthorizationRequestContext getAuthorizationRequestContextFromScopes(KeycloakSession session, ClientModel client, String scope) {
        return getAuthorizationRequestContextFromScopes(session, client, null, scope);
    }

    /**
     * 从 OAuth2 scope 参数构建授权请求上下文（含用户信息）。
     * @param session Keycloak 会话
     * @param client 客户端
     * @param user 用户（可为 null）
     * @param scope scope 字符串
     * @return 授权请求上下文
     */
    public static AuthorizationRequestContext getAuthorizationRequestContextFromScopes(KeycloakSession session, ClientModel client, UserModel user, String scope) {
        if (!Profile.isFeatureEnabled(Profile.Feature.PARAMETERIZED_SCOPES)) {
            throw new RuntimeException("The Parameterized Scopes feature is not enabled and the AuthorizationRequestContext hasn't been generated");
        }
        AuthorizationRequestParserProvider clientScopeParser = session.getProvider(AuthorizationRequestParserProvider.class,
                ClientScopeAuthorizationRequestParserProviderFactory.CLIENT_SCOPE_PARSER_ID);

        if (clientScopeParser == null) {
            throw new RuntimeException(String.format("No provider found for authorization requests parser %1s",
                    ClientScopeAuthorizationRequestParserProviderFactory.CLIENT_SCOPE_PARSER_ID));
        }

        return clientScopeParser.parseScopes(user, client, scope);
    }

    /**
     * 在 scope 上下文基础上追加客户端引用的扩展方法。
     * @param session Keycloak 会话
     * @param client 客户端
     * @param scope scope 字符串
     * @return 含客户端条目的授权请求上下文
     */
    public static AuthorizationRequestContext getAuthorizationRequestContextFromScopesWithClient(KeycloakSession session, ClientModel client, String scope) {
        AuthorizationRequestContext authorizationRequestContext = getAuthorizationRequestContextFromScopes(session, client, null, scope);
        authorizationRequestContext.getAuthorizationDetailEntries().add(new AuthorizationDetails(client));
        return authorizationRequestContext;
    }

    /**
     * 返回含客户端的 {@link AuthorizationDetails} 流。
     * @param session Keycloak 会话
     * @param client 客户端
     * @param scope scope 字符串
     * @return AuthorizationDetails 流
     */
    public static Stream<AuthorizationDetails> getAuthorizationRequestsStreamFromScopesWithClient(KeycloakSession session, ClientModel client, String scope) {
        AuthorizationRequestContext authorizationRequestContext = getAuthorizationRequestContextFromScopesWithClient(session, client, scope);
        return authorizationRequestContext.getAuthorizationDetailEntries().stream();
    }

    /**
     * 从授权请求上下文中提取全部 {@link ClientScopeModel} 流。
     * @param session Keycloak 会话
     * @param client 客户端
     * @param scope scope 字符串
     * @return ClientScopeModel 流
     */
    public static Stream<ClientScopeModel> getClientScopesStreamFromAuthorizationRequestContextWithClient(KeycloakSession session, ClientModel client, String scope) {
        return getAuthorizationRequestContextFromScopesWithClient(session, client, scope).getAuthorizationDetailEntries().stream()
                .filter(authorizationDetails -> authorizationDetails.getSource() == AuthorizationRequestSource.SCOPE)
                .map(AuthorizationDetails::getClientScope);
    }
}
