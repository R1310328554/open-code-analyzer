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

package org.keycloak.admin.client.resource;

import java.util.List;
import java.util.Set;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.ManagementPermissionReference;
import org.keycloak.representations.idm.ManagementPermissionRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

/**
 * 单个角色的管理 REST 资源。
 * <p>
 * 提供角色 CRUD、细粒度权限管理、组合角色操作，
 * 以及查询拥有该角色的用户与组等功能。
 *
 * @author rodrigo.sasaki@icarros.com.br
 */
public interface RoleResource {

    /**
     * 启用或禁用细粒度权限功能。
     * <p>
     * 返回更新后的服务器状态，封装于 {@link ManagementPermissionReference} 中。
     *
     * @param status 待应用的权限状态请求
     * @return 指示更新后状态的权限引用
     */
    @PUT
    @Path("/management/permissions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    ManagementPermissionReference setPermissions(ManagementPermissionRepresentation status);

    /**
     * 查询细粒度权限功能是否已启用。
     *
     * @return 当前权限功能的表示对象
     */
    @GET
    @Path("/management/permissions")
    @Produces(MediaType.APPLICATION_JSON)
    ManagementPermissionReference getPermissions();

    /** 获取当前角色的表示对象。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    RoleRepresentation toRepresentation();

    /** 更新当前角色配置。 */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    void update(RoleRepresentation roleRepresentation);

    /** 删除当前角色。 */
    @DELETE
    void remove();

    /** 获取当前角色的全部组合角色。 */
    @GET
    @Path("composites")
    @Produces(MediaType.APPLICATION_JSON)
    Set<RoleRepresentation> getRoleComposites();

    /** 获取当前角色的领域级组合角色。 */
    @GET
    @Path("composites/realm")
    @Produces(MediaType.APPLICATION_JSON)
    Set<RoleRepresentation> getRealmRoleComposites();

    /**
     * 获取当前角色在指定客户端下的组合角色。
     *
     * @param clientUuid 客户端 UUID
     * @return 客户端级组合角色集合
     */
    @GET
    @Path("composites/clients/{clientUuid}")
    @Produces(MediaType.APPLICATION_JSON)
    Set<RoleRepresentation> getClientRoleComposites(@PathParam("clientUuid") String clientUuid);

    /**
     * 为当前角色添加组合角色。
     *
     * @param rolesToAdd 待添加的组合角色列表
     */
    @POST
    @Path("composites")
    @Consumes(MediaType.APPLICATION_JSON)
    void addComposites(List<RoleRepresentation> rolesToAdd);

    /**
     * 从当前角色移除组合角色。
     *
     * @param rolesToRemove 待移除的组合角色列表
     */
    @DELETE
    @Path("composites")
    @Consumes(MediaType.APPLICATION_JSON)
    void deleteComposites(List<RoleRepresentation> rolesToRemove);

    /**
     * 获取拥有当前角色的用户成员。
     * <p>
     * 返回按用户名升序排列的用户列表。
     * </p>
     * <p>
     * 注意：此方法仅返回前 100 名用户。如需获取全部用户，请使用分页
     * （参见 {@link #getUserMembers(Integer, Integer)}）。
     * </p>
     *
     * @return 拥有该角色的用户列表
     */
    @GET
    @Path("users")
    @Produces(MediaType.APPLICATION_JSON)
    List<UserRepresentation> getUserMembers();

    /**
     * 分页获取拥有当前角色的用户成员。
     * <p>
     * 返回按用户名升序排列的用户列表，支持分页参数。
     * </p>
     *
     * @param firstResult 分页偏移量
     * @param maxResults 分页大小
     * @return 拥有该角色的用户列表
     */
    @GET
    @Path("users")
    @Produces(MediaType.APPLICATION_JSON)
    List<UserRepresentation> getUserMembers(@QueryParam("first") Integer firstResult,
            @QueryParam("max") Integer maxResults);

    /**
     * 分页获取拥有当前角色的用户成员（可选简要表示）。
     * <p>
     * 返回按用户名升序排列的用户列表，支持分页参数。
     * </p>
     *
     * @param briefRepresentation 是否以简要形式返回用户（自 Keycloak 26 起可用；旧版本忽略此参数，默认为 false）
     * @param firstResult 分页偏移量
     * @param maxResults 分页大小
     * @return 拥有该角色的用户列表
     */
    @GET
    @Path("users")
    @Produces(MediaType.APPLICATION_JSON)
    List<UserRepresentation> getUserMembers(@QueryParam("briefRepresentation") Boolean briefRepresentation,
            @QueryParam("first") Integer firstResult,
            @QueryParam("max") Integer maxResults);

    /**
     * 获取拥有当前角色的组成员。
     * <p>
     * 返回拥有该角色的组列表。
     * </p>
     *
     * @return 拥有该角色的组集合
     */
    @GET
    @Path("groups")
    @Produces(MediaType.APPLICATION_JSON)
    Set<GroupRepresentation> getRoleGroupMembers();

    /**
     * 分页获取拥有当前角色的组成员。
     * <p>
     * 返回拥有该角色的组列表，支持分页参数。
     * </p>
     *
     * @param firstResult 分页偏移量
     * @param maxResults 分页大小
     * @return 拥有该角色的组集合
     */
    @GET
    @Path("groups")
    @Produces(MediaType.APPLICATION_JSON)
    Set<GroupRepresentation> getRoleGroupMembers(@QueryParam("first") Integer firstResult,
                                               @QueryParam("max") Integer maxResults);

    /**
     * 获取拥有当前角色的用户成员。
     * <p>
     * 返回拥有该角色的用户集合。
     * </p>
     *
     * @return 拥有该角色的用户集合
     *
     * @deprecated 请改用 {@link #getUserMembers()}
     */
    @GET
    @Path("users")
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated
    Set<UserRepresentation> getRoleUserMembers();

    /**
     * 分页获取拥有当前角色的用户成员。
     * <p>
     * 返回拥有该角色的用户集合，支持分页参数。
     * </p>
     *
     * @param firstResult 分页偏移量
     * @param maxResults 分页大小
     * @return 拥有该角色的用户集合
     *
     * @deprecated 请改用 {@link #getUserMembers(Integer, Integer)}
     */
    @GET
    @Path("users")
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated
    Set<UserRepresentation> getRoleUserMembers(@QueryParam("first") Integer firstResult,
                                               @QueryParam("max") Integer maxResults);
}
