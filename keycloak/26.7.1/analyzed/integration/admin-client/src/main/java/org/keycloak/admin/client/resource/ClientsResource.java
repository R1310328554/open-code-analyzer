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
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.idm.ClientRepresentation;

/**
 * OAuth/OIDC 客户端集合的管理 REST 资源。
 * <p>
 * 支持创建、查询、删除客户端，并提供多种过滤与分页查询方式。
 *
 * @author rodrigo.sasaki@icarros.com.br
 */
public interface ClientsResource {

    /** 按内部 ID 获取单个客户端资源。 */
    @Path("{id}")
    ClientResource get(@PathParam("id") String id);

    /** 创建新客户端。 */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response create(ClientRepresentation clientRepresentation);

    /** 列出领域内所有客户端。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<ClientRepresentation> findAll();

    /**
     * 列出客户端，可按可见性过滤。
     *
     * @param viewableOnly 为 true 时仅返回当前用户有权查看的客户端
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<ClientRepresentation> findAll(@QueryParam("viewableOnly") boolean viewableOnly);

    /**
     * 分页查询客户端，支持按 clientId 精确匹配或模糊搜索。
     *
     * @param clientId 客户端标识符
     * @param viewableOnly 是否仅返回可见客户端
     * @param search 是否启用模糊搜索模式
     * @param firstResult 分页起始偏移
     * @param maxResults 分页最大条数
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<ClientRepresentation> findAll(@QueryParam("clientId") String clientId,
                                                 @QueryParam("viewableOnly") Boolean viewableOnly,
                                                 @QueryParam("search") Boolean search,
                                                 @QueryParam("first") Integer firstResult,
                                                 @QueryParam("max") Integer maxResults);

    /** 按 clientId 精确查找客户端。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<ClientRepresentation> findByClientId(@QueryParam("clientId") String clientId);

    /** 按通用搜索表达式查询客户端。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<ClientRepresentation> query(@QueryParam("q") String searchQuery);

    /** 删除指定 ID 的客户端。 */
    @Path("{id}")
    @DELETE
    Response delete(@PathParam("id") String id);

}
