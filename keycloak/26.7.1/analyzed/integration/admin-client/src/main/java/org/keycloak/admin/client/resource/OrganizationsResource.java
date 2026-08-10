/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

import org.keycloak.representations.idm.OrganizationRepresentation;

/**
 * 领域内组织集合的管理 REST 资源。
 * <p>
 * 支持创建、查询、搜索与统计组织，并提供跨组织成员关系子资源。
 * 本端点及其所有子端点自 Keycloak 25 起可用，且需启用特性 {@link org.keycloak.common.Profile.Feature#ORGANIZATION}。
 *
 * @since Keycloak 25. All the child endpoints are also available since that version<p>
 *
 * This endpoint including all the child endpoints requires feature {@link org.keycloak.common.Profile.Feature#ORGANIZATION} to be enabled<p>
 */
public interface OrganizationsResource {

    /** 创建新组织。 */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response create(OrganizationRepresentation organization);

    /** 按 ID 获取单个组织管理资源。 */
    @Path("{id}")
    OrganizationResource get(@PathParam("id") String id);

    /**
     * 返回领域内的全部组织。
     *
     * @return 组织列表
     * @Deprecated 请改用 {@link org.keycloak.admin.client.resource.OrganizationsResource#list}。
     */
    @Deprecated
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<OrganizationRepresentation> getAll();

    /**
     * 分页返回领域内的组织。
     *
     * @param firstResult 首个元素索引（分页偏移）
     * @param maxResults 最大返回数量
     * @return 组织列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<OrganizationRepresentation> list(
            @QueryParam("first") Integer firstResult,
            @QueryParam("max") Integer maxResults
    );

    /**
     * 按名称或域名搜索组织。
     *
     * @param search 组织名称或域名的搜索文本
     * @return 匹配的组织列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<OrganizationRepresentation> search(@QueryParam("search") String search);

    /**
     * 按多种条件搜索组织。
     *
     * @param search 组织名称或域名的搜索文本
     * @param exact 为 true 时名称或域名须与 search 精确匹配；为 false 时部分匹配即可
     * @param first 分页起始位置；为负或 null 时忽略
     * @param max 最大返回数量；为负或 null 时忽略
     * @return 匹配的组织列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<OrganizationRepresentation> search(
            @QueryParam("search") String search,
            @QueryParam("exact") Boolean exact,
            @QueryParam("first") Integer first,
            @QueryParam("max") Integer max
    );

    /**
     * 按多种条件搜索组织，可控制返回字段详略。
     *
     * @param search 组织名称或域名的搜索文本
     * @param exact 为 true 时名称或域名须与 search 精确匹配；为 false 时部分匹配即可
     * @param first 分页起始位置；为负或 null 时忽略
     * @param max 最大返回数量；为负或 null 时忽略
     * @param briefRepresentation 为 false 时返回完整表示；否则仅返回基本字段。自 Keycloak 26.1 起支持
     * @return 匹配的组织列表
     * @since Keycloak 26.1. 旧版服务器请使用 {@link #search(String, Boolean, Integer, Integer)}
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<OrganizationRepresentation> search(
            @QueryParam("search") String search,
            @QueryParam("exact") Boolean exact,
            @QueryParam("first") Integer first,
            @QueryParam("max") Integer max,
            @QueryParam("briefRepresentation") Boolean briefRepresentation
    );

    /**
     * 按自定义属性查询组织。
     *
     * @param searchQuery 属性查询表达式，格式为 {@code key1:value1 key2:value2}
     * @return 匹配属性条件的组织列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<OrganizationRepresentation> searchByAttribute(
            @QueryParam("q") String searchQuery
    );

    /**
     * 按自定义属性查询组织，支持分页。
     *
     * @param searchQuery 属性查询表达式，格式为 {@code key1:value1 key2:value2}
     * @param first 分页起始位置；为负或 null 时忽略
     * @param max 最大返回数量；为负或 null 时忽略
     * @return 匹配属性条件的组织列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<OrganizationRepresentation> searchByAttribute(
            @QueryParam("q") String searchQuery,
            @QueryParam("first") Integer first,
            @QueryParam("max") Integer max
    );

    /**
     * 按搜索条件统计组织数量。
     *
     * @param search 搜索文本
     * @return 匹配搜索条件的组织数量
     * @since Keycloak 26.3
     */
    @GET
    @Path("count")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    long count(@QueryParam("search") String search);

    /**
     * 按搜索条件统计组织数量，支持精确匹配。
     *
     * @param search 搜索文本
     * @param exact 为 true 时名称或域名须与 search 精确匹配；为 false 时部分匹配即可
     * @return 匹配搜索条件的组织数量
     * @since Keycloak 26.3
     */
    @GET
    @Path("count")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    long count(
            @QueryParam("search") String search,
            @QueryParam("exact") Boolean exact
    );

    /**
     * 按自定义属性统计组织数量。
     *
     * @param searchQuery 属性查询表达式，格式为 {@code key1:value1 key2:value2}
     * @return 匹配属性条件的组织数量
     * @since Keycloak 26.3
     */
    @GET
    @Path("count")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    long countByAttribute(
            @QueryParam("q") String searchQuery
    );


    /** 获取跨组织成员关系管理子资源。 */
    @Path("members")
    OrganizationsMembersResource members();
}
