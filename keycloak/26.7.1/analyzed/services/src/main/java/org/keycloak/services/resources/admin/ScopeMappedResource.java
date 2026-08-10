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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.ScopeContainerModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.ClientMappingsRepresentation;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.services.resources.KeycloakOpenAPI;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;
import org.keycloak.services.util.ScopeMappedUtil;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.NoCache;

/**
 * 客户端范围映射管理基类。
 * <p>管理客户端或客户端范围与领域/客户端角色的 scope 关联。</p>
 *
 * @resource Scope Mappings
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@Extension(name = KeycloakOpenAPI.Profiles.ADMIN, value = "")
public class ScopeMappedResource {
    /** 当前领域 */
    protected RealmModel realm;
    /** 细粒度权限评估器 */
    protected AdminPermissionEvaluator auth;
    /** 管理权限检查回调 */
    protected AdminPermissionEvaluator.RequirePermissionCheck managePermission;
    /** 查看权限检查回调 */
    protected AdminPermissionEvaluator.RequirePermissionCheck viewPermission;

    /** 范围容器（客户端或客户端范围） */
    protected ScopeContainerModel scopeContainer;
    /** Keycloak 会话 */
    protected KeycloakSession session;
    /** 管理事件构建器 */
    protected AdminEventBuilder adminEvent;

    /** 构造范围映射资源。 */
    public ScopeMappedResource(RealmModel realm, AdminPermissionEvaluator auth, ScopeContainerModel scopeContainer,
                               KeycloakSession session, AdminEventBuilder adminEvent,
                               AdminPermissionEvaluator.RequirePermissionCheck managePermission,
                               AdminPermissionEvaluator.RequirePermissionCheck viewPermission) {
        this.realm = realm;
        this.auth = auth;
        this.scopeContainer = scopeContainer;
        this.session = session;
        this.adminEvent = adminEvent.resource(ResourceType.REALM_SCOPE_MAPPING);
        this.managePermission = managePermission;
        this.viewPermission = viewPermission;
    }

    /**
     * 获取客户端的全部 scope 映射（已弃用）。
     *
     * @return 映射表示
     * @deprecated 管理控制台与 Admin Client 均未使用，未来版本可能移除
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Deprecated
    @Tag(name= KeycloakOpenAPI.Admin.Tags.SCOPE_MAPPINGS)
    @Operation(summary = "Get all scope mappings for the client", deprecated = true)
    public MappingsRepresentation getScopeMappings() {
        viewPermission.require();

        if (scopeContainer == null) {
            throw new NotFoundException("Could not find client");
        }

        MappingsRepresentation all = new MappingsRepresentation();
        List<RoleRepresentation> realmRep = scopeContainer.getRealmScopeMappingsStream()
                .map(ModelToRepresentation::toBriefRepresentation)
                .collect(Collectors.toList());
        if (!realmRep.isEmpty()) {
            all.setRealmMappings(realmRep);
        }

        Stream<ClientModel> clients = realm.getClientsStream();
        Map<String, ClientMappingsRepresentation> clientMappings = clients
                .map(c -> ScopeMappedUtil.toClientMappingsRepresentation(c, scopeContainer))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(ClientMappingsRepresentation::getClient, Function.identity()));

        if (!clientMappings.isEmpty()) {
            all.setClientMappings(clientMappings);
        }
        return all;
    }

    /**
     * 获取与客户端 scope 关联的领域级角色。
     *
     * @return 角色表示流
     */
    @Path("realm")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.SCOPE_MAPPINGS)
    @Operation(summary = "Get realm-level roles associated with the client's scope")
    public Stream<RoleRepresentation> getRealmScopeMappings() {
        viewPermission.require();

        if (scopeContainer == null) {
            throw new NotFoundException("Could not find client");
        }

        return scopeContainer.getRealmScopeMappingsStream()
                .map(ModelToRepresentation::toBriefRepresentation);
    }

    /**
     * 获取可附加到客户端 scope 的领域级角色。
     *
     * @return 可用角色流
     */
    @Path("realm/available")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.SCOPE_MAPPINGS)
    @Operation(summary = "Get realm-level roles that are available to attach to this client's scope")
    public Stream<RoleRepresentation> getAvailableRealmScopeMappings() {
        viewPermission.require();

        if (scopeContainer == null) {
            throw new NotFoundException("Could not find client");
        }

        return realm.getRolesStream()
                .filter(((Predicate<RoleModel>) scopeContainer::hasDirectScope).negate())
                .filter(auth.roles()::canMapClientScope)
                .map(ModelToRepresentation::toBriefRepresentation);
    }

    /**
     * 获取与客户端 scope 关联的有效领域级角色（递归展开复合角色）。
     * <p>展示客户端 scope 关联的领域角色的完整视图。</p>
     *
     * @param briefRepresentation 为 false 时返回含属性的完整表示
     * @return 角色表示流
     */
    @Path("realm/composite")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.SCOPE_MAPPINGS)
    @Operation(summary = "Get effective realm-level roles associated with the client’s scope What this does is recurse any composite roles associated with the client’s scope and adds the roles to this lists.",
        description = "The method is really to show a comprehensive total view of realm-level roles associated with the client.")
    public Stream<RoleRepresentation> getCompositeRealmScopeMappings(@Parameter(description = "if false, return roles with their attributes") @QueryParam("briefRepresentation") @DefaultValue("true") boolean briefRepresentation) {
        viewPermission.require();

        if (scopeContainer == null) {
            throw new NotFoundException("Could not find client");
        }

        Function<RoleModel, RoleRepresentation> toBriefRepresentation = briefRepresentation ?
                ModelToRepresentation::toBriefRepresentation : ModelToRepresentation::toRepresentation;
        return realm.getRolesStream()
                .filter(scopeContainer::hasScope)
                .map(toBriefRepresentation);
    }

    /**
     * 向客户端 scope 添加一组领域级角色。
     *
     * @param roles 角色列表
     */
    @Path("realm")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.SCOPE_MAPPINGS)
    @Operation(summary = "Add a set of realm-level roles to the client's scope")
    @APIResponse(responseCode = "204", description = "No Content")
    public void addRealmScopeMappings(List<RoleRepresentation> roles) {
        managePermission.require();

        if (scopeContainer == null) {
            throw new NotFoundException("Could not find client");
        }

        for (RoleRepresentation role : roles) {
            RoleModel roleModel = realm.getRoleById(role.getId());
            if (roleModel == null) {
                throw new NotFoundException("Role not found");
            }
            auth.roles().requireMapClientScope(roleModel);
            scopeContainer.addScopeMapping(roleModel);
        }

        adminEvent.operation(OperationType.CREATE).resourcePath(session.getContext().getUri()).representation(roles).success();
    }

    /**
     * 从客户端 scope 移除一组领域级角色。
     *
     * @param roles 角色列表；为 null 时移除全部
     */
    @Path("realm")
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.SCOPE_MAPPINGS)
    @Operation(summary = "Remove a set of realm-level roles from the client's scope")
    public void deleteRealmScopeMappings(List<RoleRepresentation> roles) {
        managePermission.require();

        if (scopeContainer == null) {
            throw new NotFoundException("Could not find client");
        }

        if (roles == null) {
            roles = scopeContainer.getRealmScopeMappingsStream()
                    .filter(auth.roles()::canMapClientScope)
                    .peek(scopeContainer::deleteScopeMapping)
                    .map(ModelToRepresentation::toBriefRepresentation)
                    .collect(Collectors.toList());
       } else {
            for (RoleRepresentation role : roles) {
                RoleModel roleModel = realm.getRoleById(role.getId());
                if (roleModel == null) {
                    throw new NotFoundException("Role not found");
                }
                auth.roles().requireMapClientScope(roleModel);
                scopeContainer.deleteScopeMapping(roleModel);
            }
        }

        adminEvent.operation(OperationType.DELETE).resourcePath(session.getContext().getUri()).representation(roles).success();

    }

    /** 获取指定客户端的 scope 映射子资源。 */
    @Path("clients/{client}")
    public ScopeMappedClientResource getClientByIdScopeMappings(@PathParam("client") String client) {
        ClientModel clientModel = realm.getClientById(client);
        if (clientModel == null) {
            throw new NotFoundException("Could not find client");
        }
        return new ScopeMappedClientResource(realm, auth, this.scopeContainer, session, clientModel, adminEvent, managePermission, viewPermission);
    }
}
