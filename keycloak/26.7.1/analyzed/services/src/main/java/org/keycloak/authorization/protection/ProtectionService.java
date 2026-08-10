/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.authorization.protection;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response.Status;

import org.keycloak.OAuthErrorException;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.admin.ResourceSetService;
import org.keycloak.authorization.common.KeycloakIdentity;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.protection.permission.PermissionService;
import org.keycloak.authorization.protection.permission.PermissionTicketService;
import org.keycloak.authorization.protection.policy.UserManagedPermissionService;
import org.keycloak.authorization.protection.resource.ResourceService;
import org.keycloak.common.ClientConnection;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.ErrorResponseException;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.AdminEventBuilder;

/**
 * UMA Protection API 根服务：挂载 resource_set、permission、ticket、uma-policy 子路径。
 * <p>校验 bearer 身份与 uma_protection scope（资源服务器自身除外）。</p>
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class ProtectionService {

    private final KeycloakSession session;
    private final AuthorizationProvider authorization;

    protected final ClientConnection clientConnection;

    /** @param authorization 授权提供者 */
    public ProtectionService(AuthorizationProvider authorization) {
        this.session = authorization.getKeycloakSession();
        this.authorization = authorization;
        this.clientConnection = session.getContext().getConnection();
    }

    @Path("/resource_set")
    /** /resource_set：资源注册与管理（需 uma_protection）。 */
    public Object resource() {
        KeycloakIdentity identity = createIdentity(true);
        ResourceServer resourceServer = getResourceServer(identity);
        ResourceSetService resourceManager = new ResourceSetService(this.session, resourceServer, this.authorization, null, createAdminEventBuilder(identity, resourceServer));
        return new ResourceService(authorization, resourceServer, identity, resourceManager);
    }

    /** 为 protection 操作构建管理审计事件。 */
    private AdminEventBuilder createAdminEventBuilder(KeycloakIdentity identity, ResourceServer resourceServer) {
        RealmModel realm = authorization.getRealm();
        ClientModel client = realm.getClientById(resourceServer.getClientId());
        KeycloakSession keycloakSession = authorization.getKeycloakSession();
        UserModel serviceAccount = keycloakSession.users().getServiceAccount(client);
        AdminEventBuilder adminEvent = new AdminEventBuilder(realm, new AdminAuth(realm, identity.getAccessToken(), serviceAccount, client), keycloakSession, clientConnection);
        return adminEvent;
    }

    @Path("/permission")
    /** /permission：创建 permission ticket。 */
    public Object permission() {
        KeycloakIdentity identity = createIdentity(false);

        return new PermissionService(identity, getResourceServer(identity), this.authorization);
    }
    
    @Path("/permission/ticket")
    /** /permission/ticket：权限票据管理（需 uma_protection）。 */
    public Object ticket() {
        KeycloakIdentity identity = createIdentity(true);

        return new PermissionTicketService(identity, getResourceServer(identity), this.authorization);
    }
    
    @Path("/uma-policy")
    /** /uma-policy：用户托管 UMA 策略管理。 */
    public Object policy() {
        KeycloakIdentity identity = createIdentity(false);

        return new UserManagedPermissionService(identity, getResourceServer(identity), this.authorization, createAdminEventBuilder(identity, getResourceServer(identity)));
    }

    /** 解析 bearer 身份并按需校验 uma_protection 客户端角色。 */
    private KeycloakIdentity createIdentity(boolean checkProtectionScope) {
        KeycloakIdentity identity = new KeycloakIdentity(this.authorization.getKeycloakSession());
        ResourceServer resourceServer = getResourceServer(identity);
        KeycloakSession keycloakSession = authorization.getKeycloakSession();
        RealmModel realm = keycloakSession.getContext().getRealm();
        ClientModel client = realm.getClientById(resourceServer.getClientId());

        if (checkProtectionScope) {
            if (identity.isResourceServer()) {
                // 身份为资源服务器自身时无需 uma_protection scope
                return identity;
            }

            if (!identity.hasClientRole(client.getClientId(), "uma_protection")) {
                throw new ErrorResponseException(OAuthErrorException.INVALID_SCOPE, "Requires uma_protection scope.", Status.FORBIDDEN);
            }
        }

        return identity;
    }

    /** 从 token issuedFor 解析已注册的资源服务器。 */
    private ResourceServer getResourceServer(KeycloakIdentity identity) {
        String clientId = identity.getAccessToken().getIssuedFor();
        RealmModel realm = authorization.getKeycloakSession().getContext().getRealm();
        ClientModel clientModel = realm.getClientByClientId(clientId);

        if (clientModel == null) {
            clientModel = realm.getClientById(clientId);

            if (clientModel == null) {
                throw new ErrorResponseException("invalid_clientId", "Client application with id [" + clientId + "] does not exist in realm [" + realm.getName() + "]", Status.BAD_REQUEST);
            }
        }

        ResourceServer resourceServer = this.authorization.getStoreFactory().getResourceServerStore().findByClient(clientModel);

        if (resourceServer == null) {
            throw new ErrorResponseException("invalid_clientId", "Client application [" + clientModel.getClientId() + "] is not registered as a resource server.", Status.FORBIDDEN);
        }

        return resourceServer;
    }
}
