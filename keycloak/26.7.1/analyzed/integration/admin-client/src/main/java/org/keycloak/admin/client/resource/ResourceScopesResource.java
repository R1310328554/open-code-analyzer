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

import org.keycloak.representations.idm.authorization.ScopeRepresentation;

/**
 * 资源作用域（Scope）集合的管理 REST 资源。
 * <p>
 * 提供创建作用域、按 ID 导航至单个作用域、列出全部作用域及按名称搜索的能力。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface ResourceScopesResource {

    /**
     * 创建新的资源作用域。
     *
     * @param scope 作用域表示对象
     * @return 包含新建作用域信息的 HTTP 响应
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response create(ScopeRepresentation scope);

    /**
     * 按 ID 获取单个作用域子资源。
     *
     * @param id 作用域 ID
     * @return 作用域子资源
     */
    @Path("{id}")
    ResourceScopeResource scope(@PathParam("id") String id);

    /** 列出所有资源作用域。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<ScopeRepresentation> scopes();

    /**
     * 按名称搜索资源作用域。
     *
     * @param name 作用域名称
     * @return 匹配的作用域表示对象
     */
    @Path("/search")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    ScopeRepresentation findByName(@QueryParam("name") String name);
}
