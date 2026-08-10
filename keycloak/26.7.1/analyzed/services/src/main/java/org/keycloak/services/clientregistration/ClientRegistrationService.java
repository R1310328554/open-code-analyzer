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

package org.keycloak.services.clientregistration;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.ErrorResponseException;

/**
 * 动态客户端注册 REST 服务入口。
 * <p>按 Provider ID 路由至对应 {@link ClientRegistrationProvider}，并注入认证与事件上下文。</p>
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ClientRegistrationService {

    /** 事件构建器 */
    private final EventBuilder event;

    /** Keycloak 会话 */
    private final KeycloakSession session;

    /**
     * @param session Keycloak 会话
     * @param event 事件构建器
     */
    public ClientRegistrationService(KeycloakSession session, EventBuilder event) {
        this.session = session;
        this.event = event;
    }

    /**
     * 解析并返回指定 Provider 的注册端点资源。
     * @param providerId 注册 Provider ID（如 openid-connect、install）
     * @return Provider 实例（JAX-RS 子资源）
     */
    @Path("{provider}")
    public Object provider(@PathParam("provider") String providerId) {
        checkSsl();

        ClientRegistrationProvider provider = session.getProvider(ClientRegistrationProvider.class, providerId);

        if (provider == null) {
            throw new NotFoundException("Client registration provider not found");
        }

        provider.setEvent(event);
        provider.setAuth(new ClientRegistrationAuth(session, provider, event, providerId));
        return provider;
    }

    /** 若领域要求 SSL，则非 HTTPS 请求抛出 403 */
    private void checkSsl() {
        if (!session.getContext().getUri().getBaseUri().getScheme().equals("https")) {
            if (session.getContext().getRealm().getSslRequired().isRequired(session.getContext().getConnection())) {
                throw new ErrorResponseException("invalid_request", "HTTPS required", Response.Status.FORBIDDEN);
            }
        }
    }

}
