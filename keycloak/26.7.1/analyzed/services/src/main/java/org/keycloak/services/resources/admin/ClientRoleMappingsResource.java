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
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelException;
import org.keycloak.models.ModelIllegalStateException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleMapperModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.models.utils.RoleUtils;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.ErrorResponseException;
import org.keycloak.services.resources.KeycloakOpenAPI;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;
import org.keycloak.storage.ReadOnlyException;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.NoCache;

/**
 * 客户端级角色映射 REST 资源。
 * <p>管理用户或组在特定客户端上的直接/复合/可用角色映射。</p>
 *
 * @resource Client Role Mappings
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@Extension(name = KeycloakOpenAPI.Profiles.ADMIN, value = "")
public class ClientRoleMappingsResource {
    /** 日志记录器 */
    protected static final Logger logger = Logger.getLogger(ClientRoleMappingsResource.class);

    /** Keycloak 会话 */
    protected KeycloakSession session;
    /** 当前领域 */
    protected RealmModel realm;
    /** 细粒度权限评估器 */
    protected AdminPermissionEvaluator auth;
    /** 角色映射主体（用户或组） */
    protected RoleMapperModel user;
    /** 目标客户端 */
    protected ClientModel client;
    /** 管理事件构建器 */
    protected AdminEventBuilder adminEvent;
    /** 请求 URI 信息 */
    private UriInfo uriInfo;
    /** 管理权限检查回调 */
    protected AdminPermissionEvaluator.RequirePermissionCheck managePermission;
    /** 查看权限检查回调 */
    protected AdminPermissionEvaluator.RequirePermissionCheck viewPermission;


    /** 构造客户端角色映射资源。
     * @param uriInfo URI 信息
     * @param session Keycloak 会话
     * @param realm 当前领域
     * @param auth 权限评估器
     * @param user 角色映射主体
     * @param client 目标客户端
     * @param adminEvent 管理事件构建器
     * @param manageCheck 管理权限检查
     * @param viewCheck 查看权限检查
     */
    public ClientRoleMappingsResource(UriInfo uriInfo, KeycloakSession session, RealmModel realm, AdminPermissionEvaluator auth,
                                      RoleMapperModel user, ClientModel client, AdminEventBuilder adminEvent,
                                      AdminPermissionEvaluator.RequirePermissionCheck manageCheck, AdminPermissionEvaluator.RequirePermissionCheck viewCheck ) {
        this.uriInfo = uriInfo;
        this.session = session;
        this.realm = realm;
        this.auth = auth;
        this.user = user;
        this.client = client;
        this.managePermission = manageCheck;
        this.viewPermission = viewCheck;
        this.adminEvent = adminEvent.resource(ResourceType.CLIENT_ROLE_MAPPING);
    }

    /**
     * 获取用户/组在该客户端上的直接角色映射。
     * @return 角色简要表示流
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENT_ROLE_MAPPINGS)
    @Operation( summary = "Get client-level role mappings for the user or group, and the app")
    public Stream<RoleRepresentation> getClientRoleMappings() {
        viewPermission.require();

        return user.getClientRoleMappingsStream(client).map(ModelToRepresentation::toBriefRepresentation);
    }

    /**
     * 获取有效客户端级角色映射（递归展开复合角色）。
     * @param briefRepresentation 为 false 时返回含属性的完整表示
     * @return 角色表示流
     */
    @Path("composite")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENT_ROLE_MAPPINGS)
    @Operation( summary = "Get effective client-level role mappings This recurses any composite roles")
    public Stream<RoleRepresentation> getCompositeClientRoleMappings(@Parameter(description = "if false, return roles with their attributes") @QueryParam("briefRepresentation") @DefaultValue("true") boolean briefRepresentation) {
        viewPermission.require();

        Function<RoleModel, RoleRepresentation> toBriefRepresentation = briefRepresentation
                ? ModelToRepresentation::toBriefRepresentation : ModelToRepresentation::toRepresentation;

        // 预先计算完整有效角色集（直接 + 组继承），再按客户端过滤，避免 O(C*M*D) 的 hasRole 递归开销
        // roles for users, direct only for groups), then filter by client.
        // This avoids the O(C*M*D) cost of calling user.hasRole() per client
        // role, which recursively expands composites without memoization.
        return RoleUtils.getDeepRoleMappings(user).stream()
                .filter(r -> r.isClientRole() && r.getContainerId().equals(client.getId()))
                .map(toBriefRepresentation);
    }

    /**
     * 获取可映射但尚未分配的角色列表。
     * @return 可用角色表示流
     */
    @Path("available")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENT_ROLE_MAPPINGS)
    @Operation( summary = "Get available client-level roles that can be mapped to the user or group")
    public Stream<RoleRepresentation> getAvailableClientRoleMappings() {
        viewPermission.require();

        return client.getRolesStream()
                .filter(auth.roles()::canMapRole)
                .filter(((Predicate<RoleModel>) user::hasDirectRole).negate())
                .map(ModelToRepresentation::toBriefRepresentation);
    }

    /**
     * 为用户/组添加客户端级角色映射。
     * @param roles 待添加角色列表
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENT_ROLE_MAPPINGS)
    @Operation( summary = "Add client-level roles to the user or group role mapping")
    @APIResponse(responseCode = "204", description = "No Content")
    public void addClientRoleMapping(List<RoleRepresentation> roles) {
        managePermission.require();

        try {
            for (RoleRepresentation role : roles) {
                RoleModel roleModel = client.getRole(role.getName());
                if (roleModel == null || !roleModel.getId().equals(role.getId())) {
                    throw new NotFoundException("Role not found");
                }
                auth.roles().requireMapRole(roleModel);
                user.grantRole(roleModel);
            }
        } catch (ModelIllegalStateException e) {
            logger.error(e.getMessage(), e);
            throw ErrorResponse.error(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        } catch (ModelException | ReadOnlyException me) {
            logger.warn(me.getMessage(), me);
            throw new ErrorResponseException("invalid_request", "Could not add user role or group mappings!", Response.Status.BAD_REQUEST);
        }

        if (!roles.isEmpty()) {
            adminEvent.operation(OperationType.CREATE).resourcePath(uriInfo).representation(roles).success();
        }

    }

    /**
     * 删除用户/组的客户端级角色映射；roles 为 null 时删除全部。
     * @param roles 待删除角色列表
     */
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENT_ROLE_MAPPINGS)
    @Operation( summary = "Delete client-level roles from user or group role mapping")
    public void deleteClientRoleMapping(List<RoleRepresentation> roles) {
        managePermission.require();

        if (roles == null) {
            roles = user.getClientRoleMappingsStream(client)
                    .peek(roleModel -> {
                        auth.roles().requireMapRole(roleModel);
                        user.deleteRoleMapping(roleModel);
                    })
                    .map(ModelToRepresentation::toBriefRepresentation)
                    .collect(Collectors.toList());
        } else {
            for (RoleRepresentation role : roles) {
                RoleModel roleModel = client.getRole(role.getName());
                if (roleModel == null || !roleModel.getId().equals(role.getId())) {
                    throw new NotFoundException("Role not found");
                }

                auth.roles().requireMapRole(roleModel);
                try {
                    user.deleteRoleMapping(roleModel);
                } catch (ModelIllegalStateException e) {
                    logger.error(e.getMessage(), e);
                    throw ErrorResponse.error(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
                } catch (ModelException | ReadOnlyException me) {
                    logger.warn(me.getMessage(), me);
                    throw new ErrorResponseException("invalid_request", "Could not remove user or group role mappings!", Response.Status.BAD_REQUEST);
                }
            }
        }

        adminEvent.operation(OperationType.DELETE).resourcePath(uriInfo).representation(roles).success();
    }
}
