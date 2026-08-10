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
package org.keycloak.authorization.admin;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.fgap.AdminPermissionsSchema;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.models.utils.RepresentationToModel;
import org.keycloak.representations.idm.authorization.ResourceServerRepresentation;
import org.keycloak.services.resources.KeycloakOpenAPI;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;

import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import static org.keycloak.models.utils.ModelToRepresentation.toRepresentation;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
@Extension(name = KeycloakOpenAPI.Profiles.ADMIN, value = "")
/**
 * 客户端资源服务器管理端点：生命周期 CRUD 及授权配置导入导出。
 */
public class ResourceServerService {

    private final AuthorizationProvider authorization;
    private final AdminPermissionEvaluator auth;
    private final AdminEventBuilder adminEvent;
    private final KeycloakSession session;
    private ResourceServer resourceServer;
    private final ClientModel client;

    /** 构造资源服务器管理服务。 */
    public ResourceServerService(AuthorizationProvider authorization, ResourceServer resourceServer, ClientModel client, AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        this.authorization = authorization;
        this.session = authorization.getKeycloakSession();
        this.client = client;
        this.resourceServer = resourceServer;
        this.auth = auth;
        this.adminEvent = adminEvent;
    }

    /** 为客户端创建资源服务器（需服务账户用户）。 */
    public ResourceServer create(boolean newClient) {
        AdminPermissionsSchema.SCHEMA.throwExceptionIfAdminPermissionClient(session, client.getId());

        this.auth.realm().requireManageAuthorization(resourceServer);

        UserModel serviceAccount = this.session.users().getServiceAccount(client);

        if (serviceAccount == null) {
            throw new RuntimeException("Client does not have a service account.");
        }

        if (this.resourceServer == null) {
            this.resourceServer = RepresentationToModel.createResourceServer(client, session, true);
            audit(ModelToRepresentation.toRepresentation(resourceServer, client), OperationType.CREATE, session.getContext().getUri(), newClient);
        }

        return resourceServer;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponse(responseCode = "204", description = "No Content")
    /** 更新资源服务器策略执行模式、决策策略等设置。 */
    public Response update(ResourceServerRepresentation server) {
        AdminPermissionsSchema.SCHEMA.throwExceptionIfAdminPermissionClient(session, client.getId());

        this.auth.realm().requireManageAuthorization(resourceServer);
        this.resourceServer.setAllowRemoteResourceManagement(server.isAllowRemoteResourceManagement());
        this.resourceServer.setPolicyEnforcementMode(server.getPolicyEnforcementMode());
        this.resourceServer.setDecisionStrategy(server.getDecisionStrategy());
        audit(ModelToRepresentation.toRepresentation(resourceServer, client), OperationType.UPDATE, session.getContext().getUri(), false);
        return Response.noContent().build();
    }

    /** 删除资源服务器及其全部授权数据。 */
    public void delete() {
        AdminPermissionsSchema.SCHEMA.throwExceptionIfAdminPermissionClient(session, client.getId());

        this.auth.realm().requireManageAuthorization(resourceServer);
        // 删除前生成表示以访问懒加载字段
        ResourceServerRepresentation rep = ModelToRepresentation.toRepresentation(resourceServer, client);
        authorization.getStoreFactory().getResourceServerStore().delete(client);
        audit(rep, OperationType.DELETE, session.getContext().getUri(), false);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    /** @return 当前资源服务器表示 */
    public ResourceServerRepresentation findById() {
        this.auth.realm().requireViewAuthorization(resourceServer);
        return toRepresentation(this.resourceServer, this.client);
    }

    @Path("/settings")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    /** 导出完整授权设置（含资源、策略等）。 */
    public ResourceServerRepresentation exportSettings() {
        AdminPermissionsSchema.SCHEMA.throwExceptionIfAdminPermissionClient(session, client.getId());
        this.auth.realm().requireManageAuthorization(resourceServer);
        return ModelToRepresentation.toResourceServerRepresentation(session, client);
    }

    @Path("/import")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @APIResponse(responseCode = "204", description = "No Content")
    /** 从表示导入并覆盖授权配置。 */
    public Response importSettings(ResourceServerRepresentation rep) {
        AdminPermissionsSchema.SCHEMA.throwExceptionIfAdminPermissionClient(session, client.getId());
        this.auth.realm().requireManageAuthorization(resourceServer);

        rep.setClientId(client.getId());

        resourceServer = RepresentationToModel.toModel(rep, authorization, client);

        audit(ModelToRepresentation.toRepresentation(resourceServer, client), OperationType.UPDATE, session.getContext().getUri(), false);

        return Response.noContent().build();
    }

    @Path("/resource")
    /** @return 受保护资源集合管理子 API */
    public ResourceSetService getResourceSetResource() {
        return new ResourceSetService(this.session, this.resourceServer, this.authorization, this.auth, adminEvent);
    }

    @Path("/scope")
    /** @return 作用域管理子 API */
    public ScopeService getScopeResource() {
        return new ScopeService(this.session, this.resourceServer, this.authorization, this.auth, adminEvent);
    }

    @Path("/policy")
    /** @return 策略管理子 API */
    public PolicyService getPolicyResource() {
        return new PolicyService(this.resourceServer, this.authorization, this.auth, adminEvent);
    }

    @Path("/permission")
    /** @return 权限（Permission）管理子 API */
    public PermissionService getPermissionTypeResource() {
        this.auth.realm().requireViewAuthorization(resourceServer);
        return new PermissionService(this.resourceServer, this.authorization, this.auth, adminEvent);
    }

    /** 记录资源服务器管理审计事件。 */
    private void audit(ResourceServerRepresentation rep, OperationType operation, UriInfo uriInfo, boolean newClient) {
        if (newClient) {
            adminEvent.resource(ResourceType.AUTHORIZATION_RESOURCE_SERVER).operation(operation).resourcePath(uriInfo, client.getId())
                    .representation(rep).success();
        } else {
            adminEvent.resource(ResourceType.AUTHORIZATION_RESOURCE_SERVER).operation(operation).resourcePath(uriInfo)
                    .representation(rep).success();
        }
    }
}
