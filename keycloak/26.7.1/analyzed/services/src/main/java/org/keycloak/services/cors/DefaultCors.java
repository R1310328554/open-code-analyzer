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

package org.keycloak.services.cors;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Response.ResponseBuilder;

import org.keycloak.common.util.CollectionUtil;
import org.keycloak.common.util.UriUtils;
import org.keycloak.http.HttpRequest;
import org.keycloak.http.HttpResponse;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.utils.WebOriginsUtils;
import org.keycloak.representations.AccessToken;

import org.jboss.logging.Logger;

/**
 * 默认 CORS 处理器实现。
 * <p>根据允许的 Origin 集合、预检/认证标志向 HTTP 响应写入 {@code Access-Control-*} 头；Origin 不在白名单且非同源时抛出 {@link ForbiddenException}。</p>
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class DefaultCors implements Cors {

    /** 日志记录器 */
    private static final Logger logger = Logger.getLogger(DefaultCors.class);

    /** 当前 HTTP 请求 */
    private final HttpRequest request;
    /** 当前 HTTP 响应 */
    private final HttpResponse response;
    /** Keycloak 会话 */
    private final KeycloakSession session;
    /** 关联的 JAX-RS 响应构建器（可选） */
    private ResponseBuilder builder;
    /** 预检请求允许的请求头列表（逗号分隔） */
    private final String allowedHeaders;
    /** 允许的 Origin 集合 */
    private Set<String> allowedOrigins;
    /** 预检允许的 HTTP 方法集合 */
    private Set<String> allowedMethods;
    /** 暴露给浏览器的响应头集合 */
    private Set<String> exposedHeaders;

    /** 是否为 CORS 预检请求 */
    private boolean preflight;
    /** 是否允许携带凭证（Cookie/Authorization） */
    private boolean auth;

    /** 包内构造：绑定会话与允许头配置。
     * @param session Keycloak 会话
     * @param allowedHeaders 允许的非 Authorization 请求头
     */
    DefaultCors(KeycloakSession session, String allowedHeaders) {
        this.session = session;
        this.request = session.getContext().getHttpRequest();
        this.response = session.getContext().getHttpResponse();
        this.allowedHeaders = allowedHeaders;
    }

    /** {@inheritDoc} 关联响应构建器 */
    @Override
    public Cors builder(ResponseBuilder builder) {
        this.builder = builder;
        return this;
    }

    /** {@inheritDoc} 标记为预检请求处理 */
    @Override
    public Cors preflight() {
        preflight = true;
        return this;
    }

    /** {@inheritDoc} 允许凭证并在预检中包含 Authorization 头 */
    @Override
    public Cors auth() {
        auth = true;
        return this;
    }

    /** {@inheritDoc} 允许任意 Origin（{@code *}） */
    @Override
    public Cors allowAllOrigins() {
        allowedOrigins = Collections.singleton(ACCESS_CONTROL_ALLOW_ORIGIN_WILDCARD);
        return this;
    }

    /** {@inheritDoc} 从客户端 Web Origins 解析允许列表并校验当前 Origin */
    @Override
    public Cors checkAllowedOrigins(KeycloakSession session, ClientModel client) {
        if (client != null) {
            allowedOrigins = WebOriginsUtils.resolveValidWebOrigins(session, client);
        }
        checkOrigin();
        return this;
    }

    /** {@inheritDoc} 优先使用令牌中的 allowedOrigins，否则回退到客户端配置 */
    @Override
    public Cors checkAllowedOrigins(AccessToken token) {
        if (token != null) {
            allowedOrigins = token.getAllowedOrigins();
            if (allowedOrigins == null || allowedOrigins.isEmpty()) {
                ClientModel client = resolveClient(token);
                if (client != null) {
                    return checkAllowedOrigins(session, client);
                }
            }
        }
        checkOrigin();
        return this;
    }

    /** 根据访问令牌的 issuedFor 解析客户端模型。 */
    private ClientModel resolveClient(AccessToken token) {
        String clientId = token.getIssuedFor();
        if (clientId == null) {
            return null;
        }
        var realm = session.getContext().getRealm();
        return realm == null ? null : realm.getClientByClientId(clientId);
    }

    /** {@inheritDoc} 使用显式 Origin 列表并校验当前请求 */
    @Override
    public Cors checkAllowedOrigins(List<String> allowedOrigins) {
        if (allowedOrigins != null && !allowedOrigins.isEmpty()) {
            this.allowedOrigins = new HashSet<>(allowedOrigins);
        }
        checkOrigin();
        return this;
    }

    /** {@inheritDoc} 设置预检允许的 HTTP 方法 */
    @Override
    public Cors allowedMethods(String... allowedMethods) {
        this.allowedMethods = new HashSet<>(Arrays.asList(allowedMethods));
        return this;
    }

    /** {@inheritDoc} 追加暴露给 JavaScript 的响应头 */
    @Override
    public Cors exposedHeaders(String... exposedHeaders) {
        if (this.exposedHeaders == null) {
            this.exposedHeaders = new HashSet<>();
        }

        this.exposedHeaders.addAll(Arrays.asList(exposedHeaders));

        return this;
    }

    /** {@inheritDoc} 将 CORS 响应头写入 HTTP 响应 */
    @Override
    public void add() {
        if (request == null) {
            throw new IllegalStateException("request is not set");
        }

        if (response == null) {
            throw new IllegalStateException("response is not set");
        }

        String origin = request.getHttpHeaders().getRequestHeaders().getFirst(ORIGIN_HEADER);
        if (origin == null) {
            logger.trace("No Origin header, ignoring");
            return;
        }

        if (!preflight && !isOriginAllowed(origin)) {
            logInvalidOrigin(origin);
            return;
        }

        response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, origin);

        if (preflight) {
            if (allowedMethods != null) {
                response.setHeader(ACCESS_CONTROL_ALLOW_METHODS, CollectionUtil.join(allowedMethods));
            } else {
                response.setHeader(ACCESS_CONTROL_ALLOW_METHODS, DEFAULT_ALLOW_METHODS);
            }
        }

        if (!preflight && exposedHeaders != null) {
            response.setHeader(ACCESS_CONTROL_EXPOSE_HEADERS, CollectionUtil.join(exposedHeaders));
        }

        response.setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, Boolean.toString(auth));

        if (preflight) {
            if (auth) {
                response.setHeader(ACCESS_CONTROL_ALLOW_HEADERS, String.format("%s, %s", allowedHeaders, AUTHORIZATION_HEADER));
            } else {
                response.setHeader(ACCESS_CONTROL_ALLOW_HEADERS, allowedHeaders);
            }
        }

        if (preflight) {
            response.setHeader(ACCESS_CONTROL_MAX_AGE, String.valueOf(DEFAULT_MAX_AGE));
        }
    }

    /** 非预检请求校验 Origin；允许同源或白名单，否则抛出 403。 */
    private void checkOrigin() {
        if (preflight) {
            return;
        }

        String origin = request.getHttpHeaders().getRequestHeaders().getFirst(ORIGIN_HEADER);
        if (origin == null || isOriginAllowed(origin)) {
            return;
        }

        String requestOrigin = UriUtils.getOrigin(session.getContext().getUri().getRequestUri());
        if (origin.equals(requestOrigin)) {
            return;
        }

        logInvalidOrigin(origin, requestOrigin);
        throw new ForbiddenException("Invalid origin");
    }

    /** 判断 Origin 是否在允许集合或通配符中。 */
    private boolean isOriginAllowed(String origin) {
        return allowedOrigins != null
                && (allowedOrigins.contains(origin) || allowedOrigins.contains(ACCESS_CONTROL_ALLOW_ORIGIN_WILDCARD));
    }

    /** DEBUG 级别记录无效 Origin（含请求源对比）。 */
    private void logInvalidOrigin(String origin) {
        if (logger.isDebugEnabled()) {
            String requestOrigin = UriUtils.getOrigin(session.getContext().getUri().getRequestUri());
            logInvalidOrigin(origin, requestOrigin);
        }
    }

    /** 记录 Origin 与请求 URI 源不匹配的细节。 */
    private void logInvalidOrigin(String origin, String requestOrigin) {
        if (logger.isDebugEnabled() && !origin.equals(requestOrigin)) {
            logger.debugv("Invalid CORS request: origin {0} not in allowed origins {1}", origin, allowedOrigins);
        }
    }

    /** {@inheritDoc} 无状态，无需清理 */
    @Override
    public void close() {
    }

}
