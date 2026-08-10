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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.keycloak.common.Profile;
import org.keycloak.common.util.Encode;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleContainerModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.ManagementPermissionReference;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.resources.KeycloakOpenAPI;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;
import org.keycloak.services.resources.admin.fgap.AdminPermissionManagement;
import org.keycloak.services.resources.admin.fgap.AdminPermissions;
import org.keycloak.utils.ProfileHelper;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.NoCache;

/**
 * 角色容器 REST 资源（领域或客户端角色）。
 * <p>按角色名称管理 CRUD、复合角色、成员查询及细粒度权限。</p>
 *
 * @resource Roles
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@Extension(name = KeycloakOpenAPI.Profiles.ADMIN, value = "")
public class RoleContainerResource extends RoleResource {
    /** 当前领域 */
    private final RealmModel realm;
    /** 细粒度权限评估器 */
    protected AdminPermissionEvaluator auth;

    /** 角色容器（领域或客户端） */
    protected RoleContainerModel roleContainer;
    /** 管理事件构建器 */
    private AdminEventBuilder adminEvent;
    /** 当前请求 URI 信息 */
    private UriInfo uriInfo;
    /** Keycloak 会话 */
    private KeycloakSession session;

    /** 构造带角色容器的资源。 */
    public RoleContainerResource(KeycloakSession session, UriInfo uriInfo, RealmModel realm,
                                 AdminPermissionEvaluator auth, RoleContainerModel roleContainer, AdminEventBuilder adminEvent) {
        super(realm);
        this.uriInfo = uriInfo;
        this.realm = realm;
        this.auth = auth;
        this.roleContainer = roleContainer;
        this.adminEvent = adminEvent;
        this.session = session;
    }

    /** 构造资源（角色容器稍后通过 {@link #setRoleContainer} 设置）。 */
    public RoleContainerResource(KeycloakSession session, UriInfo uriInfo, RealmModel realm,
                                 AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        this(session, uriInfo, realm, auth, null, adminEvent);
    }

    /** 设置当前操作的角色容器。 */
    public void setRoleContainer(RoleContainerModel roleContainer) {
        this.roleContainer = roleContainer;
    }

    /**
     * 获取领域或客户端的全部角色（支持搜索与分页）。
     *
     * @return 角色表示流
     */
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.ROLES)
    @Operation(summary = "Get all roles for the realm or client")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "", content = @Content(schema = @Schema(implementation = RoleRepresentation.class, type = SchemaType.ARRAY))),
        @APIResponse(responseCode = "403", description = "Forbidden")
    })
    public Stream<RoleRepresentation> getRoles(@QueryParam("search") @DefaultValue("") String search,
                                               @QueryParam("first") Integer firstResult,
                                               @QueryParam("max") Integer maxResults,
                                               @QueryParam("briefRepresentation") @DefaultValue("true") boolean briefRepresentation) {
        auth.roles().requireList(roleContainer);

        Stream<RoleModel> roleModels;

        if(search != null && search.trim().length() > 0) {
            roleModels = roleContainer.searchForRolesStream(search, firstResult, maxResults);
        } else if (!Objects.isNull(firstResult) && !Objects.isNull(maxResults)) {
            roleModels = roleContainer.getRolesStream(firstResult, maxResults);
        } else {
            roleModels = roleContainer.getRolesStream();
        }

        Function<RoleModel, RoleRepresentation> toRoleRepresentation = briefRepresentation ?
                ModelToRepresentation::toBriefRepresentation :
                ModelToRepresentation::toRepresentation;
        return roleModels.map(toRoleRepresentation);
    }

    /**
     * 在领域或客户端中创建新角色。
     *
     * @param rep 角色表示
     * @return 201 Created
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.ROLES)
    @Operation(summary = "Create a new role for the realm or client")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "Created"),
        @APIResponse(responseCode = "400", description = "Bad Request"),
        @APIResponse(responseCode = "403", description = "Forbidden"),
        @APIResponse(responseCode = "404", description = "Not Found"),
        @APIResponse(responseCode = "409", description = "Conflict"),
        @APIResponse(responseCode = "500", description = "Internal Server Error")
    })
    public Response createRole(final RoleRepresentation rep) {
        auth.roles().requireManage(roleContainer);

        if (rep.getName() == null) {
            throw new BadRequestException("role has no name");
        }

        try {
            RoleModel role = roleContainer.addRole(rep.getName());
            role.setDescription(rep.getDescription());

            Map<String, List<String>> attributes = rep.getAttributes();
            if (attributes != null) {
                for (Map.Entry<String, List<String>> attr : attributes.entrySet()) {
                    role.setAttribute(attr.getKey(), attr.getValue());
                }
            }

            rep.setId(role.getId());

            if (role.isClientRole()) {
                adminEvent.resource(ResourceType.CLIENT_ROLE);
            } else {
                adminEvent.resource(ResourceType.REALM_ROLE);
            }

            // KEYCLOAK-12754：创建时处理嵌套复合角色
            if (rep.isComposite() && rep.getComposites() != null) {
                RoleRepresentation.Composites composites = rep.getComposites();

                Set<String> compositeRealmRoles = composites.getRealm();
                if (compositeRealmRoles != null && !compositeRealmRoles.isEmpty()) {
                    Set<RoleModel> realmRoles = new LinkedHashSet<>();
                    for (String roleName : compositeRealmRoles) {
                        RoleModel realmRole = realm.getRole(roleName);
                        if (realmRole == null) {
                            throw ErrorResponse.error("Realm Role with name " + roleName + " does not exist", Response.Status.NOT_FOUND);
                        }
                        realmRoles.add(realmRole);
                    }
                    realmRoles.stream().peek(auth.roles()::requireMapComposite).forEach(role::addCompositeRole);
                }

                Map<String, List<String>> compositeClientRoles = composites.getClient();
                if (compositeClientRoles != null && !compositeClientRoles.isEmpty()) {
                    Set<Map.Entry<String, List<String>>> entries = compositeClientRoles.entrySet();
                    for (Map.Entry<String, List<String>> clientIdWithClientRoleNames : entries) {
                        String clientId = clientIdWithClientRoleNames.getKey();
                        List<String> clientRoleNames = clientIdWithClientRoleNames.getValue();
                        ClientModel client = realm.getClientByClientId(clientId);
                        if (client == null) {
                            continue;
                        }
                        Set<RoleModel> clientRoles = new LinkedHashSet<>();
                        for (String roleName : clientRoleNames) {
                            RoleModel clientRole = client.getRole(roleName);
                            if (clientRole == null) {
                                throw ErrorResponse.error("Client Role with name " + roleName + " does not exist", Response.Status.NOT_FOUND);
                            }
                            clientRoles.add(clientRole);
                        }
                        clientRoles.stream().peek(auth.roles()::requireMapComposite).forEach(role::addCompositeRole);
                    }
                }
            }

            adminEvent.operation(OperationType.CREATE).resourcePath(uriInfo, role.getName()).representation(rep).success();

            return Response.created(uriInfo.getAbsolutePathBuilder().path(Encode.encodePathSegmentAsIs(role.getName())).build()).build();
        } catch (ModelDuplicateException e) {
            throw ErrorResponse.exists("Role with name " + rep.getName() + " already exists");
        }
    }

    /**
     * 按名称获取角色。
     *
     * @param roleName 角色名称（非 ID）
     * @return 角色表示
     */
    @Path("{role-name}")
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.ROLES)
    @Operation(summary = "Get a role by name")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "", content = @Content(schema = @Schema(implementation = RoleRepresentation.class))),
        @APIResponse(responseCode = "403", description = "Forbidden"),
        @APIResponse(responseCode = "404", description = "Not Found")
    })
    public RoleRepresentation getRole(final @Parameter(description = "role's name (not id!)") @PathParam("role-name") String roleName) {
        auth.roles().requireView(roleContainer);

        RoleModel roleModel = roleContainer.getRole(roleName);
        if (roleModel == null) {
            throw new NotFoundException("Could not find role");
        }

        return getRole(roleModel);
    }

    /**
     * 按名称删除角色。
     *
     * @param roleName 角色名称（非 ID）
     */
    @Path("{role-name}")
    @DELETE
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.ROLES)
    @Operation(summary = "Delete a role by name")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "No Content"),
        @APIResponse(responseCode = "400", description = "Bad Request"),
        @APIResponse(responseCode = "403", description = "Forbidden"),
        @APIResponse(responseCode = "404", description = "Not Found")
    })
    public void deleteRole(final @Parameter(description = "role's name (not id!)") @PathParam("role-name") String roleName) {
        auth.roles().requireManage(roleContainer);
        RoleModel role = roleContainer.getRole(roleName);
        if (role == null) {
            throw new NotFoundException("Could not find role");
        } else if (realm.getDefaultRole().getId().equals(role.getId())) {
            throw ErrorResponse.error(roleName + " is default role of the realm and cannot be removed.",
                    Response.Status.BAD_REQUEST);
        }
        auth.roles().requireManage(role);
        RoleRepresentation roleRepresentation = new RoleRepresentation();
        roleRepresentation.setId(role.getId());
        roleRepresentation.setName(role.getName());

        deleteRole(role);

        if (role.isClientRole()) {
            adminEvent.resource(ResourceType.CLIENT_ROLE);
        } else {
            adminEvent.resource(ResourceType.REALM_ROLE);
        }

        adminEvent.operation(OperationType.DELETE).representation(roleRepresentation).resourcePath(uriInfo).success();

    }

    /**
     * 按名称更新角色。
     *
     * @param roleName 角色名称（非 ID）
     * @param rep 角色表示
     * @return 204 无内容
     */
    @Path("{role-name}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.ROLES)
    @Operation(summary = "Update a role by name")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "No Content"),
        @APIResponse(responseCode = "400", description = "Bad Request"),
        @APIResponse(responseCode = "403", description = "Forbidden"),
        @APIResponse(responseCode = "404", description = "Not Found"),
        @APIResponse(responseCode = "409", description = "Conflict")
    })
    public Response updateRole(final @Parameter(description = "role's name (not id!)") @PathParam("role-name") String roleName, final RoleRepresentation rep) {
        auth.roles().requireManage(roleContainer);
        RoleModel role = roleContainer.getRole(roleName);
        if (role == null) {
            throw new NotFoundException("Could not find role");
        }
        auth.roles().requireManage(role);
        try {
            updateRole(rep, role, realm, session);

            if (role.isClientRole()) {
                adminEvent.resource(ResourceType.CLIENT_ROLE);
            } else {
                adminEvent.resource(ResourceType.REALM_ROLE);
            }

            adminEvent.operation(OperationType.UPDATE).resourcePath(uriInfo).representation(rep).success();

            return Response.noContent().build();
        } catch (ModelDuplicateException e) {
            throw ErrorResponse.exists("Role with name " + rep.getName() + " already exists");
        }
    }

    /**
     * 为角色添加复合子角色。
     *
     * @param roleName 角色名称（非 ID）
     * @param roles 子角色列表
     */
    @Path("{role-name}/composites")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.ROLES)
    @Operation(summary = "Add a composite to the role")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "No Content"),
        @APIResponse(responseCode = "403", description = "Forbidden"),
        @APIResponse(responseCode = "404", description = "Not Found")
    })
    public void addComposites(final @Parameter(description = "role's name (not id!)") @PathParam("role-name") String roleName, List<RoleRepresentation> roles) {
        auth.roles().requireManage(roleContainer);
        RoleModel role = roleContainer.getRole(roleName);
        if (role == null) {
            throw new NotFoundException("Could not find role");
        }
        addComposites(auth, adminEvent, uriInfo, roles, role);
    }

    /**
     * 获取角色的复合子角色。
     *
     * @param roleName 角色名称（非 ID）
     * @return 子角色流
     */
    @Path("{role-name}/composites")
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.ROLES)
    @Operation(summary = "Get composites of the role")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "", content = @Content(schema = @Schema(implementation = RoleRepresentation.class, type = SchemaType.ARRAY))),
        @APIResponse(responseCode = "403", description = "Forbidden"),
        @APIResponse(responseCode = "404", description = "Not Found")
    })
    public Stream<RoleRepresentation> getRoleComposites(final @Parameter(description = "role's name (not id!)") @PathParam("role-name") String roleName) {
        auth.roles().requireView(roleContainer);
        RoleModel role = roleContainer.getRole(roleName);
        if (role == null) {
            throw new NotFoundException("Could not find role");
        }
        return role.getCompositesStream().map(ModelToRepresentation::toBriefRepresentation);
    }

    /**
     * 获取角色复合中的领域级子角色。
     *
     * @param roleName 角色名称（非 ID）
     * @return 领域级子角色流
     */
    @Path("{role-name}/composites/realm")
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.ROLES)
    @Operation(summary = "Get realm-level roles of the role's composite")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "", content = @Content(schema = @Schema(implementation = RoleRepresentation.class, type = SchemaType.ARRAY))),
        @APIResponse(responseCode = "403", description = "Forbidden"),
        @APIResponse(responseCode = "404", description = "Not Found")
    })
    public Stream<RoleRepresentation> getRealmRoleComposites(final @Parameter(description = "role's name (not id!)") @PathParam("role-name") String roleName) {
        auth.roles().requireView(roleContainer);
        RoleModel role = roleContainer.getRole(roleName);
        if (role == null) {
            throw new NotFoundException("Could not find role");
        }
        return getRealmRoleComposites(role);
    }

    /**
     * 获取角色复合中属于指定客户端的子角色。
     *
     * @param roleName 角色名称（非 ID）
     * @param clientUuid 客户端 UUID
     * @return 客户端级子角色流
     */
    @Path("{role-name}/composites/clients/{targetClientUuid}")
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.ROLES)
    @Operation(summary = "Get client-level roles for the client that are in the role's composite")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "", content = @Content(schema = @Schema(implementation = RoleRepresentation.class, type = SchemaType.ARRAY))),
        @APIResponse(responseCode = "403", description = "Forbidden"),
        @APIResponse(responseCode = "404", description = "Not Found")
    })
    public Stream<RoleRepresentation> getClientRoleComposites(final @Parameter(description = "role's name (not id!)") @PathParam("role-name") String roleName,
                                                                final @PathParam("targetClientUuid") String clientUuid) {
        auth.roles().requireView(roleContainer);
        RoleModel role = roleContainer.getRole(roleName);
        if (role == null) {
            throw new NotFoundException("Could not find role");
        }
        ClientModel clientModel = realm.getClientById(clientUuid);
        if (clientModel == null) {
            throw new NotFoundException("Could not find client");

        }
        return getClientRoleComposites(clientModel, role);
    }


    /**
     * 从角色复合中移除子角色。
     *
     * @param roleName 角色名称（非 ID）
     * @param roles 待移除角色列表
     */
    @Path("{role-name}/composites")
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.ROLES)
    @Operation(summary = "Remove roles from the role's composite")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "No Content"),
        @APIResponse(responseCode = "403", description = "Forbidden"),
        @APIResponse(responseCode = "404", description = "Not Found")
    })
    public void deleteComposites(
                                   final @Parameter(description = "role's name (not id!)") @PathParam("role-name") String roleName,
                                   @Parameter(description = "roles to remove") List<RoleRepresentation> roles) {

        auth.roles().requireManage(roleContainer);
        RoleModel role = roleContainer.getRole(roleName);
        if (role == null) {
            throw new NotFoundException("Could not find role");
        }
        deleteComposites(adminEvent, uriInfo, roles, role);
    }

    /**
     * 返回角色授权权限是否已初始化及资源引用。
     *
     * @param roleName 角色名称
     * @return 管理权限引用
     */
    @Path("{role-name}/management/permissions")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.ROLES)
    @Operation(summary = "Return object stating whether role Authorization permissions have been initialized or not and a reference")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "", content = @Content(schema = @Schema(implementation = ManagementPermissionReference.class))),
        @APIResponse(responseCode = "403", description = "Forbidden"),
        @APIResponse(responseCode = "404", description = "Not Found")
    })
    public ManagementPermissionReference getManagementPermissions(final @PathParam("role-name") String roleName) {
        ProfileHelper.requireFeature(Profile.Feature.ADMIN_FINE_GRAINED_AUTHZ);
        auth.roles().requireView(roleContainer);
        RoleModel role = roleContainer.getRole(roleName);
        if (role == null) {
            throw new NotFoundException("Could not find role");
        }

        AdminPermissionManagement permissions = AdminPermissions.management(session, realm);
        if (!permissions.roles().isPermissionsEnabled(role)) {
            return new ManagementPermissionReference();
        }
        return RoleByIdResource.toMgmtRef(role, permissions);
    }

    /**
     * Return object stating whether role Authorization permissions have been initialized or not and a reference
     *
     *
     * @param roleName 角色名称
     * @return 初始化后的管理权限引用
     */
    @Path("{role-name}/management/permissions")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.ROLES)
    @Operation(summary = "Return object stating whether role Authorization permissions have been initialized or not and a reference")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "", content = @Content(schema = @Schema(implementation = ManagementPermissionReference.class))),
        @APIResponse(responseCode = "403", description = "Forbidden"),
        @APIResponse(responseCode = "404", description = "Not Found")
    })
    public ManagementPermissionReference setManagementPermissionsEnabled(final @PathParam("role-name") String roleName, ManagementPermissionReference ref) {
        ProfileHelper.requireFeature(Profile.Feature.ADMIN_FINE_GRAINED_AUTHZ);
        auth.roles().requireManage(roleContainer);
        RoleModel role = roleContainer.getRole(roleName);
        if (role == null) {
            throw new NotFoundException("Could not find role");
        }

        AdminPermissionManagement permissions = AdminPermissions.management(session, realm);
        permissions.roles().setPermissionsEnabled(role, ref.isEnabled());
        if (ref.isEnabled()) {
            return RoleByIdResource.toMgmtRef(role, permissions);
        } else {
            return new ManagementPermissionReference();
        }
    }

    /**
     * 返回拥有指定角色的用户流。
     *
     * @param roleName 角色名称
     * @param firstResult first result to return. Ignored if negative or {@code null}.
     * @param maxResults maximum number of results to return. Unbounded if negative.
     * @param briefRepresentation Boolean which defines whether brief representations are returned (default: false)
     * @return a non-empty {@code Stream} of users.
     */
    @Path("{role-name}/users")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.ROLES)
    @Operation(summary = "Returns a stream of users that have the specified role name.")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "", content = @Content(schema = @Schema(implementation = UserRepresentation.class, type = SchemaType.ARRAY))),
        @APIResponse(responseCode = "403", description = "Forbidden"),
        @APIResponse(responseCode = "404", description = "Not Found")
    })
    public Stream<UserRepresentation> getUsersInRole(final @Parameter(description = "the role name.") @PathParam("role-name") String roleName,
                                                    @Parameter(description = "Boolean which defines whether brief representations are returned (default: false)") @QueryParam("briefRepresentation") Boolean briefRepresentation,
                                                    @Parameter(description = "first result to return. Ignored if negative or {@code null}.") @QueryParam("first") Integer firstResult,
                                                    @Parameter(description = "Maximum number of results to return. Unbounded if negative.") @QueryParam("max") @DefaultValue(Constants.DEFAULT_MAX_RESULTS_STR) Integer maxResults) {

        auth.roles().requireView(roleContainer);
        auth.users().requireQuery();

        firstResult = firstResult != null ? firstResult : 0;
        maxResults = maxResults != null ? maxResults : Constants.DEFAULT_MAX_RESULTS;

        RoleModel role = roleContainer.getRole(roleName);
        if (role == null) {
            throw new NotFoundException("Could not find role");
        }

        boolean briefRep = Boolean.TRUE.equals(briefRepresentation);

        return session.users().getRoleMembersStream(realm, role, firstResult, maxResults)
                .map((u) -> ModelToRepresentation.toRepresentation(session, u, briefRep));
    }

    /**
     * 返回拥有指定角色的组流。
     *
     * @param roleName 角色名称
     * @param firstResult first result to return. Ignored if negative or {@code null}.
     * @param maxResults maximum number of results to return. Unbounded if negative.
     * @param briefRepresentation if false, return a full representation of the {@code GroupRepresentation} objects.
     * @return a non-empty {@code Stream} of groups.
     */
    @Path("{role-name}/groups")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.ROLES)
    @Operation(summary = "Returns a stream of groups that have the specified role name")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "", content = @Content(schema = @Schema(implementation = UserRepresentation.class, type = SchemaType.ARRAY))),
        @APIResponse(responseCode = "403", description = "Forbidden"),
        @APIResponse(responseCode = "404", description = "Not Found")
    })
    public Stream<GroupRepresentation> getGroupsInRole(final @Parameter(description = "the role name.") @PathParam("role-name") String roleName,
                                                    @Parameter(description = "First result to return. Ignored if negative or {@code null}.") @QueryParam("first") Integer firstResult,
                                                    @Parameter(description = "Maximum number of results to return. Unbounded if negative.") @QueryParam(Constants.DEFAULT_MAX_RESULTS_STR) Integer maxResults,
                                                    @Parameter(description = "If false, return a full representation of the {@code GroupRepresentation} objects.") @QueryParam("briefRepresentation") @DefaultValue("true") boolean briefRepresentation) {

        auth.roles().requireView(roleContainer);
        firstResult = firstResult != null ? firstResult : 0;
        maxResults = maxResults != null ? maxResults : Constants.DEFAULT_MAX_RESULTS;

        RoleModel role = roleContainer.getRole(roleName);
        if (role == null) {
            throw new NotFoundException("Could not find role");
        }

        return session.groups().getGroupsByRoleStream(realm, role, firstResult, maxResults)
                .map(g -> ModelToRepresentation.toRepresentation(g, !briefRepresentation));
    }
}
