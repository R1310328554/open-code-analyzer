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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
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
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.common.ClientConnection;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelException;
import org.keycloak.models.ModelIllegalStateException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleContainerModel;
import org.keycloak.models.RoleMapperModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.ClientMappingsRepresentation;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.ErrorResponseException;
import org.keycloak.services.resources.KeycloakOpenAPI;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;
import org.keycloak.storage.ReadOnlyException;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.NoCache;

/**
 * 角色映射 REST 资源基类。
 * <p>管理用户或组等领域/客户端角色分配，供 {@link UserResource} 等调用。</p>
 *
 * @resource Role Mapper
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:mpaulosnunes@gmail.com">Miguel P. Nunes</a>
 * @version $Revision: 1 $
 */
@Extension(name = KeycloakOpenAPI.Profiles.ADMIN, value = "")
public class RoleMapperResource {

    /** 日志记录器 */
    protected static final Logger logger = Logger.getLogger(RoleMapperResource.class);

    /** 当前领域 */
    protected final RealmModel realm;

    /** 角色映射目标（用户/组等） */
    private final RoleMapperModel roleMapper;

    /** 管理事件构建器 */
    private final AdminEventBuilder adminEvent;

    /** 管理权限检查回调 */
    protected final AdminPermissionEvaluator.RequirePermissionCheck managePermission;
    /** 查看权限检查回调 */
    protected final AdminPermissionEvaluator.RequirePermissionCheck viewPermission;
    /** 细粒度权限评估器 */
    private final AdminPermissionEvaluator auth;

    /** 客户端连接信息 */
    protected final ClientConnection clientConnection;

    /** Keycloak 会话 */
    protected final KeycloakSession session;

    /** HTTP 请求头 */
    protected final HttpHeaders headers;

    /** 构造角色映射资源。 */
    public RoleMapperResource(KeycloakSession session,
                              AdminPermissionEvaluator auth,
                              RoleMapperModel roleMapper,
                              AdminEventBuilder adminEvent,
                              AdminPermissionEvaluator.RequirePermissionCheck manageCheck,
                              AdminPermissionEvaluator.RequirePermissionCheck viewCheck) {
        this.session = session;
        this.auth = auth;
        this.realm = session.getContext().getRealm();
        this.clientConnection = session.getContext().getConnection();
        this.adminEvent = adminEvent.resource(ResourceType.REALM_ROLE_MAPPING);
        this.roleMapper = roleMapper;
        this.managePermission = manageCheck;
        this.viewPermission = viewCheck;
        this.headers = session.getContext().getRequestHeaders();

    }

    /**
     * 获取全部角色映射（领域级与按客户端分组）。
     *
     * @return 映射表示
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.ROLE_MAPPER)
    @Operation(summary = "Get role mappings")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "", content = @Content(schema = @Schema(implementation = MappingsRepresentation.class))),
        @APIResponse(responseCode = "403", description = "Forbidden")
    })
    public MappingsRepresentation getRoleMappings() {
        viewPermission.require();

        List<RoleRepresentation> realmRolesRepresentation = new ArrayList<>();
        Map<String, ClientMappingsRepresentation> appMappings = new HashMap<>();

        final AtomicReference<ClientMappingsRepresentation> mappings = new AtomicReference<>();

        roleMapper.getRoleMappingsStream().forEach(roleMapping -> {
            RoleContainerModel container = roleMapping.getContainer();
            if (container instanceof RealmModel) {
                realmRolesRepresentation.add(ModelToRepresentation.toBriefRepresentation(roleMapping));
            } else if (container instanceof ClientModel) {
                ClientModel clientModel = (ClientModel) container;
                mappings.set(appMappings.get(clientModel.getClientId()));
                if (mappings.get() == null) {
                    mappings.set(new ClientMappingsRepresentation());
                    mappings.get().setId(clientModel.getId());
                    mappings.get().setClient(clientModel.getClientId());
                    mappings.get().setMappings(new ArrayList<>());
                    appMappings.put(clientModel.getClientId(), mappings.get());
                }
                mappings.get().getMappings().add(ModelToRepresentation.toBriefRepresentation(roleMapping));
            }
        });

        MappingsRepresentation all = new MappingsRepresentation();
        if (!realmRolesRepresentation.isEmpty()) all.setRealmMappings(realmRolesRepresentation);
        if (!appMappings.isEmpty()) all.setClientMappings(appMappings);

        return all;
    }

    /**
     * 获取领域级角色映射。
     *
     * @return 角色表示流
     */
    @Path("realm")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.ROLE_MAPPER)
    @Operation(summary = "Get realm-level role mappings")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "", content = @Content(schema = @Schema(implementation = RoleRepresentation.class, type = SchemaType.ARRAY))),
        @APIResponse(responseCode = "403", description = "Forbidden")
    })
    public Stream<RoleRepresentation> getRealmRoleMappings() {
        viewPermission.require();

        return roleMapper.getRealmRoleMappingsStream().map(ModelToRepresentation::toBriefRepresentation);
    }

    /**
     * 获取有效领域级角色映射（递归展开复合角色）。
     *
     * @param briefRepresentation 为 false 时返回含属性的完整表示
     *
     * @return 角色表示流
     */
    @Path("realm/composite")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.ROLE_MAPPER)
    @Operation(summary = "Get effective realm-level role mappings This will recurse all composite roles to get the result.")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "", content = @Content(schema = @Schema(implementation = RoleRepresentation.class, type = SchemaType.ARRAY))),
        @APIResponse(responseCode = "403", description = "Forbidden")
    })
    public Stream<RoleRepresentation> getCompositeRealmRoleMappings(@Parameter(description = "if false, return roles with their attributes") @QueryParam("briefRepresentation") @DefaultValue("true") boolean briefRepresentation) {
        viewPermission.require();

        Function<RoleModel, RoleRepresentation> toBriefRepresentation = briefRepresentation ?
                ModelToRepresentation::toBriefRepresentation : ModelToRepresentation::toRepresentation;
        return realm.getRolesStream()
                .filter(roleMapper::hasRole)
                .map(toBriefRepresentation);
    }

    /**
     * 获取可映射的领域级角色（尚未直接分配）。
     *
     * @return 可用角色流
     */
    @Path("realm/available")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.ROLE_MAPPER)
    @Operation(summary = "Get realm-level roles that can be mapped")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "", content = @Content(schema = @Schema(implementation = RoleRepresentation.class, type = SchemaType.ARRAY))),
        @APIResponse(responseCode = "403", description = "Forbidden")
    })
    public Stream<RoleRepresentation> getAvailableRealmRoleMappings() {
        viewPermission.require();

        return realm.getRolesStream()
                .filter(this::canMapRole)
                .filter(((Predicate<RoleModel>) roleMapper::hasDirectRole).negate())
                .map(ModelToRepresentation::toBriefRepresentation);
    }

    /**
     * 为用户添加领域级角色映射。
     *
     * @param roles 待添加的角色列表
     */
    @Path("realm")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.ROLE_MAPPER)
    @Operation(summary = "Add realm-level role mappings to the user")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "No Content"),
        @APIResponse(responseCode = "400", description = "Bad Request"),
        @APIResponse(responseCode = "403", description = "Forbidden"),
        @APIResponse(responseCode = "404", description = "Not Found"),
        @APIResponse(responseCode = "500", description = "Internal Server Error")
    })
    public void addRealmRoleMappings(@Parameter(description = "Roles to add") List<RoleRepresentation> roles) {
        managePermission.require();

        logger.debugv("** addRealmRoleMappings: {0}", roles);

        try {
            for (RoleRepresentation role : roles) {
                RoleModel roleModel = realm.getRole(role.getName());
                if (roleModel == null || !roleModel.getId().equals(role.getId())) {
                    throw new NotFoundException("Role not found");
                }
                auth.roles().requireMapRole(roleModel);
                roleMapper.grantRole(roleModel);
            }
        } catch (ModelIllegalStateException e) {
            logger.error(e.getMessage(), e);
            throw ErrorResponse.error(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        } catch (ModelException | ReadOnlyException me) {
            logger.warn(me.getMessage(), me);
            throw new ErrorResponseException("invalid_request", "Could not add user role mappings!", Response.Status.BAD_REQUEST);
        }

        if (!roles.isEmpty()) {
            adminEvent.operation(OperationType.CREATE).resourcePath(session.getContext().getUri()).representation(roles).success();
        }
    }

    /**
     * 删除领域级角色映射。
     *
     * @param roles 待删除角色；为 null 时删除全部
     */
    @Path("realm")
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.ROLE_MAPPER)
    @Operation(summary = "Delete realm-level role mappings")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "No Content"),
        @APIResponse(responseCode = "400", description = "Bad Request"),
        @APIResponse(responseCode = "403", description = "Forbidden"),
        @APIResponse(responseCode = "404", description = "Not Found"),
        @APIResponse(responseCode = "500", description = "Internal Server Error")
    })
    public void deleteRealmRoleMappings(List<RoleRepresentation> roles) {
        managePermission.require();

        logger.debug("deleteRealmRoleMappings");
        if (roles == null) {
            roles = roleMapper.getRealmRoleMappingsStream()
                    .peek(roleModel -> {
                        auth.roles().requireMapRole(roleModel);
                        roleMapper.deleteRoleMapping(roleModel);
                    })
                    .map(ModelToRepresentation::toBriefRepresentation)
                    .collect(Collectors.toList());

        } else {
            for (RoleRepresentation role : roles) {
                RoleModel roleModel = realm.getRole(role.getName());
                if (roleModel == null || !roleModel.getId().equals(role.getId())) {
                    throw new NotFoundException("Role not found");
                }
                auth.roles().requireMapRole(roleModel);
                try {
                    roleMapper.deleteRoleMapping(roleModel);
                } catch (ModelIllegalStateException e) {
                    logger.error(e.getMessage(), e);
                    throw ErrorResponse.error(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
                } catch (ModelException | ReadOnlyException me) {
                    logger.warn(me.getMessage(), me);
                    throw new ErrorResponseException("invalid_request", "Could not remove user role mappings!", Response.Status.BAD_REQUEST);
                }
            }

        }

        adminEvent.operation(OperationType.DELETE).resourcePath(session.getContext().getUri()).representation(roles).success();

    }

    /** 检查当前管理员是否可映射指定角色。 */
    private boolean canMapRole(RoleModel roleModel) {
        return auth.roles().canMapRole(roleModel);
    }

    /** 获取指定客户端的角色映射子资源。 */
    @Path("clients/{client-id}")
    public ClientRoleMappingsResource getUserClientRoleMappingsResource(@PathParam("client-id") @Parameter(description = "client id (not clientId!)") String client) {
        ClientModel clientModel = realm.getClientById(client);
        if (clientModel == null) {
            throw new NotFoundException("Client not found");
        }
        ClientRoleMappingsResource resource = new ClientRoleMappingsResource(session.getContext().getUri(), session, realm, auth, roleMapper,
                clientModel, adminEvent,
                managePermission, viewPermission);
        return resource;

    }
}
