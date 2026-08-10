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

package org.keycloak.quarkus.runtime.integration.resteasy;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.ext.RuntimeDelegate;

import org.keycloak.http.HttpResponse;

import org.jboss.resteasy.reactive.server.core.ResteasyReactiveRequestContext;
import org.jboss.resteasy.reactive.server.vertx.VertxResteasyReactiveRequestContext;

/**
 * 基于 Resteasy Reactive 的 {@link HttpResponse} 实现，
 * 封装状态码、响应头与 Set-Cookie 写入。
 */
public final class QuarkusHttpResponse implements HttpResponse {

    private static final RuntimeDelegate.HeaderDelegate<NewCookie> NEW_COOKIE_HEADER_DELEGATE = RuntimeDelegate.getInstance().createHeaderDelegate(NewCookie.class);

    private final ResteasyReactiveRequestContext requestContext;

    private Set<NewCookie> newCookies;

    /** @param requestContext 当前请求/响应上下文 */
    public QuarkusHttpResponse(ResteasyReactiveRequestContext requestContext) {
        this.requestContext = Objects.requireNonNull(requestContext);
    }

    /** {@inheritDoc} 从 Vert.x 响应读取 HTTP 状态码。 */
    @Override
    public int getStatus() {
        VertxResteasyReactiveRequestContext serverHttpResponse = (VertxResteasyReactiveRequestContext) requestContext.serverResponse();
        return serverHttpResponse.vertxServerResponse().getStatusCode();
    }

    /** {@inheritDoc} 设置 HTTP 状态码。 */
    @Override
    public void setStatus(int statusCode) {
        requestContext.serverResponse().setStatusCode(statusCode);
    }

    /** {@inheritDoc} 追加响应头（允许多值）。 */
    @Override
    public void addHeader(String name, String value) {
        requestContext.serverResponse().addResponseHeader(name, value);
    }

    /** {@inheritDoc} 覆盖设置响应头。 */
    @Override
    public void setHeader(String name, String value) {
        requestContext.serverResponse().setResponseHeader(name, value);
    }

    /** {@inheritDoc} 若尚未设置相同 Cookie 则写入 Set-Cookie 头。 */
    @Override
    public void setCookieIfAbsent(NewCookie newCookie) {
        if (newCookie == null) {
            throw new IllegalArgumentException("Cookie is null");
        }

        if (newCookies == null) {
            newCookies = new HashSet<>();
        }

        if (newCookies.add(newCookie)) {
            String headerValue = NEW_COOKIE_HEADER_DELEGATE.toString(newCookie);
            addHeader(HttpHeaders.SET_COOKIE, headerValue);
        }
    }
}
