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

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.ManagementPermissionReference;
import org.keycloak.representations.idm.ManagementPermissionRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

/**
 * 单个用户组的管理 REST 资源。
 * <p>
 * 支持组 CRUD、细粒度权限管理、子组层级、角色映射及成员查询等操作。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface GroupResource {

    /**
     * 启用或禁用细粒度权限功能。
     * 返回更新后的服务器状态，封装于 {@link ManagementPermissionReference} 中。
     *
     * @param status 要应用的权限状态请求
     * @return 指示更新后权限状态的引用对象
     */
    @PUT
    @Path("/management/permissions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    ManagementPermissionReference setPermissions(ManagementPermissionRepresentation status);

    /**
     * 返回细粒度权限是否已启用。
     *
     * @return 当前权限功能的表示对象
     */
    @GET
    @Path("/management/permissions")
    @Produces(MediaType.APPLICATION_JSON)
    ManagementPermissionReference getPermissions();

    /**
     * 获取组表示对象，不展开层级（子组字段不会被填充）。
     *
     * @return 当前组的表示对象
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    GroupRepresentation toRepresentation();

    /**
     * 更新组属性。
     *
     * @param rep 组表示对象
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    void update(GroupRepresentation rep);

    /** 删除当前组。 */
    @DELETE
    void remove();

    /**
     * 获取本组下子组的分页列表。
     *
     * @param first 分页起始位置
     * @param max 返回结果的最大数量
     * @param briefRepresentation 若为 {@code true}，每个子组仅含基本信息
     *                           （id、name、path、parentId）；若为 {@code false}，返回完整表示
     *                           （含角色映射与属性）
     */
    @GET
    @Path("children")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    List<GroupRepresentation> getSubGroups(@QueryParam("first") Integer first, @QueryParam("max") Integer max, @QueryParam("briefRepresentation") Boolean briefRepresentation);

    /**
     * 获取本组下子组的分页列表。
     *
     * @param first 分页起始位置
     * @param max 返回结果的最大数量
     * @param briefRepresentation 若为 {@code true}，每个子组仅含基本信息
     *                           （id、name、path、parentId）；若为 {@code false}，返回完整表示
     *                           （含角色映射与属性）
     * @param subGroupsCount 若为 {@code true}，为每个子组返回其子组数量；默认为 true。
     *                       自 Keycloak 26.3 起支持；旧版本中始终为 true
     */
    @GET
    @Path("children")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    List<GroupRepresentation> getSubGroups(@QueryParam("first") Integer first, @QueryParam("max") Integer max,
                                           @QueryParam("briefRepresentation") Boolean briefRepresentation,
                                           @QueryParam("subGroupsCount") Boolean subGroupsCount);

    /**
     * 按指定参数过滤，获取本组下子组的分页列表。
     *
     * @param search 精确或部分组名；为空或 {@code null} 时返回所有子组。
     *               自 Keycloak 25 起可用；旧版本忽略此参数
     * @param exact 若为 {@code true}，对 {@code search} 进行精确匹配；否则部分匹配。
     *              自 Keycloak 25 起可用；旧版本忽略此参数
     * @param first 分页起始位置
     * @param max 返回结果的最大数量
     * @param briefRepresentation 若为 {@code true}，每个子组仅含基本信息；否则返回完整表示
     */
    @GET
    @Path("children")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    List<GroupRepresentation> getSubGroups(
            @QueryParam("search") String search,
            @QueryParam("exact") Boolean exact,
            @QueryParam("first") Integer first,
            @QueryParam("max") Integer max,
            @QueryParam("briefRepresentation") Boolean briefRepresentation);

    /**
     * 按指定参数过滤，获取本组下子组的分页列表。
     *
     * @param search 精确或部分组名；为空或 {@code null} 时返回所有子组。
     *               自 Keycloak 25 起可用；旧版本忽略此参数
     * @param exact 若为 {@code true}，对 {@code search} 进行精确匹配；否则部分匹配。
     *              自 Keycloak 25 起可用；旧版本忽略此参数
     * @param first 分页起始位置
     * @param max 返回结果的最大数量
     * @param briefRepresentation 若为 {@code true}，每个子组仅含基本信息；否则返回完整表示
     * @param subGroupsCount 若为 {@code true}，为每个子组返回其子组数量；默认为 true。
     *                       自 Keycloak 26.3 起支持；旧版本中始终为 true
     */
    @GET
    @Path("children")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    List<GroupRepresentation> getSubGroups(
            @QueryParam("search") String search,
            @QueryParam("exact") Boolean exact,
            @QueryParam("first") Integer first,
            @QueryParam("max") Integer max,
            @QueryParam("briefRepresentation") Boolean briefRepresentation,
            @QueryParam("subGroupsCount") Boolean subGroupsCount);

    /**
     * 设置或创建子组。若子组已存在则更新其父级；不存在则创建并设置父级。
     *
     * @param rep 子组表示对象
     */
    @POST
    @Path("children")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Response subGroup(GroupRepresentation rep);

    /** 获取本组的角色映射资源。 */
    @Path("role-mappings")
    RoleMappingResource roles();

    /**
     * 获取组成员列表。
     * <p>
     * 返回组内所有用户，最多 100 条。
     *
     * @return 用户表示对象列表
     */
    @GET
    @Path("/members")
    @Produces(MediaType.APPLICATION_JSON)
    List<UserRepresentation> members();

    /**
     * 按分页参数获取组成员列表。
     *
     * @param firstResult 分页偏移量
     * @param maxResults 分页大小
     * @return 用户表示对象列表
     */
    @GET
    @Path("/members")
    @Produces(MediaType.APPLICATION_JSON)
    List<UserRepresentation> members(@QueryParam("first") Integer firstResult,
                                     @QueryParam("max") Integer maxResults);

    /**
     * 按分页参数获取组成员列表，可选择简要表示。
     *
     * @param firstResult 分页偏移量
     * @param maxResults 分页大小
     * @param briefRepresentation 若为 true，仅返回基本信息（id、username、created、
     *      first/last name、email、enabled、emailVerified、federationLink、access）；
     *      用户属性、必需操作及 not-before 等字段不会返回
     * @return 用户表示对象列表
     */
    @GET
    @Path("/members")
    @Produces(MediaType.APPLICATION_JSON)
    List<UserRepresentation> members(@QueryParam("first") Integer firstResult,
                                     @QueryParam("max") Integer maxResults,
                                     @QueryParam("briefRepresentation") Boolean briefRepresentation);
}
