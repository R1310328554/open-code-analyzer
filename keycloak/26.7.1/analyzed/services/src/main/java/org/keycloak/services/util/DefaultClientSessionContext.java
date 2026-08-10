/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.OAuth2Constants;
import org.keycloak.common.Profile;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.models.utils.RoleUtils;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.rar.AuthorizationRequestContext;
import org.keycloak.rar.AuthorizationRequestSource;
import org.keycloak.util.TokenUtil;

import org.jboss.logging.Logger;

/**
 * {@link ClientSessionContext} 的默认实现，按请求创建，非线程安全。
 * <p>负责解析 OAuth scope 参数、过滤用户有权使用的 client scope、
 * 懒加载角色与协议映射器，并生成写入令牌的 scope 字符串。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DefaultClientSessionContext implements ClientSessionContext {

    private static final Logger logger = Logger.getLogger(DefaultClientSessionContext.class);

    /** 已认证的客户端会话 */
    private final AuthenticatedClientSessionModel clientSession;
    /** 请求中解析出的 client scope 集合 */
    private final Set<ClientScopeModel> requestedScopes;
    /** 当前 Keycloak 会话 */
    private final KeycloakSession session;
    /** 原始 scope 参数字符串 */
    private final String requestedScopeString;

    /** 经权限过滤后允许使用的 client scope（懒加载） */
    private Set<ClientScopeModel> allowedClientScopes;

    /** 当前上下文下的有效角色（懒加载） */
    private Set<RoleModel> roles;
    /** 当前上下文下的协议映射器（懒加载） */
    private Set<ProtocolMapperModel> protocolMappers;

    /** 用户全部角色（含复合角色展开），尚未按 client scope 过滤 */
    private Set<RoleModel> userRoles;

    /** 上下文附加属性（如 offline_access 缓存） */
    private final Map<String, Object> attributes = new HashMap<>();
    /** 允许的 client scope ID 集合（懒加载） */
    private Set<String> clientScopeIds;
    /** 写入令牌的 scope 字符串（懒加载） */
    private String scopeString;

    /** 若不为 null，则仅允许名称在此集合内的 scope */
    private final Set<String> restrictedScopes;

    /** 私有构造；同时将自身注册到 session 属性供后续复用。 */
    private DefaultClientSessionContext(AuthenticatedClientSessionModel clientSession, Set<ClientScopeModel> requestedScopes, Set<String> restrictedScopes, String requestedScopeString, KeycloakSession session) {
        this.requestedScopes = requestedScopes;
        this.restrictedScopes = restrictedScopes;
        this.clientSession = clientSession;
        this.requestedScopeString = requestedScopeString;
        this.session = session;
        this.session.setAttribute(ClientSessionContext.class.getName(), this);
    }


    /**
     * 从客户端会话 note 中的 scope 参数重新计算 client scope 上下文。
     *
     * @param clientSession 已认证客户端会话
     * @param session Keycloak 会话
     * @return 新的上下文实例
     */
    public static DefaultClientSessionContext fromClientSessionScopeParameter(AuthenticatedClientSessionModel clientSession, KeycloakSession session) {
        return fromClientSessionAndScopeParameter(clientSession, clientSession.getNote(OAuth2Constants.SCOPE), session);
    }


    /**
     * 根据显式 scope 参数字符串构建上下文。
     * <p>启用 PARAMETERIZED_SCOPES 时从 RAR 授权请求解析；否则走 {@link TokenManager} 常规逻辑。</p>
     */
    public static DefaultClientSessionContext fromClientSessionAndScopeParameter(AuthenticatedClientSessionModel clientSession, String scopeParam, KeycloakSession session) {
        Stream<ClientScopeModel> requestedScopes;
        if (Profile.isFeatureEnabled(Profile.Feature.PARAMETERIZED_SCOPES)) {
            requestedScopes = AuthorizationContextUtil.getClientScopesStreamFromAuthorizationRequestContextWithClient(session, clientSession.getClient(), scopeParam);
        } else {
            requestedScopes = TokenManager.getRequestedClientScopes(session, scopeParam, clientSession.getClient(), clientSession.getUserSession().getUser());
        }
        return new DefaultClientSessionContext(clientSession, requestedScopes.collect(Collectors.toSet()), null, scopeParam, session);
    }


    /**
     * 直接使用已解析的 client scope 集合构建上下文，可附带 restrictedScopes 白名单。
     */
    public static DefaultClientSessionContext fromClientSessionAndClientScopes(AuthenticatedClientSessionModel clientSession,
            Set<ClientScopeModel> requestedScopes, Set<String> restrictedScopes, KeycloakSession session) {
        return new DefaultClientSessionContext(clientSession, requestedScopes, restrictedScopes, clientSession.getNote(OAuth2Constants.SCOPE), session);
    }

    @Override
    public AuthenticatedClientSessionModel getClientSession() {
        return clientSession;
    }


    @Override
    public Set<String> getClientScopeIds() {
        if (clientScopeIds == null) {
            clientScopeIds = requestedScopes.stream()
                    .map(ClientScopeModel::getId)
                    .collect(Collectors.toSet());
        }
        return clientScopeIds;
    }


    @Override
    public Stream<ClientScopeModel> getClientScopesStream() {
        // 懒加载：过滤出用户有权使用的 client scope
        if (allowedClientScopes == null) {
            allowedClientScopes = requestedScopes.stream().filter(this::isAllowed).collect(Collectors.toSet());
        }
        return allowedClientScopes.stream();
    }

    @Override
    public boolean isOfflineTokenRequested() {
        Boolean offlineAccessRequested = getAttribute(OAuth2Constants.OFFLINE_ACCESS, Boolean.class);
        if (offlineAccessRequested != null) return offlineAccessRequested;

        ClientScopeModel offlineAccessScope = KeycloakModelUtils.getClientScopeByName(clientSession.getRealm(), OAuth2Constants.OFFLINE_ACCESS);
        offlineAccessRequested = offlineAccessScope == null ? false : getClientScopeIds().contains(offlineAccessScope.getId());
        setAttribute(OAuth2Constants.OFFLINE_ACCESS, offlineAccessRequested);
        return offlineAccessRequested;
    }

    @Override
    public Stream<RoleModel> getRolesStream() {
        // 懒加载有效角色
        if (roles == null) {
            roles = loadRoles();
        }
        return roles.stream();
    }


    @Override
    public Stream<ProtocolMapperModel> getProtocolMappersStream() {
        // 懒加载协议映射器
        if (protocolMappers == null) {
            protocolMappers = loadProtocolMappers();
        }
        return protocolMappers.stream();
    }


    private Set<RoleModel> getUserRoles() {
        // 懒加载用户全部角色映射
        if (userRoles == null) {
            userRoles = loadUserRoles();
        }
        return userRoles;
    }


    @Override
    public String getScopeString() {
        if (scopeString == null) {
            scopeString = getScopeString(false);
        }
        return scopeString;
    }

    @Override
    public String getScopeString(boolean ignoreIncludeInTokenScope) {
        if (Profile.isFeatureEnabled(Profile.Feature.PARAMETERIZED_SCOPES)) {
            String scopeParam = buildScopesStringFromAuthorizationRequest(ignoreIncludeInTokenScope);
            logger.tracef("Generated scope param with Parameterized Scopes enabled: %1s", scopeParam);
            String scopeSent = requestedScopeString;
            if (TokenUtil.isOIDCRequest(scopeSent)) {
                scopeParam = TokenUtil.attachOIDCScope(scopeParam);
            }
            return scopeParam;
        }
        // 合并默认/可选 scope 名称；不包含 client 自身
        String scopeParam = getClientScopesStream()
                .filter(((Predicate<ClientScopeModel>) ClientModel.class::isInstance).negate())
                .filter(scope-> scope.isIncludeInTokenScope() || ignoreIncludeInTokenScope)
                .map(ClientScopeModel::getName)
                .collect(Collectors.joining(" "));

        // OIDC 请求需附加 openid scope
        String scopeSent = requestedScopeString;
        if (TokenUtil.isOIDCRequest(scopeSent)) {
            scopeParam = TokenUtil.attachOIDCScope(scopeParam);
        }

        return scopeParam;
    }

    /**
     * 从 {@link AuthorizationRequestContext} 提取 scope 名称。
     * <p>仅保留来源为 SCOPE、允许写入令牌且用户有权使用的条目。</p>
     *
     * @param ignoreIncludeInTokenScope 是否忽略 client scope 的 includeInToken 选项
     * @return 空格分隔的 scope 名称字符串
     */
    private String buildScopesStringFromAuthorizationRequest(boolean ignoreIncludeInTokenScope) {
        return AuthorizationContextUtil.getAuthorizationRequestContextFromScopes(session, clientSession.getClient(), clientSession.getUserSession().getUser(), requestedScopeString).getAuthorizationDetailEntries().stream()
                .filter(authorizationDetails -> authorizationDetails.getSource().equals(AuthorizationRequestSource.SCOPE))
                .filter(authorizationDetails -> authorizationDetails.getClientScope().isIncludeInTokenScope() || ignoreIncludeInTokenScope)
                .filter(authorizationDetails -> isClientScopePermittedForUser(authorizationDetails.getClientScope()))
                .map(authorizationDetails -> authorizationDetails.getAuthorizationDetails().getScopeNameFromCustomData())
                .collect(Collectors.joining(" "));
    }


    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }


    @Override
    public <T> T getAttribute(String name, Class<T> clazz) {
        Object value = attributes.get(name);
        return clazz.cast(value);
    }

    @Override
    public AuthorizationRequestContext getAuthorizationRequestContext() {
        return AuthorizationContextUtil.getAuthorizationRequestContextFromScopes(session, clientSession.getClient(), clientSession.getUserSession().getUser(), requestedScopeString);
    }

    // —— 数据加载与权限过滤 ——

    /** 判断 client scope 是否在 restricted 白名单内且用户有权使用。 */
    private boolean isAllowed(ClientScopeModel clientScope) {
        if (restrictedScopes != null && !restrictedScopes.contains(clientScope.getName())) {
            logger.tracef("Client scope '%s' is not among the restricted scopes list and will not be processed", clientScope.getName());
            return false;
        }

        if (!isClientScopePermittedForUser(clientScope)) {
            if (logger.isTraceEnabled()) {
                logger.tracef("User '%s' not permitted to have client scope '%s'",
                        clientSession.getUserSession().getUser().getUsername(), clientScope.getName());
            }
            return false;
        }

        return true;
    }

    /** 判断用户是否被允许使用该 client scope（基于 scope 角色映射与用户角色交集）。 */
    private boolean isClientScopePermittedForUser(ClientScopeModel clientScope) {
        if (clientScope == null) {
            return false;
        }

        if (clientScope instanceof ClientModel) {
            return true;
        }

        Set<RoleModel> clientScopeRoles = clientScope.getScopeMappingsStream().collect(Collectors.toSet());

        // 无角色映射的 scope 默认允许
        if (clientScopeRoles.isEmpty()) {
            return true;
        }

        // 展开复合角色
        clientScopeRoles = RoleUtils.expandCompositeRoles(clientScopeRoles);

        // 按 audience 请求过滤客户端角色
        if (attributes.get(Constants.REQUESTED_AUDIENCE_CLIENTS) != null) {
            final Set<String> requestedClientIdsFromAudience = Arrays.stream(getAttribute(Constants.REQUESTED_AUDIENCE_CLIENTS, ClientModel[].class))
                    .map(ClientModel::getId)
                    .collect(Collectors.toSet());
            clientScopeRoles.removeIf(role-> role.isClientRole() && !requestedClientIdsFromAudience.contains(role.getContainerId()));
        }

        // scope 角色与用户角色须有交集才允许
        clientScopeRoles.retainAll(getUserRoles());
        return !clientScopeRoles.isEmpty();
    }


    /** 加载当前上下文下用户可获得的访问角色。 */
    private Set<RoleModel> loadRoles() {
        UserModel user = clientSession.getUserSession().getUser();
        ClientModel client = clientSession.getClient();
        return TokenManager.getAccess(user, client, getClientScopesStream());
    }


    /** 加载与客户端协议匹配且已启用的协议映射器。 */
    private Set<ProtocolMapperModel> loadProtocolMappers() {
        String protocol = clientSession.getClient().getProtocol();

        // 防御性处理：协议未配置时回退 openid-connect
        if (protocol == null) {
            logger.warnf("Client '%s' doesn't have protocol set. Fallback to openid-connect. Please fix client configuration",
                    clientSession.getClient().getClientId());
            protocol = OIDCLoginProtocol.LOGIN_PROTOCOL;
        }

        String finalProtocol = protocol;
        return getClientScopesStream()
                .flatMap(clientScope -> clientScope.getProtocolMappersStream()
                        .filter(mapper -> Objects.equals(finalProtocol, mapper.getProtocol()) &&
                                ProtocolMapperUtils.isEnabled(session, mapper)))
                .collect(Collectors.toSet());
    }


    /** 加载用户全部深度角色映射（含复合角色）。 */
    private Set<RoleModel> loadUserRoles() {
        UserModel user = clientSession.getUserSession().getUser();
        return RoleUtils.getDeepUserRoleMappings(user);
    }

}
