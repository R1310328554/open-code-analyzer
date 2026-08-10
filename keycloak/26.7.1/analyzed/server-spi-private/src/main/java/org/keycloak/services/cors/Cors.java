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

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;

import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.Provider;
import org.keycloak.representations.AccessToken;
import org.keycloak.utils.KeycloakSessionUtil;

/**
 * CORS（跨域资源共享）提供者：构建并添加 CORS 响应头。
 * <p>支持预检请求、来源校验与凭据控制，链式配置后调用 {@link #add()} 写入响应。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface Cors extends Provider {

    /** 默认预检缓存时长（1 小时，秒）。 */
    long DEFAULT_MAX_AGE = TimeUnit.HOURS.toSeconds(1);
    /** 默认允许的 HTTP 方法。 */
    String DEFAULT_ALLOW_METHODS = "GET, HEAD, OPTIONS";
    /** 默认允许的请求头集合。 */
    Set<String> DEFAULT_ALLOW_HEADERS = Set.of(
            "Origin",
            "Accept",
            "X-Requested-With",
            "Content-Type",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers",
            "DPoP");

    /** Origin 请求头名称。 */
    String ORIGIN_HEADER = "Origin";
    /** Authorization 请求头名称。 */
    String AUTHORIZATION_HEADER = "Authorization";

    /** Access-Control-Allow-Origin 响应头名称。 */
    String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    /** Access-Control-Allow-Methods 响应头名称。 */
    String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
    /** Access-Control-Allow-Headers 响应头名称。 */
    String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    /** Access-Control-Expose-Headers 响应头名称。 */
    String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
    /** Access-Control-Allow-Credentials 响应头名称。 */
    String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
    /** Access-Control-Max-Age 响应头名称。 */
    String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";

    /** 允许任意来源的通配符值。 */
    String ACCESS_CONTROL_ALLOW_ORIGIN_WILDCARD = "*";

    /** 从当前 {@link KeycloakSession} 获取 CORS 构建器。 */
    static Cors builder() {
        KeycloakSession session = KeycloakSessionUtil.getKeycloakSession();
        return session.getProvider(Cors.class);
    }

    /** 绑定 JAX-RS 响应构建器。 */
    Cors builder(ResponseBuilder builder);

    /** 标记为预检（OPTIONS）请求处理。 */
    Cors preflight();

    /** 启用需携带凭据的 CORS 配置。 */
    Cors auth();

    /** 允许任意来源（慎用）。 */
    Cors allowAllOrigins();

    /**
     * 根据客户端配置的 Web Origins 校验请求 Origin；不匹配时抛出 403。
     * Sets the allowed origins from the client's configured web origins and checks the
     * incoming Origin header against them. Throws {@link jakarta.ws.rs.ForbiddenException}
     * (HTTP 403) on a mismatch so the request stops before any side effects. Preflight and
     * same-origin requests pass through without a check.
     */
    /** 基于客户端 Web Origins 校验来源。 */
    Cors checkAllowedOrigins(KeycloakSession session, ClientModel client);

    /** 基于访问令牌中的 allowed-origins 校验来源。 */
    Cors checkAllowedOrigins(AccessToken token);

    /** 基于显式允许来源列表校验。 */
    Cors checkAllowedOrigins(List<String> allowedOrigins);

    /** 设置允许的 HTTP 方法。 */
    Cors allowedMethods(String... allowedMethods);

    /** 设置暴露给浏览器的响应头。 */
    Cors exposedHeaders(String... exposedHeaders);

    /**
     * 将 CORS 头写入当前 {@link org.keycloak.http.HttpResponse}。
     * Add the CORS headers to the current {@link org.keycloak.http.HttpResponse}.
     */
    void add();

    /**
     * 添加 CORS 头并基于给定 builder 构建 {@link Response}。
     * <p>Add the CORS headers to the current server {@link org.keycloak.http.HttpResponse} and returns a {@link Response} based
     * on the given {@code builder}.</p>
     *
     * <p>This is a convenient method to make it easier to return a {@link Response} from methods while at the same time
     * adding the corresponding CORS headers to the underlying server response.</p>
     *
     * @param builder the response builder
     * @return the response built from the response builder
     */
    default Response add(ResponseBuilder builder) {
        if (builder == null) {
            throw new IllegalStateException("builder is not set");
        }

        add();

        return builder.build();
    }
}
