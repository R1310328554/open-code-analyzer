/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.organization.protocol.mappers.oidc;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import jakarta.ws.rs.BadRequestException;

import org.keycloak.common.util.TriFunction;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeDecorator;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.OrganizationModel;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserModel;
import org.keycloak.organization.utils.Organizations;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.OIDCLoginProtocolFactory;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.utils.StringUtil;

import static org.keycloak.models.ClientScopeModel.VALUE_SEPARATOR;
import static org.keycloak.organization.utils.Organizations.getProvider;
import static org.keycloak.utils.StringUtil.isBlank;

/**
 * 处理 {@link OIDCLoginProtocolFactory#ORGANIZATION} 客户端 scope 的枚举及工具方法。
 * <p>{@link OrganizationScope} 行为类似参数化 scope：客户端以不同格式请求 organization scope 时，授予的组织访问范围随之变化（全部/指定/任一）。</p>
 */
public enum OrganizationScope {

    /** 映射用户所属的全部组织；客户端请求此 scope 时授予用户所有成员组织。 */
    ALL("*"::equals,
            (user, scopes, session) -> {
                if (user == null) {
                    return Stream.empty();
                }
                return getProvider(session).getByMember(user);
            },
            (organizations) -> true,
            (session, current, previous) -> {
                OrganizationScope currentScope = valueOfScope(session, current);
                
                // 仅处理 organization scope，忽略其他 scope
                if (currentScope == null) {
                    return null;
                }
                
                // 刷新令牌场景拒绝 ANY scope（需用户选择，刷新时不可用）
                if (isAnyScope(currentScope)) {
                    throw new BadRequestException("ANY organization scope is not allowed in this context");
                }
                
                // 允许 SINGLE（收窄）或 ALL（维持）scope
                return current;
            }),

    /**
     * 映射用户所属的一个或多个指定组织（按别名）；例如 {@code organization:org-a organization:org-b}。
     * 别名不存在或用户非成员时拒绝请求。
     */
    SPECIFIC(StringUtil::isNotBlank,
            (user, scopes, session) -> {
                List<OrganizationModel> organizations = parseScopeParameter(session, scopes)
                        .map((String scope) -> parseScopeValue(session, scope))
                        .map(alias -> getProvider(session).getByAlias(alias))
                        .filter(Objects::nonNull)
                        .filter(org -> user == null || org.isMember(user))
                        .toList();

                return organizations.stream();
            },
            (organizations) -> organizations.findAny().isPresent(),
            (session, current, previous) -> {
                if (current.equals(previous)) {
                    return current;
                }

                OrganizationScope currentScope = valueOfScope(session, current);
                
                if (OrganizationScope.ALL.equals(currentScope)) {
                    return previous;
                }

                // 当前为 ANY（organization）而先前为 SINGLE（organization:foo）时保留先前的具体组织
                // 当前 scope 仅为 organization 且先前含 organization:foo 等具体值时保留先前组织
                if (isAnyScope(currentScope)) {
                    return previous;
                }

                return null;
            }),

    /**
     * 映射单个组织：用户仅属一个组织时直接授予；属多个组织时需用户选择或从 client session 读取已选组织。
     */
    ANY(""::equals,
            (user, scopes, session) -> {
                if (user == null) {
                    return Stream.empty();
                }

                List<OrganizationModel> organizations = getProvider(session).getByMember(user).filter(OrganizationModel::isEnabled).toList();

                if (organizations.size() == 1) {
                    return organizations.stream();
                }

                ClientSessionContext context = (ClientSessionContext) session.getAttribute(ClientSessionContext.class.getName());

                if (context == null) {
                    return Stream.empty();
                }

                AuthenticatedClientSessionModel clientSession = context.getClientSession();
                String orgId = clientSession.getNote(OrganizationModel.ORGANIZATION_ATTRIBUTE);

                if (orgId == null) {
                    return Stream.empty();
                }

                return organizations.stream().filter(o -> o.getId().equals(orgId));
            },
            (organizations) -> true,
            (session, current, previous) -> {
                if (current.equals(previous)) {
                    return current;
                }

                OrganizationScope currentScope = valueOfScope(session, current);
                
                if (OrganizationScope.ALL.equals(currentScope)) {
                    return previous;
                }

                return null;
            });

    private static final String ORGANIZATION_SCOPES_SESSION_ATTRIBUTE = "kc.org.client.scope";
    private static final String UNSUPPORTED_ORGANIZATION_SCOPES_ATTRIBUTE = "kc.org.client.scope.unsupported";
    private static final Pattern SCOPE_PATTERN = Pattern.compile("(.*)" + VALUE_SEPARATOR + "(.*)");
    private static final String EMPTY_SCOPE = "";

    /**
     * 判断给定 scope 是否为 {@link OrganizationScope#ANY}。
     * <p>因 ALL/SPECIFIC 在 ANY 之前定义，其初始化 lambda 不能直接引用 ANY 常量；通过本静态方法（定义于所有常量之后）规避前向引用编译错误。</p>
     */
    private static boolean isAnyScope(OrganizationScope scope) {
        return OrganizationScope.ANY.equals(scope);
    }


    /** scope 原始值匹配谓词，例如 organization:alias 中的 alias 部分。 */
    private final Predicate<String> valueMatcher;

    /** 根据 scope 值解析用户可访问的组织流。 */
    private final TriFunction<UserModel, String, KeycloakSession, Stream<OrganizationModel>> valueResolver;

    /** 校验 scope 解析结果是否映射到有效组织。 */
    private final Predicate<Stream<OrganizationModel>> valueValidator;

    /** 在 scope 格式变更（如刷新令牌）时解析应保留的 scope 名称。 */
    private final TriFunction<KeycloakSession, String, String, String> nameResolver;

    OrganizationScope(Predicate<String> valueMatcher, TriFunction<UserModel, String, KeycloakSession, Stream<OrganizationModel>> valueResolver, Predicate<Stream<OrganizationModel>> valueValidator, TriFunction<KeycloakSession, String, String, String> nameResolver) {
        this.valueMatcher = valueMatcher;
        this.valueResolver = valueResolver;
        this.valueValidator = valueValidator;
        this.nameResolver = nameResolver;
    }

    /**
     * 根据 scope 字符串解析用户可访问的组织。
     * @param user 用户，部分 scope 解析时可为 null
     * @param scope scope 字符串
     * @param session Keycloak 会话
     * @return 解析出的组织流，无匹配时为空
     */
    public Stream<OrganizationModel> resolveOrganizations(UserModel user, String scope, KeycloakSession session) {
        if (!Organizations.isEnabled(session)) {
            return Stream.empty();
        }
        return valueResolver.apply(user, Optional.ofNullable(scope).orElse(EMPTY_SCOPE), session).filter(OrganizationModel::isEnabled);
    }

    /**
     * 从认证会话请求的 scope 解析用户所属组织。
     * @param user 用户
     * @param session Keycloak 会话
     * @return 组织流
     */
    public Stream<OrganizationModel> resolveOrganizations(UserModel user, KeycloakSession session) {
        return resolveOrganizations(user, getRequestedScopes(session), session);
    }

    /**
     * 从认证会话 scope 解析组织（不指定用户）。
     * @param session Keycloak 会话
     * @return 组织流
     */
    public Stream<OrganizationModel> resolveOrganizations(KeycloakSession session) {
        return resolveOrganizations(null, session);
    }

    /**
     * 将 scope 名称转换为带组织语义的 {@link ClientScopeModel}。
     * @param name scope 名称
     * @param user 用户
     * @param session Keycloak 会话
     * @return 客户端 scope 模型，无效时 null
     */
    public ClientScopeModel toClientScope(String name, UserModel user, KeycloakSession session) {
        OrganizationScope scope = valueOfScope(session, name);

        if (scope == null) {
            return null;
        }

        Stream<OrganizationModel> organizations = scope.resolveOrganizations(user, name, session);

        if (valueValidator.test(organizations)) {
            return new ClientScopeDecorator(resolveClientScope(session, name), name);
        }

        return null;
    }

    /**
     * 根据新请求的 scope 集合与先前 scope 名称，解析刷新时应保留的 scope 名。
     * @param session Keycloak 会话
     * @param scopes 新 scope 集合
     * @param previous 先前 scope 名称
     * @return 解析后的 scope 名称，无法映射时 null
     */
    public String resolveName(KeycloakSession session, Set<String> scopes, String previous) {
        for (String scope : scopes) {
            String resolved = nameResolver.apply(session, scope, previous);

            if (resolved == null) {
                continue;
            }

            return resolved;
        }

        return null;
    }

    /**
     * 从原始 scope 字符串解析 {@link OrganizationScope} 枚举值。
     * @param session Keycloak 会话
     * @param rawScope 原始 scope 字符串
     * @return 匹配的枚举值，无匹配时 null
     */
    public static OrganizationScope valueOfScope(KeycloakSession session, String rawScope) {
        return parseScopeParameter(session, Optional.ofNullable(rawScope).orElse(EMPTY_SCOPE))
                .map(s -> {
                    for (OrganizationScope scope : values()) {
                        if (scope.valueMatcher.test(parseScopeValue(session, s))) {
                            return scope;
                        }
                    }
                    return null;
                }).filter(Objects::nonNull)
                .findAny()
                .orElse(null);
    }

    /**
     * 从认证会话请求的 scope 解析 {@link OrganizationScope}（带会话缓存）。
     * @param session Keycloak 会话
     * @return 组织 scope 枚举值
     */
    public static OrganizationScope valueOfScope(KeycloakSession session) {
        OrganizationScope value = session.getAttribute(OrganizationScope.class.getName(), OrganizationScope.class);

        if (value != null) {
            return value;
        }

        value = valueOfScope(session, getRequestedScopes(session));

        if (value != null) {
            session.setAttribute(OrganizationScope.class.getName(), value);
        }

        return value;
    }

    private static String getRequestedScopes(KeycloakSession session) {
        AuthenticationSessionModel authSession = session.getContext().getAuthenticationSession();

        if (authSession == null) {
            return EMPTY_SCOPE;
        }

        String requestedScopes = authSession.getClientNote(OIDCLoginProtocol.SCOPE_PARAM);

        return Optional.ofNullable(requestedScopes).orElse(EMPTY_SCOPE);
    }

    private static String parseScopeValue(KeycloakSession session, String scope) {
        ClientScopeModel clientScope = resolveClientScope(session, scope);

        if (clientScope != null) {
            if (scope.equals(clientScope.getName())) {
                return "";
            }
        }

        Matcher matcher = SCOPE_PATTERN.matcher(scope);

        if (matcher.matches()) {
            return matcher.group(2);
        }

        return null;
    }

    private static Stream<String> parseScopeParameter(KeycloakSession session, String rawScope) {
        return TokenManager.parseScopeParameter(rawScope)
                .filter(scope -> resolveClientScope(session, scope) != null);
    }

    private static ClientScopeModel resolveClientScope(KeycloakSession session, String scope) {
        if (isBlank(scope)) {
            return null;
        }

        ClientModel client = session.getContext().getClient();

        if (client == null) {
            return null;
        }

        if (session.getAttributeOrDefault(UNSUPPORTED_ORGANIZATION_SCOPES_ATTRIBUTE, Set.of()).contains(scope)) {
            // scope 已处理且不支持组织映射
            return null;
        }

        Set<ClientScopeModel> organizationScopes = session.getAttributeOrDefault(ORGANIZATION_SCOPES_SESSION_ATTRIBUTE, Set.of());

        for (ClientScopeModel clientScope : organizationScopes) {
            if (scope.equals(clientScope.getName()) || scope.startsWith(clientScope.getName() + VALUE_SEPARATOR)) {
                // scope 已处理且支持组织映射
                return clientScope;
            }
        }

        Matcher matcher = SCOPE_PATTERN.matcher(scope);

        if (matcher.matches()) {
            scope = matcher.group(1);
        }

        ClientScopeModel clientScope = getClientScope(client, scope);

        if (clientScope != null) {
            Stream<String> mappers = clientScope.getProtocolMappersStream().map(ProtocolMapperModel::getProtocolMapper);

            if (mappers.noneMatch(OrganizationMembershipMapper.PROVIDER_ID::equals)) {
                Set<String> nonOrganizationScopes = session.getAttributeOrDefault(UNSUPPORTED_ORGANIZATION_SCOPES_ATTRIBUTE, Set.of());

                if (nonOrganizationScopes.isEmpty()) {
                    nonOrganizationScopes = new HashSet<>();
                }

                // 非 organization scope，缓存以避免重复处理
                nonOrganizationScopes.add(scope);
                session.setAttribute(UNSUPPORTED_ORGANIZATION_SCOPES_ATTRIBUTE, nonOrganizationScopes);

                return null;
            }

            organizationScopes = session.getAttributeOrDefault(ORGANIZATION_SCOPES_SESSION_ATTRIBUTE, Set.of());

            if (organizationScopes.isEmpty()) {
                organizationScopes = new HashSet<>();
            }

            organizationScopes.add(clientScope);
            // 支持组织的 scope，缓存以避免重复处理
            session.setAttribute(ORGANIZATION_SCOPES_SESSION_ATTRIBUTE, organizationScopes);
        }

        return clientScope;
    }

    private static ClientScopeModel getClientScope(ClientModel client, String scope) {
        ClientScopeModel clientScope = client.getClientScopes(false).get(scope);

        if (clientScope == null) {
            clientScope = client.getClientScopes(true).get(scope);
        }

        return clientScope;
    }
}
