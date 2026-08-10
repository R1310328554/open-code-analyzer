/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RoleContainerModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.representations.idm.RoleRepresentation;
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
import org.jboss.resteasy.reactive.NoCache;

/**
 * 客户端范围评估——作用域映射子资源。
 * <p>评估指定客户端在给定 scope 参数下对角色容器已授予/未授予的作用域映射。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@Extension(name = KeycloakOpenAPI.Profiles.ADMIN, value = "")
public class ClientScopeEvaluateScopeMappingsResource {

    /** Keycloak 会话 */
    private final KeycloakSession session;
    /** 角色容器（领域或客户端） */
    private final RoleContainerModel roleContainer;
    /** 细粒度权限评估器 */
    private final AdminPermissionEvaluator auth;
    /** 被评估的客户端 */
    private final ClientModel client;
    /** OIDC scope 参数字符串 */
    private final String scopeParam;

    /** 构造作用域映射评估资源。
     * @param session Keycloak 会话
     * @param roleContainer 角色容器
     * @param auth 权限评估器
     * @param client 被评估客户端
     * @param scopeParam scope 参数
     */
    public ClientScopeEvaluateScopeMappingsResource(KeycloakSession session, RoleContainerModel roleContainer, AdminPermissionEvaluator auth, ClientModel client,
                                                    String scopeParam) {
        this.session = session;
        this.roleContainer = roleContainer;
        this.auth = auth;
        this.client = client;
        this.scopeParam = scopeParam;
    }

    /**
     * 获取客户端在 access token 中实际可包含的角色（已授予作用域映射）。
     * <p>含客户端直接映射及关联客户端范围上的映射。</p>
     * @return 已授予角色表示流
     */
    @Path("/granted")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENTS)
    @Operation(summary = "Get effective scope mapping of all roles of particular role container, which this client is defacto allowed to have in the accessToken issued for him.",
            description = "This contains scope mappings, which this client has directly, as well as scope mappings, which are granted to all client scopes, which are linked with this client.")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "", content = @Content(schema = @Schema(implementation = RoleRepresentation.class, type = SchemaType.ARRAY))),
        @APIResponse(responseCode = "403", description = "Forbidden")
    })
    public Stream<RoleRepresentation> getGrantedScopeMappings() {
        return getGrantedRoles(session).map(ModelToRepresentation::toBriefRepresentation);
    }

    /**
     * 获取客户端在 access token 中不可包含的角色（未授予作用域映射）。
     * <p>即角色容器中除 {@link #getGrantedScopeMappings()} 外的其余角色。</p>
     * @return 未授予角色表示流
     */
    @Path("/not-granted")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENTS)
    @Operation(summary = "Get roles, which this client doesn't have scope for and can't have them in the accessToken issued for him.", description = "Defacto all the other roles of particular role container, which are not in {@link #getGrantedScopeMappings()}")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "", content = @Content(schema = @Schema(implementation = RoleRepresentation.class, type = SchemaType.ARRAY))),
        @APIResponse(responseCode = "403", description = "Forbidden")
    })
    public Stream<RoleRepresentation> getNotGrantedScopeMappings() {
        Set<RoleModel> grantedRoles = getGrantedRoles(session).collect(Collectors.toSet());

        return roleContainer.getRolesStream()
                .filter(((Predicate<RoleModel>) grantedRoles::contains).negate())
                .map(ModelToRepresentation::toBriefRepresentation);
    }

    /** 计算已授予角色流（考虑 full scope allowed 与客户端范围） */
    private Stream<RoleModel> getGrantedRoles(KeycloakSession session) {
        if (client.isFullScopeAllowed()) {
            return roleContainer.getRolesStream();
        }

        Set<ClientScopeModel> clientScopes = TokenManager.getRequestedClientScopes(session, scopeParam, client, null)
                .collect(Collectors.toSet());

        Predicate<RoleModel> hasClientScope = role ->
                clientScopes.stream().anyMatch(scopeContainer -> scopeContainer.hasScope(role));

        return roleContainer.getRolesStream()
                .filter(auth.roles()::canView)
                .filter(hasClientScope);
    }

}
