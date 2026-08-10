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
package org.keycloak.services.resources.admin;

import java.util.Optional;
import java.util.stream.Stream;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.models.utils.RepresentationToModel;
import org.keycloak.protocol.LoginProtocol;
import org.keycloak.protocol.LoginProtocolFactory;
import org.keycloak.representations.idm.ClientScopeRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.resources.KeycloakOpenAPI;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.NoCache;

/**
 * 领域客户端范围（Client Scopes）集合 REST 资源。
 * <p>列出、创建客户端范围，并路由到单个范围子资源。</p>
 *
 * @resource Client Scopes
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@Extension(name = KeycloakOpenAPI.Profiles.ADMIN, value = "")
public class ClientScopesResource {
    /** 日志记录器 */
    protected static final Logger logger = Logger.getLogger(ClientScopesResource.class);
    /** 当前领域 */
    protected final RealmModel realm;
    /** 细粒度权限评估器 */
    private final AdminPermissionEvaluator auth;
    /** 管理事件构建器 */
    private final AdminEventBuilder adminEvent;

    /** Keycloak 会话 */
    protected final KeycloakSession session;

    /** 构造客户端范围集合资源。
     * @param session Keycloak 会话
     * @param auth 权限评估器
     * @param adminEvent 管理事件构建器
     */
    public ClientScopesResource(KeycloakSession session, AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        this.session = session;
        this.realm = session.getContext().getRealm();
        this.auth = auth;
        this.adminEvent = adminEvent.resource(ResourceType.CLIENT_SCOPE);
    }

    /** 获取领域下所有客户端范围列表。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENT_SCOPES)
    @Operation(summary = "Get client scopes belonging to the realm Returns a list of client scopes belonging to the realm")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "", content = @Content(schema = @Schema(implementation = ClientScopeRepresentation.class, type = SchemaType.ARRAY))),
        @APIResponse(responseCode = "403", description = "Forbidden")
    })
    public Stream<ClientScopeRepresentation> getClientScopes() {
        auth.clients().requireListClientScopes();

        return auth.clients().canViewClientScopes() ?
                realm.getClientScopesStream().map(ModelToRepresentation::toRepresentation) :
                Stream.empty();
    }

    /**
     * 创建新客户端范围（名称须唯一）。
     * @param rep 客户端范围表示
     * @return 201 Created
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENT_SCOPES)
    @Operation(summary = "Create a new client scope Client Scope’s name must be unique!")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "Created"),
        @APIResponse(responseCode = "403", description = "Forbidden"),
        @APIResponse(responseCode = "409", description = "Conflict")
    })
    public Response createClientScope(ClientScopeRepresentation rep) {
        auth.clients().requireManageClientScopes();
        ClientScopeResource.validateClientScopeName(rep.getName());
        ClientScopeResource.validateClientScopeProtocol(session, rep.getProtocol());
        ClientScopeResource.validateClientScope(session, rep);
        ClientScopeResource.validateParameterizedClientScope(session, rep);
        try {
            LoginProtocolFactory loginProtocolFactory = //
                    (LoginProtocolFactory) session.getKeycloakSessionFactory().getProviderFactory(LoginProtocol.class,
                                                                                                  rep.getProtocol());
            Optional.ofNullable(loginProtocolFactory).ifPresent(lp -> lp.addClientScopeDefaults(rep));
            ClientScopeModel clientScope = RepresentationToModel.createClientScope(realm, rep);

            adminEvent.operation(OperationType.CREATE).resourcePath(session.getContext().getUri(), clientScope.getId()).representation(rep).success();

            return Response.created(session.getContext().getUri().getAbsolutePathBuilder().path(clientScope.getId()).build()).build();
        } catch (ModelDuplicateException e) {
            throw ErrorResponse.exists("Client Scope " + rep.getName() + " already exists");
        }
    }

    /**
     * 获取单个客户端范围子资源。
     * @param id 客户端范围 ID（非名称）
     * @return {@link ClientScopeResource}
     */
    @Path("{client-scope-id}")
    @NoCache
    public ClientScopeResource getClientScope(final @PathParam("client-scope-id") String id) {
        auth.clients().requireListClientScopes();
        ClientScopeModel clientModel = realm.getClientScopeById(id);
        if (clientModel == null) {
            throw new NotFoundException("Could not find client scope");
        }
        return new ClientScopeResource(realm, auth, clientModel, session, adminEvent);
    }

}
