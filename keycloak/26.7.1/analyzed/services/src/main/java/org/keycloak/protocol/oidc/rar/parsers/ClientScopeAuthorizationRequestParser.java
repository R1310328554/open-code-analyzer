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
package org.keycloak.protocol.oidc.rar.parsers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.protocol.oidc.rar.AuthorizationRequestParserProvider;
import org.keycloak.protocol.oidc.rar.model.IntermediaryScopeRepresentation;
import org.keycloak.protocol.oidc.scope.DefaultScopeType;
import org.keycloak.protocol.oidc.scope.InvalidScopeParameterException;
import org.keycloak.protocol.oidc.scope.ParameterizedScopeTypeProvider;
import org.keycloak.rar.AuthorizationDetails;
import org.keycloak.rar.AuthorizationRequestContext;
import org.keycloak.rar.AuthorizationRequestSource;
import org.keycloak.representations.AuthorizationDetailsJSONRepresentation;
import org.keycloak.saml.common.util.StringUtil;

import org.jboss.logging.Logger;

import static org.keycloak.representations.AuthorizationDetailsJSONRepresentation.PARAMETERIZED_SCOPE_RAR_TYPE;
import static org.keycloak.representations.AuthorizationDetailsJSONRepresentation.STATIC_SCOPE_RAR_TYPE;

/**
 * 客户端范围授权请求解析器。
 * <p>将 OAuth {@code scope} 参数（含默认与可选客户端范围、参数化 scope）解析为 RAR {@link AuthorizationRequestContext}。</p>
 *
 * @author <a href="mailto:dgozalob@redhat.com">Daniel Gozalo</a>
 */
public class ClientScopeAuthorizationRequestParser implements AuthorizationRequestParserProvider {

    /** 日志记录器 */
    protected static final Logger logger = Logger.getLogger(ClientScopeAuthorizationRequestParser.class);

    /** Keycloak 会话 */
    private final KeycloakSession session;

    /** @param session Keycloak 会话 */
    public ClientScopeAuthorizationRequestParser(KeycloakSession session) {
        this.session = session;
    }

    /** 解析 scope（无用户上下文，如授权端点） @return 授权请求上下文 */
    @Override
    public AuthorizationRequestContext parseScopes(ClientModel client, String scopeParam) {
        return parseScopes(null, client, scopeParam);
    }

    /**
     * 从请求的 OAuth scope 与默认客户端范围构建 {@link AuthorizationRequestContext}。
     * <p>参数化 scope 会提取并保留参数供后续使用。</p>
     *
     * @param user 登录用户（授权端点等场景可为 null）
     * @param client 请求解析的客户端
     * @param scopeParam 当前请求的 OAuth scope 参数
     * @return 包含 {@link AuthorizationDetails} 列表的授权请求上下文
     */
    @Override
    public AuthorizationRequestContext parseScopes(UserModel user, ClientModel client, String scopeParam) {
        // 将默认客户端范围映射为中间表示并加入集合
        Set<IntermediaryScopeRepresentation> clientScopeModelSet = client.getClientScopes(true).values().stream()
                .filter(clientScopeModel -> !clientScopeModel.isParameterizedScope()) // 参数化 scope 目前仅作为可选范围
                .map(IntermediaryScopeRepresentation::new)
                .collect(Collectors.toSet());

        Set<IntermediaryScopeRepresentation> intermediaryScopeRepresentations = new HashSet<>();
        if (scopeParam != null) {
            // 遍历请求的 scope 并与可选客户端范围匹配
            intermediaryScopeRepresentations = TokenManager.parseScopeParameter(scopeParam).collect(Collectors.toSet()).stream()
                    .map((String requestScope) -> getMatchingClientScope(user, requestScope, client.getClientScopes(false).values()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
        }

        // 合并默认与请求 scope，去重
        intermediaryScopeRepresentations.addAll(clientScopeModelSet);

        // 将中间表示转换为 RAR 上下文中的 AuthorizationDetails
        List<AuthorizationDetails> authorizationDetails = intermediaryScopeRepresentations.stream()
                .map(this::buildAuthorizationDetailsJSONRepresentation)
                .collect(Collectors.toList());

        return new AuthorizationRequestContext(authorizationDetails);

    }

    /**
     * 由 {@link IntermediaryScopeRepresentation} 构建 RAR 中的 {@link AuthorizationDetails}。
     *
     * @param intermediaryScopeRepresentation 中间 scope 表示
     * @return 客户端范围对应的授权详情
     */
    private AuthorizationDetails buildAuthorizationDetailsJSONRepresentation(IntermediaryScopeRepresentation intermediaryScopeRepresentation) {
        AuthorizationDetailsJSONRepresentation representation = new AuthorizationDetailsJSONRepresentation();
        representation.setCustomData("access", Collections.singletonList(intermediaryScopeRepresentation.getRequestedScopeString()));
        representation.setType(STATIC_SCOPE_RAR_TYPE);
        if (intermediaryScopeRepresentation.isParameterized() && intermediaryScopeRepresentation.getParameter() != null) {
            representation.setType(PARAMETERIZED_SCOPE_RAR_TYPE);
            representation.setCustomData("scope_parameter", intermediaryScopeRepresentation.getParameter());
        }
        return new AuthorizationDetails(intermediaryScopeRepresentation.getScope(), AuthorizationRequestSource.SCOPE, representation);
    }

    /**
     * 在可选客户端范围中匹配单个请求 scope。
     * <p>参数化 scope 按注册正则匹配并校验参数；静态 scope 按名称匹配。</p>
     *
     * @param requestScope 请求 scope 之一
     * @return 匹配成功时返回中间表示，否则 empty
     */
    private Optional<IntermediaryScopeRepresentation> getMatchingClientScope(UserModel user, String requestScope, Collection<ClientScopeModel> optionalScopes) {
        for (ClientScopeModel clientScopeModel : optionalScopes) {
            if (clientScopeModel.isParameterizedScope()) {
                String paramValue = clientScopeModel.getParameterFromScope(requestScope).orElse(null);
                if (paramValue == null) {
                    continue;
                }
                try {
                    if (user != null) {
                        resolveType(clientScopeModel).validateParameterWithUser(user, clientScopeModel, paramValue);
                    } else {
                        resolveType(clientScopeModel).validateParameter(clientScopeModel, paramValue);
                    }
                } catch (InvalidScopeParameterException e) {
                    logger.warnf("Invalid scope parameter for '%s': %s", requestScope, e.getMessage());
                    return Optional.empty();
                }
                return Optional.of(new IntermediaryScopeRepresentation(clientScopeModel, paramValue, requestScope));
            } else {
                if (requestScope.equalsIgnoreCase(clientScopeModel.getName())) {
                    return Optional.of(new IntermediaryScopeRepresentation(clientScopeModel));
                }
            }
        }
        return Optional.empty();
    }

    /** 解析参数化 scope 类型提供方 @param clientScopeModel 客户端范围 @return 类型提供方 */
    private ParameterizedScopeTypeProvider resolveType(ClientScopeModel clientScopeModel) {
        String typeId = clientScopeModel.getAttribute(ClientScopeModel.PARAMETERIZED_SCOPE_TYPE);
        if (StringUtil.isNullOrEmpty(typeId)) {
            logger.warnf("Parameterized scope '%s' has no type set, defaulting to '%s'", clientScopeModel.getName(), DefaultScopeType.TYPE);
            typeId = DefaultScopeType.TYPE;
        }
        ParameterizedScopeTypeProvider provider = session.getProvider(ParameterizedScopeTypeProvider.class, typeId);
        if (provider == null) {
            throw new IllegalStateException("Unknown parameterized scope type: " + typeId);
        }
        return provider;
    }

    /** 关闭资源（无操作） */
    @Override
    public void close() {

    }
}
