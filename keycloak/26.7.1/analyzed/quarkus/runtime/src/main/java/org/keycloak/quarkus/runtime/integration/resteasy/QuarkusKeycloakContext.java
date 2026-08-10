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

import java.util.Optional;

import jakarta.enterprise.context.ContextNotActiveException;

import org.keycloak.common.ClientConnection;
import org.keycloak.http.HttpRequest;
import org.keycloak.http.HttpResponse;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.DefaultKeycloakContext;

import io.vertx.core.http.HttpServerRequest;
import org.jboss.resteasy.reactive.server.core.CurrentRequestManager;
import org.jboss.resteasy.reactive.server.core.ResteasyReactiveRequestContext;

/**
 * Quarkus/RESTEasy Reactive 环境下的 Keycloak 请求上下文：
 * 从当前 REST 请求绑定 {@link HttpRequest}、{@link HttpResponse} 与 {@link ClientConnection}。
 */
public final class QuarkusKeycloakContext extends DefaultKeycloakContext {

    /** @param session 当前 Keycloak 会话 */
    public QuarkusKeycloakContext(KeycloakSession session) {
        super(session);
    }

    /** {@inheritDoc} 基于 Resteasy Reactive 请求上下文创建 Quarkus HTTP 请求包装。 */
    @Override
    protected Optional<HttpRequest> createHttpRequest() {
        return getResteasyReactiveRequestContext().map(QuarkusHttpRequest::new);
    }

    /** {@inheritDoc} 基于 Resteasy Reactive 请求上下文创建 Quarkus HTTP 响应包装。 */
    @Override
    protected Optional<HttpResponse> createHttpResponse() {
        return getResteasyReactiveRequestContext().map(QuarkusHttpResponse::new);
    }

    /** {@inheritDoc} 从 Vert.x 底层请求提取客户端连接信息。 */
    @Override
    protected Optional<ClientConnection> createClientConnection() {
        return getResteasyReactiveRequestContext().map(c -> new QuarkusClientConnection(c.unwrap(HttpServerRequest.class)));
    }

    /** 获取当前线程绑定的 Resteasy Reactive 请求上下文；无活跃请求时返回空。 */
    private Optional<ResteasyReactiveRequestContext> getResteasyReactiveRequestContext() {
        try {
            return Optional.ofNullable(CurrentRequestManager.get());
        } catch (ContextNotActiveException e) {
            return Optional.empty();
        }
    }
}
