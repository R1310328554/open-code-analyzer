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

package org.keycloak.services;

import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.UriInfo;

import org.keycloak.Token;
import org.keycloak.common.ClientConnection;
import org.keycloak.http.HttpRequest;
import org.keycloak.http.HttpResponse;
import org.keycloak.locale.LocaleSelectorProvider;
import org.keycloak.logging.MappedDiagnosticContextProvider;
import org.keycloak.logging.MappedDiagnosticContextUtil;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakUriInfo;
import org.keycloak.models.OrganizationModel;
import org.keycloak.models.Permissions;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.tracing.TracingAttributes;
import org.keycloak.tracing.TracingProvider;
import org.keycloak.urls.UrlType;

import io.opentelemetry.api.trace.Span;

/**
 * {@link KeycloakContext} 默认抽象实现。
 * <p>维护请求级 realm/client/会话/HTTP 上下文、URI 解析、Locale 与分布式追踪属性；子类实现 HTTP 请求/响应的创建方式（Quarkus/Undertow 等）。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public abstract class DefaultKeycloakContext implements KeycloakContext {
    /** 当前请求关联的领域 */
    private RealmModel realm;

    /** 当前客户端（可来自认证会话） */
    private ClientModel client;

    /** 当前组织上下文 */
    private OrganizationModel organization;

    /** 所属 Keycloak 会话 */
    protected KeycloakSession session;

    /** 按 URL 类型缓存的 URI 信息 */
    private Map<UrlType, KeycloakUriInfo> uriInfo;

    /** 当前认证会话 */
    private AuthenticationSessionModel authenticationSession;
    /** 当前用户会话 */
    private UserSessionModel userSession;
    /** 当前 HTTP 请求 */
    private HttpRequest request;
    /** 当前 HTTP 响应 */
    private HttpResponse response;
    /** 客户端连接信息 */
    private ClientConnection clientConnection;
    /** Bearer 令牌（用于从 JWT 解析用户） */
    private Token bearerToken;
    /** 权限检查门面 */
    private final Permissions permissions;

    /** @param session Keycloak 会话 */
    public DefaultKeycloakContext(KeycloakSession session) {
        this.session = session;
        this.permissions = new DefaultPermissions(session, this);
    }

    /** @return 认证服务器前端基 URI */
    @Override
    public URI getAuthServerUrl() {
        return getUri(UrlType.FRONTEND).getBaseUri();
    }

    /** @return 前端 URL 上下文路径 */
    @Override
    public String getContextPath() {
        return getUri(UrlType.FRONTEND).getBaseUri().getPath();
    }

    /** 按 URL 类型获取 Keycloak URI 信息 @param type FRONTEND/BACKEND 等 @return KeycloakUriInfo */
    @Override
    public KeycloakUriInfo getUri(UrlType type) {
        if (uriInfo == null || !uriInfo.containsKey(type)) {
            if (uriInfo == null) {
                uriInfo = new HashMap<>();
            }
            UriInfo info = null;
            try {
                info = getHttpRequest().getUri();
            } catch (ContextNotActiveException e) {
                info = (UriInfo) Proxy.newProxyInstance(UriInfo.class.getClassLoader(), new Class[] { UriInfo.class }, (proxy, method, args) -> {
                    throw new ContextNotActiveException(e); // 无活动 CDI 上下文时抛出 ContextNotActiveException（见 UriInfo/KeycloakUriInfo 文档）
                });
            }
            uriInfo.put(type, new KeycloakUriInfo(session, type, info));
        }
        return uriInfo.get(type);
    }

    /** @return 前端 KeycloakUriInfo */
    @Override
    public KeycloakUriInfo getUri() {
        return getUri(UrlType.FRONTEND);
    }

    /** @return 当前 HTTP 请求头 */
    @Override
    public HttpHeaders getRequestHeaders() {
        return getHttpRequest().getHttpHeaders();
    }

    /** @return 当前领域 */
    @Override
    public RealmModel getRealm() {
        return realm;
    }

    /** 设置当前领域并清除 URI 缓存 @param realm 领域模型 */
    @Override
    public void setRealm(RealmModel realm) {
        this.realm = realm;
        this.uriInfo = null;
        trace(realm);
        mdc().update(this, realm);
    }

    /** @return 当前客户端，未设置时尝试从认证会话获取 */
    @Override
    public ClientModel getClient() {
        if (client == null) {
            client = Optional.ofNullable(authenticationSession)
                    .map(AuthenticationSessionModel::getClient)
                    .orElse(null);
        }
        return client;
    }

    /** 设置当前客户端 @param client 客户端模型 */
    @Override
    public void setClient(ClientModel client) {
        this.client = client;
        trace(client);
        mdc().update(this, client);
    }

    /** @return 当前组织 */
    @Override
    public OrganizationModel getOrganization() {
        return organization;
    }

    /** 设置当前组织 @param organization 组织模型 */
    @Override
    public void setOrganization(OrganizationModel organization) {
        this.organization = organization;
        mdc().update(this, organization);
    }

    /** @return 客户端连接，未设置时创建空实现或子类提供 */
    @Override
    public ClientConnection getConnection() {
        if (clientConnection == null) {
            clientConnection = createClientConnection().orElseGet(() -> new ClientConnection() {});
        }

        return clientConnection;
    }

    /** 解析用户 Locale @param user 用户模型 @return 区域设置 */
    @Override
    public Locale resolveLocale(UserModel user) {
        return session.getProvider(LocaleSelectorProvider.class).resolveLocale(getRealm(), user);
    }

    /** 解析用户 Locale，可选忽略 Accept-Language @param ignoreAcceptLanguageHeader 是否忽略请求头 @return 区域设置 */
    @Override
    public Locale resolveLocale(UserModel user, boolean ignoreAcceptLanguageHeader) {
        return session.getProvider(LocaleSelectorProvider.class).resolveLocale(getRealm(), user, ignoreAcceptLanguageHeader);
    }

    @Override
    public AuthenticationSessionModel getAuthenticationSession() {
        return authenticationSession;
    }

    @Override
    public void setAuthenticationSession(AuthenticationSessionModel authenticationSession) {
        this.authenticationSession = authenticationSession;
        trace(authenticationSession);
        mdc().update(this, authenticationSession);
    }

    @Override
    public HttpRequest getHttpRequest() {
        if (request == null) {
            request = createHttpRequest().orElseThrow(ContextNotActiveException::new);
        }

        return request;
    }

    @Override
    public HttpResponse getHttpResponse() {
        if (response == null) {
            response = createHttpResponse().orElseThrow(ContextNotActiveException::new);
        }

        return response;
    }

    /** 子类可覆盖以提供真实连接信息 @return 客户端连接 Optional */
    protected Optional<ClientConnection> createClientConnection() {
        return Optional.empty();
    }

    /** 创建 HTTP 请求（由运行时子类实现） @return HttpRequest Optional */
    protected abstract Optional<HttpRequest> createHttpRequest();

    /** 创建 HTTP 响应（由运行时子类实现） @return HttpResponse Optional */
    protected abstract Optional<HttpResponse> createHttpResponse();

    /** @return 所属 Keycloak 会话 */
    protected KeycloakSession getSession() {
        return session;
    }

    @Override
    public void setConnection(ClientConnection clientConnection) {
        this.clientConnection = clientConnection;
    }

    @Override
    public void setHttpRequest(HttpRequest httpRequest) {
        this.request = httpRequest;
    }

    @Override
    public void setHttpResponse(HttpResponse httpResponse) {
        this.response = httpResponse;
    }

    @Override
    public UserSessionModel getUserSession() {
        return userSession;
    }

    @Override
    public void setUserSession(UserSessionModel userSession) {
        this.userSession = userSession;
        trace(userSession);
        mdc().update(this, userSession);
    }

    // 分布式追踪
    private Span getCurrentSpan() {
        return session.getProvider(TracingProvider.class).getCurrentSpan();
    }

    private void trace(AuthenticationSessionModel session) {
        if (session != null) {
            var span = getCurrentSpan();
            if (!span.isRecording()) return;

            if (session.getParentSession() != null) {
                span.setAttribute(TracingAttributes.AUTH_SESSION_ID, session.getParentSession().getId());
            }
            if (session.getTabId() != null) {
                span.setAttribute(TracingAttributes.AUTH_TAB_ID, session.getTabId());
            }
        }
    }

    private void trace(RealmModel realm) {
        if (realm != null) {
            var span = getCurrentSpan();
            if (span.isRecording()) {
                span.setAttribute(TracingAttributes.REALM_NAME, realm.getName());
            }
        }
    }

    private void trace(ClientModel client) {
        if (client != null) {
            var span = getCurrentSpan();
            if (span.isRecording()) {
                span.setAttribute(TracingAttributes.CLIENT_ID, client.getClientId());
            }
        }
    }

    private void trace(UserSessionModel userSession) {
        if (userSession != null) {
            var span = getCurrentSpan();
            if (span.isRecording()) {
                span.setAttribute(TracingAttributes.SESSION_ID, userSession.getId());
            }
        }
    }

    @Override
    public void setBearerToken(Token token) {
        this.bearerToken = token;
    }

    @Override
    public Token getBearerToken() {
        return bearerToken;
    }

    /** 从 Bearer JWT 或用户会话解析当前用户 @return 用户模型或 null */
    @Override
    public UserModel getUser() {
        UserModel user = null;

        if (bearerToken instanceof JsonWebToken jwt) {
            String issuer = jwt.getIssuer();
            String realmName = issuer.substring(issuer.lastIndexOf("/") + 1);
            RealmModel realm = session.realms().getRealmByName(realmName);
            String id = jwt.getSubject();

            if (realm != null && id != null) {
                user = session.users().getUserById(realm, id);
            }
        }

        if (user == null) {
            user = userSession == null ? null : userSession.getUser();
        }

        return user;
    }

    private MappedDiagnosticContextProvider mdc() {
        return MappedDiagnosticContextUtil.getMappedDiagnosticContextProvider(session);
    }

    /** @return 权限检查门面 */
    @Override
    public Permissions getPermissions() {
        return permissions;
    }
}
