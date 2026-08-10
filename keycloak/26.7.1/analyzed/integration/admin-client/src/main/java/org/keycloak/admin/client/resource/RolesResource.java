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
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.keycloak.representations.idm.RoleRepresentation;

/**
 * 领域角色集合的管理 REST 资源。
 * <p>
 * 提供角色列表查询（支持搜索与分页）、创建角色、
 * 按名称导航至单个角色子资源及删除角色等能力。
 *
 * @author rodrigo.sasaki@icarros.com.br
 */
public interface RolesResource {

    /** 列出全部领域角色。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<RoleRepresentation> list();

    /**
     * 列出全部领域角色。
     *
     * @param briefRepresentation 若为 false，则返回包含属性的完整角色信息
     * @return 全部角色列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<RoleRepresentation> list(@QueryParam("briefRepresentation") @DefaultValue("true") boolean briefRepresentation);

    /**
     * 分页列出领域角色。
     *
     * @param search 搜索关键字
     * @param firstResult 分页起始索引
     * @param maxResults 分页最大条数
     * @return 角色分页切片
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<RoleRepresentation> list(@QueryParam("first") Integer firstResult,
                                  @QueryParam("max") Integer maxResults);

    /**
     * 分页列出领域角色。
     *
     * @param firstResult 分页起始索引
     * @param maxResults 分页最大条数
     * @param briefRepresentation 若为 false，则返回包含属性的完整角色信息
     * @return 角色分页切片
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<RoleRepresentation> list(@QueryParam("first") Integer firstResult,
                                  @QueryParam("max") Integer maxResults,
                                  @QueryParam("briefRepresentation") @DefaultValue("true") boolean briefRepresentation);

    /**
     * 按搜索条件列出领域角色。
     *
     * @param search 搜索关键字
     * @param briefRepresentation 若为 false，则返回包含属性的完整角色信息
     * @return 匹配的角色列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<RoleRepresentation> list(@QueryParam("search") @DefaultValue("") String search,
                                  @QueryParam("briefRepresentation") @DefaultValue("true") boolean briefRepresentation);

    /**
     * 按搜索条件分页列出领域角色。
     *
     * @param search 搜索关键字
     * @param firstResult 分页起始索引
     * @param maxResults 分页最大条数
     * @return 匹配的角色分页切片
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<RoleRepresentation> list(@QueryParam("search") @DefaultValue("") String search,
                                  @QueryParam("first") Integer firstResult,
                                  @QueryParam("max") Integer maxResults);

    /**
     * 按搜索条件分页列出领域角色。
     *
     * @param search 搜索关键字
     * @param firstResult 分页起始索引
     * @param maxResults 分页最大条数
     * @param briefRepresentation 若为 false，则返回包含属性的完整角色信息
     * @return 匹配的角色分页切片
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<RoleRepresentation> list(@QueryParam("search") @DefaultValue("") String search,
                                  @QueryParam("first") Integer firstResult,
                                  @QueryParam("max") Integer maxResults,
                                  @QueryParam("briefRepresentation") @DefaultValue("true") boolean briefRepresentation);

    /**
     * 创建新的领域角色。
     *
     * @param roleRepresentation 角色表示对象
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    void create(RoleRepresentation roleRepresentation);

    /**
     * 按角色名称获取单个角色子资源。
     *
     * @param roleName 角色名称
     * @return 角色子资源
     */
    @Path("{roleName}")
    RoleResource get(@PathParam("roleName") String roleName);

    /**
     * 按角色名称删除角色。
     *
     * @param roleName 角色名称
     */
    @Path("{role-name}")
    @DELETE
    void deleteRole(final @PathParam("role-name") String roleName);

}
