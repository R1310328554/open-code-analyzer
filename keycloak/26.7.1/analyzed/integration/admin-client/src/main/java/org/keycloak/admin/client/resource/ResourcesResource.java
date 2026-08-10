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
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.idm.authorization.ResourceRepresentation;

/**
 * 授权资源（Resource）集合的管理 REST 资源。
 * <p>
 * 提供创建资源、按 ID 导航、多条件过滤查询及按名称搜索等能力。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface ResourcesResource {

    /**
     * 创建新的授权资源。
     *
     * @param resource 资源表示对象
     * @return 包含新建资源信息的 HTTP 响应
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response create(ResourceRepresentation resource);

    /**
     * 按 ID 获取单个资源子资源。
     *
     * @param id 资源 ID
     * @return 资源子资源
     */
    @Path("{id}")
    ResourceResource resource(@PathParam("id") String id);

    /**
     * 按多种条件过滤并分页查询资源。
     *
     * @param name 资源名称
     * @param uri 资源 URI
     * @param owner 资源所有者
     * @param type 资源类型
     * @param scope 关联作用域
     * @param firstResult 分页起始索引
     * @param maxResult 分页最大条数
     * @return 匹配的资源列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<ResourceRepresentation> find(@QueryParam("name") String name,
                  @QueryParam("uri") String uri,
                  @QueryParam("owner") String owner,
                  @QueryParam("type") String type,
                  @QueryParam("scope") String scope,
                  @QueryParam("first") Integer firstResult,
                  @QueryParam("max") Integer maxResult);

    /**
     * 按名称精确搜索单个资源。
     *
     * @param name 资源名称
     * @return 匹配的资源表示对象
     */
    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    ResourceRepresentation searchByName(@QueryParam("name") String name);

    /**
     * 按名称查找资源列表。
     *
     * @param name 资源名称
     * @return 匹配的资源列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<ResourceRepresentation> findByName(@QueryParam("name") String name);

    /**
     * 按名称及所有者查找资源列表。
     *
     * @param name 资源名称
     * @param owner 资源所有者
     * @return 匹配的资源列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<ResourceRepresentation> findByName(@QueryParam("name") String name, @QueryParam("owner") String owner);

    /** 列出所有授权资源。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<ResourceRepresentation> resources();
}
