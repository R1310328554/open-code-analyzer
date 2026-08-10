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

import org.keycloak.representations.idm.authorization.ScopePermissionRepresentation;

/**
 * 作用域权限（Scope Permission）集合的管理 REST 资源。
 * <p>
 * 提供创建作用域权限、按 ID 或名称查询、多条件过滤搜索等能力。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface ScopePermissionsResource {

    /**
     * 创建新的作用域权限。
     *
     * @param representation 作用域权限表示对象
     * @return 包含新建权限信息的 HTTP 响应
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response create(ScopePermissionRepresentation representation);

    /**
     * 按 ID 获取单个作用域权限子资源。
     *
     * @param id 权限 ID
     * @return 作用域权限子资源
     */
    @Path("{id}")
    ScopePermissionResource findById(@PathParam("id") String id);

    /**
     * 按名称搜索作用域权限。
     *
     * @param name 权限名称
     * @param fields 需获取的字段（自 Keycloak 26.7.0 起可用）
     * @return 匹配的作用域权限表示对象
     */
    @Path("/search")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    ScopePermissionRepresentation findByName(@QueryParam("name") String name, @QueryParam("fields") String fields);

    /** 按名称搜索作用域权限（不指定字段过滤）。 */
    default ScopePermissionRepresentation findByName(String name) {
        return findByName(name, null);
    }

    /**
     * 按给定过滤条件搜索作用域权限。
     *
     * @param id 策略 ID
     * @param name 权限名称
     * @param resource 关联资源
     * @param fields 需获取的字段（自 Keycloak 26.7.0 起可用）
     * @param firstResult 分页起始索引
     * @param maxResult 分页最大条数
     * @return 匹配的作用域权限列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<ScopePermissionRepresentation> findAll(@QueryParam("policyId") String id,
                                        @QueryParam("name") String name,
                                        @QueryParam("resource") String resource,
                                        @QueryParam("fields") String fields,
                                        @QueryParam("first") Integer firstResult,
                                        @QueryParam("max") Integer maxResult);

    /** 按给定过滤条件搜索作用域权限（不指定字段过滤）。 */
    default List<ScopePermissionRepresentation> findAll(String id, String name, String resource,
                                        Integer firstResult, Integer maxResult) {
        return findAll(id, name, resource, null, firstResult, maxResult);
    }
}
