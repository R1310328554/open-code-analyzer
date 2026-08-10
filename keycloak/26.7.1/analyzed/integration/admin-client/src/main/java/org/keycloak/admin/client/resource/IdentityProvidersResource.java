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
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.idm.IdentityProviderRepresentation;

/**
 * 领域内身份提供程序（Identity Provider）集合的管理 REST 资源。
 * <p>
 * 支持 IdP 实例的创建、查询、按别名访问及配置导入等操作。
 *
 * @author pedroigor
 */
public interface IdentityProvidersResource {

    /** 按别名获取单个身份提供程序资源。 */
    @Path("instances/{alias}")
    IdentityProviderResource get(@PathParam("alias") String alias);

    /** 列出所有身份提供程序实例。 */
    @GET
    @Path("instances")
    @Produces(MediaType.APPLICATION_JSON)
    List<IdentityProviderRepresentation> findAll();

    /**
     * 按指定参数分页查询身份提供程序列表。
     *
     * @param type IdP 类型，参见 {@code org.keycloak.models.IdentityProviderType}；
     *             省略时不按类型过滤。自 Keycloak 26.5.0 起可用
     * @param capability IdP 能力，参见 {@code org.keycloak.models.IdentityProviderCapability}；
     *                   仅在未指定 type 时生效；省略时不按能力过滤。自 Keycloak 26.5.0 起可用
     * @param search 按名称搜索，支持前缀（name*）、包含（*name*）或精确（"name"）匹配，默认前缀
     * @param briefRepresentation 若为 true，仅返回 ID、alias、providerId 及 enabled 等基本信息
     * @param firstResult 分页偏移量
     * @param maxResults 最大返回数量（默认 100）
     * @return 匹配的身份提供程序列表
     */
    @GET
    @Path("instances")
    @Produces(MediaType.APPLICATION_JSON)
    List<IdentityProviderRepresentation> find(@QueryParam("type") String type, @QueryParam("capability") String capability,
                                              @QueryParam("search") String search, @QueryParam("briefRepresentation") Boolean briefRepresentation,
                                              @QueryParam("first") Integer firstResult, @QueryParam("max") Integer maxResults);

    /** 按名称搜索并分页查询身份提供程序（不含 type/capability 过滤）。 */
    @GET
    @Path("instances")
    @Produces(MediaType.APPLICATION_JSON)
    List<IdentityProviderRepresentation> find(@QueryParam("search") String search, @QueryParam("briefRepresentation") Boolean briefRepresentation,
                                              @QueryParam("first") Integer firstResult, @QueryParam("max") Integer maxResults);

    /**
     * 按指定参数分页查询身份提供程序列表，支持仅返回领域级 IdP。
     *
     * @param search 按名称搜索，支持前缀、包含或精确匹配
     * @param briefRepresentation 若为 true，仅返回基本信息
     * @param firstResult 分页偏移量
     * @param maxResults 最大返回数量（默认 100）
     * @param realmOnly 若为 true，仅返回领域级 IdP（未关联组织的 IdP）。
     *                  自 Keycloak 26 起可用；旧版本忽略此参数
     * @return 匹配的身份提供程序列表
     */
    @GET
    @Path("instances")
    @Produces(MediaType.APPLICATION_JSON)
    List<IdentityProviderRepresentation> find(@QueryParam("search") String search, @QueryParam("briefRepresentation") Boolean briefRepresentation,
                                              @QueryParam("first") Integer firstResult, @QueryParam("max") Integer maxResults,
                                              @QueryParam("realmOnly") Boolean realmOnly);

    /** 创建新的身份提供程序实例。 */
    @POST
    @Path("instances")
    @Consumes(MediaType.APPLICATION_JSON)
    Response create(IdentityProviderRepresentation identityProvider);

    /** 按提供程序 ID 获取 IdP 工厂/配置信息。 */
    @GET
    @Path("/providers/{provider_id}")
    @Produces(MediaType.APPLICATION_JSON)
    Response getIdentityProviders(@PathParam("provider_id") String providerId);

    /** 从 multipart 表单数据导入 IdP 配置。 */
    @POST
    @Path("import-config")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    Map<String, String> importFrom(Object data);

    /** 从 JSON 映射导入 IdP 配置。 */
    @POST
    @Path("import-config")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Map<String, String> importFrom(Map<String, Object> data);
}
