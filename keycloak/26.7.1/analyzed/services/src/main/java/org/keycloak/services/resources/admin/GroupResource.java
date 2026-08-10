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

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.authorization.fgap.AdminPermissionsSchema;
import org.keycloak.common.Profile;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.Constants;
import org.keycloak.models.GroupModel;
import org.keycloak.models.GroupModel.GroupPathChangeEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.ManagementPermissionReference;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.resources.KeycloakOpenAPI;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;
import org.keycloak.services.resources.admin.fgap.AdminPermissionManagement;
import org.keycloak.services.resources.admin.fgap.AdminPermissions;
import org.keycloak.utils.GroupUtils;
import org.keycloak.utils.ProfileHelper;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.NoCache;

import static org.keycloak.utils.StreamsUtil.paginatedStream;
import static org.keycloak.utils.StringUtil.isBlank;

/**
 * 单个组 REST 资源。
 * <p>管理组 CRUD、子组、成员、角色映射及细粒度管理权限。</p>
 *
 * @resource Groups
 * @author Bill Burke
 */
@Extension(name = KeycloakOpenAPI.Profiles.ADMIN, value = "")
public class GroupResource {

    /** 当前领域 */
    private final RealmModel realm;
    /** Keycloak 会话 */
    private final KeycloakSession session;
    /** 细粒度权限评估器 */
    private final AdminPermissionEvaluator auth;
    /** 管理事件构建器 */
    private final AdminEventBuilder adminEvent;
    /** 目标组 */
    private final GroupModel group;

    /** 构造单个组资源。
     * @param realm 当前领域
     * @param group 目标组
     * @param session Keycloak 会话
     * @param auth 权限评估器
     * @param adminEvent 管理事件构建器
     */
    public GroupResource(RealmModel realm, GroupModel group, KeycloakSession session, AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        this.realm = realm;
        this.session = session;
        this.auth = auth;
        this.adminEvent = adminEvent.resource(ResourceType.GROUP);
        this.group = group;
    }

    /** 获取组详情（含 access 与子组计数）。
     * @return 组表示
     */
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.GROUPS)
    @Operation()
    public GroupRepresentation getGroup() {
        this.auth.groups().requireView(group);

        GroupRepresentation rep = GroupUtils.toRepresentation(this.auth.groups(), group, true);

        rep.setAccess(auth.groups().getAccess(group));

        return GroupUtils.populateSubGroupCount(group, rep);
    }

    /**
     * 更新组（不处理子组）。
     * @param rep 组表示
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.GROUPS)
    @Operation( summary = "Update group, ignores subgroups.")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "No Content"),
        @APIResponse(responseCode = "400", description = "Bad Request"),
        @APIResponse(responseCode = "409", description = "Conflict")
    })
    public Response updateGroup(GroupRepresentation rep) {
        this.auth.groups().requireManage(group);

        String groupName = rep.getName();

        if (isBlank(groupName)) {
            throw ErrorResponse.error("Group name is missing", Response.Status.BAD_REQUEST);
        }

        if (rep.getId() != null && !group.getId().equals(rep.getId())) {
            throw ErrorResponse.error("Invalid group id", Response.Status.BAD_REQUEST);
        }

        if (!Objects.equals(groupName, group.getName())) {
            boolean exists = siblings().filter(s -> !Objects.equals(s.getId(), group.getId()))
                    .anyMatch(s -> Objects.equals(s.getName(), groupName));
            if (exists) {
                throw ErrorResponse.exists("Sibling group named '" + groupName + "' already exists.");
            }
        }

        updateGroup(rep, group, realm, session);
        adminEvent.operation(OperationType.UPDATE).resourcePath(session.getContext().getUri()).representation(rep).success();

        return Response.noContent().build();
    }

    /** 获取同级组流（顶级或同一父组下） */
    private Stream<GroupModel> siblings() {
        if (group.getParentId() == null) {
            return session.groups().getTopLevelGroupsStream(realm);
        } else {
            return group.getParent().getSubGroupsStream();
        }
    }

    /** 删除组 */
    @DELETE
    @Tag(name = KeycloakOpenAPI.Admin.Tags.GROUPS)
    @Operation()
    public void deleteGroup() {
        this.auth.groups().requireManage(group);
        GroupRepresentation groupRepresentation = new GroupRepresentation();
        groupRepresentation.setId(group.getId());
        groupRepresentation.setName(group.getName());

        realm.removeGroup(group);
        adminEvent.operation(OperationType.DELETE).representation(groupRepresentation).resourcePath(session.getContext().getUri()).success();
    }

    /**
     * 分页返回该组的直接子组。
     * @param search 名称搜索
     * @param exact 是否精确匹配
     * @param first 分页偏移
     * @param max 最大条数
     * @param briefRepresentation 是否简要表示
     * @param subGroupsCount 是否填充子组计数
     * @return 子组表示流
     */
    @GET
    @Path("children")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.GROUPS)
    @Operation( summary = "Return a paginated list of subgroups that have a parent group corresponding to the group on the URL")
    public Stream<GroupRepresentation> getSubGroups(
            @Parameter(description = "A String representing either an exact group name or a partial name, defaults to prefix search.") @QueryParam("search") String search,
            @Parameter(description = "Boolean which defines whether the params \"search\" must match exactly or not") @QueryParam("exact") Boolean exact,
            @Parameter(description = "The position of the first result to be returned (pagination offset).") @QueryParam("first") @DefaultValue("0") Integer first,
            @Parameter(description = "The maximum number of results that are to be returned. Defaults to 10") @QueryParam("max") @DefaultValue("10") Integer max,
            @Parameter(description = "Boolean which defines whether brief groups representations are returned or not (default: false)") @QueryParam("briefRepresentation") @DefaultValue("false") Boolean briefRepresentation,
            @Parameter(description = "Boolean which defines whether to return the count of subgroups for each subgroup of this group (default: true)") @QueryParam("subGroupsCount") @DefaultValue("true") Boolean subGroupsCount) {
        this.auth.groups().requireList();
        this.auth.groups().requireView(group);

        Stream<GroupModel> stream = group.getSubGroupsStream(search, exact, -1, -1);

        if (!AdminPermissionsSchema.SCHEMA.isAdminPermissionsEnabled(realm)) {
            stream = stream.filter(auth.groups()::canView);
        }

        return paginatedStream(stream, first, max)
            .map(g -> {
                GroupRepresentation rep = GroupUtils.toRepresentation(auth.groups(), g, !briefRepresentation);

                if (subGroupsCount) {
                    return GroupUtils.populateSubGroupCount(g, rep);
                }

                return rep;
            });
    }

    /**
     * 设置或创建子组（已存在则移动父级）。
     * @param rep 子组表示
     */
    @POST
    @Path("children")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.GROUPS)
    @Operation( summary = "Set or create child.", description = "This will just set the parent if it exists. Create it and set the parent if the group doesn’t exist.")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "Created"),
        @APIResponse(responseCode = "204", description = "No Content"),
        @APIResponse(responseCode = "400", description = "Bad Request"),
        @APIResponse(responseCode = "409", description = "Conflict")
    })
    public Response addChild(GroupRepresentation rep) {
        this.auth.groups().requireManage(group);

        String groupName = rep.getName();
        if (isBlank(groupName)) {
            throw ErrorResponse.error("Group name is missing", Response.Status.BAD_REQUEST);
        }

        try {
            Response.ResponseBuilder builder = Response.status(204);
            GroupModel child;
            if (rep.getId() != null) {
                child = realm.getGroupById(rep.getId());
                if (child == null) {
                    throw new NotFoundException("Could not find child by id");
                }
                auth.groups().requireManage(child);
                if (!Objects.equals(child.getParentId(), group.getId())) {
                    realm.moveGroup(child, group);
                }
                adminEvent.operation(OperationType.UPDATE);
            } else {
                child = realm.createGroup(groupName, group);
                updateGroup(rep, child, realm, session);
                URI uri = session.getContext().getUri().getBaseUriBuilder()
                        .path(AdminRoot.class)
                        .path(AdminRoot.class, "getRealmsAdmin")
                        .path(RealmsAdminResource.class, "getRealmAdmin")
                        .path(RealmAdminResource.class, "getGroups")
                        .path(GroupsResource.class, "getGroupById")
                        .build(realm.getName(), child.getId());
                builder.status(201).location(uri);
                rep.setId(child.getId());
                adminEvent.operation(OperationType.CREATE);

            }
            adminEvent.resourcePath(session.getContext().getUri()).representation(rep).success();

            GroupRepresentation childRep = GroupUtils.toRepresentation(auth.groups(), child, true);
            return builder.type(MediaType.APPLICATION_JSON_TYPE).entity(childRep).build();
        } catch (ModelDuplicateException e) {
            throw ErrorResponse.exists("Sibling group named '" + groupName + "' already exists.");
        }
    }

    /** 将组表示同步到模型（名称、属性、描述），名称变更时触发路径变更事件 */
    public static void updateGroup(GroupRepresentation rep, GroupModel model, RealmModel realm, KeycloakSession session) {
        String newName = rep.getName();
        if (newName != null) {
            String existingName = model.getName();
            if (!newName.equals(existingName)) {
                String previousPath = KeycloakModelUtils.buildGroupPath(model);

                model.setName(newName);

                String newPath = KeycloakModelUtils.buildGroupPath(model);

                GroupPathChangeEvent.fire(model, newPath, previousPath, session);
            }
        }

        if (rep.getAttributes() != null) {
            Set<String> attrsToRemove = new HashSet<>(model.getAttributes().keySet());
            attrsToRemove.removeAll(rep.getAttributes().keySet());
            for (Map.Entry<String, List<String>> attr : rep.getAttributes().entrySet()) {
                model.setAttribute(attr.getKey(), attr.getValue());
            }

            for (String attr : attrsToRemove) {
                model.removeAttribute(attr);
            }
        }

        model.setDescription(rep.getDescription());
    }

    /** 组角色映射子资源 */
    @Path("role-mappings")
    public RoleMapperResource getRoleMappings() {
        AdminPermissionEvaluator.RequirePermissionCheck manageCheck = () -> auth.groups().requireManage(group);
        AdminPermissionEvaluator.RequirePermissionCheck viewCheck = () -> auth.groups().requireView(group);
        return new RoleMapperResource(session, auth, group, adminEvent, manageCheck, viewCheck);

    }

    /**
     * 获取组成员用户流。
     * @param firstResult 分页偏移
     * @param maxResults 最大条数（默认 100）
     * @param briefRepresentation 是否仅返回基本信息
     * @return 用户表示流
     */
    @GET
    @NoCache
    @Path("members")
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.GROUPS)
    @Operation( summary = "Get users Returns a stream of users, filtered according to query parameters")
    public Stream<UserRepresentation> getMembers(@Parameter(description = "Pagination offset") @QueryParam("first") Integer firstResult,
                                                 @Parameter(description = "Maximum results size (defaults to 100)") @QueryParam("max") Integer maxResults,
                                                 @Parameter(description = "Only return basic information (only guaranteed to return id, username, created, first and last name, email, enabled state, email verification state, federation link, and access. Note that it means that namely user attributes, required actions, and not before are not returned.)")
                                                     @QueryParam("briefRepresentation") Boolean briefRepresentation) {
        this.auth.groups().requireViewMembers(group);

        firstResult = firstResult != null ? firstResult : 0;
        maxResults = maxResults != null ? maxResults : Constants.DEFAULT_MAX_RESULTS;
        boolean briefRep = Boolean.TRUE.equals(briefRepresentation);

        return session.users().getGroupMembersStream(realm, group, firstResult, maxResults)
                .map(user -> ModelToRepresentation.toRepresentation(session, user, briefRep));
    }

    /**
     * 获取组细粒度管理权限初始化状态与引用。
     * @return 管理权限引用
     */
    @Path("management/permissions")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.GROUPS)
    @Operation( summary = "Return object stating whether client Authorization permissions have been initialized or not and a reference")
    public ManagementPermissionReference getManagementPermissions() {
        ProfileHelper.requireFeature(Profile.Feature.ADMIN_FINE_GRAINED_AUTHZ);
        auth.groups().requireView(group);

        AdminPermissionManagement permissions = AdminPermissions.management(session, realm);
        if (!permissions.groups().isPermissionsEnabled(group)) {
            return new ManagementPermissionReference();
        }
        return toMgmtRef(group, permissions);
    }

    /** 构建已启用的组管理权限引用 */
    private ManagementPermissionReference toMgmtRef(GroupModel group, AdminPermissionManagement permissions) {
        ManagementPermissionReference ref = new ManagementPermissionReference();
        ref.setEnabled(true);
        ref.setResource(permissions.groups().resource(group).getId());
        ref.setScopePermissions(permissions.groups().getPermissions(group));
        return ref;
    }


    /**
     * 启用或禁用组细粒度管理权限。
     * @param ref 管理权限引用
     * @return 更新后的权限引用
     */
    @Path("management/permissions")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.GROUPS)
    @Operation( summary = "Return object stating whether client Authorization permissions have been initialized or not and a reference")
    public ManagementPermissionReference setManagementPermissionsEnabled(ManagementPermissionReference ref) {
        ProfileHelper.requireFeature(Profile.Feature.ADMIN_FINE_GRAINED_AUTHZ);
        auth.groups().requireManage(group);

        AdminPermissionManagement permissions = AdminPermissions.management(session, realm);
        permissions.groups().setPermissionsEnabled(group, ref.isEnabled());
        if (ref.isEnabled()) {
            return toMgmtRef(group, permissions);
        } else {
            return new ManagementPermissionReference();
        }
    }
}
