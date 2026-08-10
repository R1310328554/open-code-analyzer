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
import java.util.Map;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
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
 * 领域内用户组集合的管理 REST 资源。
 * <p>
 * 支持组列表查询（含搜索、分页、层级展开）、计数、创建及按 ID 访问单个组。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface GroupsResource {

    /**
     * 获取所有组。
     *
     * @return 包含所有组的列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<GroupRepresentation> groups();

    /**
     * 按分页参数获取组列表。
     *
     * @param first 分页起始索引
     * @param max 最大返回数量
     * @return 组列表切片
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    List<GroupRepresentation> groups(@QueryParam("first") Integer first, @QueryParam("max") Integer max);

    /**
     * 按名称搜索并分页获取组列表。
     *
     * @param search 精确或部分组名
     * @param first 分页起始索引
     * @param max 最大返回数量
     * @return 组列表切片
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    List<GroupRepresentation> groups(@QueryParam("search") String search,
                                     @QueryParam("first") Integer first,
                                     @QueryParam("max") Integer max);

    /**
     * 按名称搜索并分页获取组列表，可选择简要表示。
     *
     * @param search 精确或部分组名
     * @param first 分页起始索引
     * @param max 最大返回数量
     * @param briefRepresentation 若为 {@code true}，仅返回基本信息（id、name、path、parentId）；
     *                            若为 {@code false}，返回完整表示（含角色映射与属性）
     * @return 组列表切片
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    List<GroupRepresentation> groups(@QueryParam("search") String search,
                                     @QueryParam("first") Integer first,
                                     @QueryParam("max") Integer max,
                                     @QueryParam("briefRepresentation") @DefaultValue("true") boolean briefRepresentation);

    /**
     * 按名称搜索并分页获取组列表，支持精确匹配与子组计数。
     *
     * @param search 精确或部分组名
     * @param exact 若为 {@code true}，对 {@code search} 精确匹配；否则部分匹配
     * @param first 分页起始索引
     * @param max 最大返回数量
     * @param briefRepresentation 若为 {@code true}，仅返回基本信息；否则返回完整表示
     * @param subGroupsCount 若为 {@code true}，为每个组返回子组数量；默认为 true。
     *                       自 Keycloak 26.3 起支持；旧版本中始终为 true
     * @return 组列表切片
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    List<GroupRepresentation> groups(@QueryParam("search") String search,
                                     @QueryParam("exact") Boolean exact,
                                     @QueryParam("first") Integer first,
                                     @QueryParam("max") Integer max,
                                     @QueryParam("briefRepresentation") @DefaultValue("true") boolean briefRepresentation,
                                     @QueryParam("subGroupsCount") @DefaultValue("true") Boolean subGroupsCount);

    /**
     * 按名称搜索并分页获取组列表，支持精确匹配。
     *
     * @param search 精确或部分组名
     * @param exact 若为 {@code true}，对 {@code search} 精确匹配；否则部分匹配
     * @param first 分页起始索引
     * @param max 最大返回数量
     * @param briefRepresentation 若为 {@code true}，仅返回基本信息；否则返回完整表示
     * @return 组列表切片
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    List<GroupRepresentation> groups(@QueryParam("search") String search,
                                     @QueryParam("exact") Boolean exact,
                                     @QueryParam("first") Integer first,
                                     @QueryParam("max") Integer max,
                                     @QueryParam("briefRepresentation") @DefaultValue("true") boolean briefRepresentation);

    /**
     * 统计所有组的数量。
     *
     * @return 键为 {@code count}、值为组数量的映射
     */
    @GET
    @Path("count")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Map<String, Long> count();

    /**
     * 按名称搜索统计匹配的组数量。
     *
     * @param search 组名搜索条件
     * @return 键为 {@code count}、值为匹配组数量的映射
     */
    @GET
    @Path("count")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Map<String, Long> count(@QueryParam("search") String search);

    /**
     * 统计顶级组数量。
     *
     * @param onlyTopGroups {@code true} 时仅统计顶级组；{@code false} 时统计所有组
     * @return 键为 {@code count}、值为组数量的映射
     */
    @GET
    @Path("count")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Map<String, Long> count(@QueryParam("top") @DefaultValue("true") boolean onlyTopGroups);

    /**
     * 创建顶级组或添加子组。若组已存在则更新并设置父级；不存在则创建并设置父级。
     *
     * @param rep 组表示对象
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response add(GroupRepresentation rep);

    /** 按 ID 获取单个组资源。 */
    @Path("{id}")
    GroupResource group(@PathParam("id") String id);

    /** 按查询表达式搜索组。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<GroupRepresentation> query(@QueryParam("q") String searchQuery);

    /** 按查询表达式搜索组，可选择是否填充层级结构。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<GroupRepresentation> query(@QueryParam("q") String searchQuery, @QueryParam("populateHierarchy") boolean populateHierarchy);

    /** 按查询表达式搜索组，支持分页与简要表示。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<GroupRepresentation> query(@QueryParam("q") String searchQuery,
            @QueryParam("populateHierarchy") boolean populateHierarchy, @QueryParam("first") Integer first,
            @QueryParam("max") Integer max, @QueryParam("briefRepresentation") boolean briefRepresentation);

}
