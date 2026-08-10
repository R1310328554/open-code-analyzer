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

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.managers.ClientManager;
import org.keycloak.services.managers.RealmManager;

/**
 * 适配器安装客户端注册提供者。
 * <p>提供 GET 端点，返回指定客户端的适配器安装配置 JSON（如 keycloak.json）。</p>
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class AdapterInstallationClientRegistrationProvider implements ClientRegistrationProvider {

    /** Keycloak 会话 */
    private KeycloakSession session;
    /** 事件构建器 */
    private EventBuilder event;
    /** 注册端点认证上下文 */
    private ClientRegistrationAuth auth;

    /** @param session Keycloak 会话 */
    public AdapterInstallationClientRegistrationProvider(KeycloakSession session) {
        this.session = session;
    }

    /**
     * 获取客户端适配器安装配置。
     * @param clientId 客户端标识符
     * @return 安装配置 JSON
     */
    @GET
    @Path("{clientId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("clientId") String clientId) {
        event.event(EventType.CLIENT_INFO);

        ClientModel client = session.getContext().getRealm().getClientByClientId(clientId);
        auth.requireView(client, true);

        ClientManager clientManager = new ClientManager(new RealmManager(session));
        Object rep = clientManager.toInstallationRepresentation(session.getContext().getRealm(), client, session.getContext().getUri().getBaseUri());

        event.client(client.getClientId()).success();
        return Response.ok(rep).build();
    }

    /** {@inheritDoc} 注入认证上下文 */
    @Override
    public void setAuth(ClientRegistrationAuth auth) {
        this.auth = auth;
    }

    /** {@inheritDoc} 返回认证上下文 */
    @Override
    public ClientRegistrationAuth getAuth() {
        return auth;
    }

    /** {@inheritDoc} 注入事件构建器 */
    @Override
    public void setEvent(EventBuilder event) {
        this.event = event;
    }

    /** {@inheritDoc} 返回事件构建器 */
    @Override
    public EventBuilder getEvent() {
        return event;
    }

    @Override
    public void close() {
    }

}
