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
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.idm.GroupRepresentation;

/**
 * 组织内用户组集合的管理 REST 资源。
 * <p>
 * 支持创建顶级组织组、按多种条件查询组列表、按路径定位组及访问单个组资源。
 *
 * @since Keycloak server 26.6.0. All the child endpoints are also available since that version unless mentioned otherwise<p>
 */
public interface OrganizationGroupsResource {

    /** 创建组织内的顶级组。 */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response addTopLevelGroup(GroupRepresentation rep);

    /**
     * 获取组织内所有组，支持搜索与分页。
     *
     * @param search 组名搜索条件
     * @param searchQuery 属性查询表达式（格式 {@code key1:value1 key2:value2}）
     * @param exact 是否精确匹配组名
     * @param first 分页起始位置
     * @param max 最大返回数量
     * @param briefRepresentation 是否返回简要组表示
     * @param subGroupsCount 是否返回每个组的子组数量
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<GroupRepresentation> getAll(
            @QueryParam("search") String search,
            @QueryParam("q") String searchQuery,
            @QueryParam("exact") Boolean exact,
            @QueryParam("first") Integer first,
            @QueryParam("max") Integer max,
            @QueryParam("briefRepresentation") boolean briefRepresentation,
            @QueryParam("subGroupsCount") boolean subGroupsCount
    );

    /**
     * 获取组织内所有组，支持搜索、分页及层级展开。
     *
     * @param search 组名搜索条件
     * @param searchQuery 属性查询表达式
     * @param exact 是否精确匹配组名
     * @param first 分页起始位置
     * @param max 最大返回数量
     * @param briefRepresentation 是否返回简要组表示
     * @param populateHierarchy 是否填充组层级结构
     * @param subGroupsCount 是否返回每个组的子组数量
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<GroupRepresentation> getAll(
            @QueryParam("search") String search,
            @QueryParam("q") String searchQuery,
            @QueryParam("exact") Boolean exact,
            @QueryParam("first") Integer first,
            @QueryParam("max") Integer max,
            @QueryParam("briefRepresentation") boolean briefRepresentation,
            @QueryParam("populateHierarchy") boolean populateHierarchy,
            @QueryParam("subGroupsCount") boolean subGroupsCount
    );

    /**
     * 按路径获取组织组。
     *
     * @param path 组的完整路径
     * @param subGroupsCount 是否返回子组数量
     */
    @GET
    @Path("group-by-path/{path: .*}")
    @Produces(MediaType.APPLICATION_JSON)
    GroupRepresentation getGroupByPath(
            @PathParam("path") String path,
            @QueryParam("subGroupsCount") boolean subGroupsCount
    );

    /** 按组 ID 获取单个组织组资源。 */
    @Path("{group-id}")
    OrganizationGroupResource group(@PathParam("group-id") String groupId);
}
