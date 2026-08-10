/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
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
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.MemberRepresentation;

/**
 * 组织内单个用户组的管理 REST 资源。
 * <p>
 * 支持组 CRUD、子组管理、角色映射及成员增删查等操作。
 */
public interface OrganizationGroupResource {

    /**
     * 获取组织组的表示对象。
     *
     * @param subGroupsCount 是否返回子组数量
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    GroupRepresentation toRepresentation(@QueryParam("subGroupsCount") boolean subGroupsCount);

    /**
     * 角色映射资源。
     *
     * @return 角色映射管理资源
     * @since Keycloak server 26.7.0
     */
    @Path("role-mappings")
    RoleMappingResource roles();

    /** 更新组织组属性。 */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    Response update(GroupRepresentation rep);

    /** 删除当前组织组。 */
    @DELETE
    void delete();

    /**
     * 获取子组的分页列表，支持名称搜索。
     *
     * @param search 组名搜索条件
     * @param exact 是否精确匹配
     * @param first 分页起始位置
     * @param max 最大返回数量
     */
    @GET
    @Path("children")
    @Produces(MediaType.APPLICATION_JSON)
    List<GroupRepresentation> getSubGroups(
            @QueryParam("search") String search,
            @QueryParam("exact") Boolean exact,
            @QueryParam("first") Integer first,
            @QueryParam("max") Integer max
    );

    /** 创建或添加子组。 */
    @POST
    @Path("children")
    @Consumes(MediaType.APPLICATION_JSON)
    Response addSubGroup(GroupRepresentation rep);

    /**
     * 获取组成员的分页列表。
     *
     * @param first 分页起始位置
     * @param max 最大返回数量
     * @param briefRepresentation 是否返回简要成员表示
     */
    @GET
    @Path("members")
    @Produces(MediaType.APPLICATION_JSON)
    List<MemberRepresentation> getMembers(
            @QueryParam("first") Integer first,
            @QueryParam("max") Integer max,
            @QueryParam("briefRepresentation") Boolean briefRepresentation
    );

    /** 将用户添加为本组成员。 */
    @PUT
    @Path("members/{userId}")
    void addMember(@PathParam("userId") String userId);

    /** 将用户从本组移除。 */
    @DELETE
    @Path("members/{userId}")
    void removeMember(@PathParam("userId") String userId);
}
