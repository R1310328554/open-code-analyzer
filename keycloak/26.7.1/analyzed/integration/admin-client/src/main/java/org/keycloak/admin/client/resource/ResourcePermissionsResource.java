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

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.idm.authorization.ResourcePermissionRepresentation;

/**
 * 资源权限（Resource Permission）集合的管理 REST 资源。
 * <p>
 * 提供创建资源权限、按 ID 或名称查询，以及导航至单个权限子资源的能力。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface ResourcePermissionsResource {

    /**
     * 创建新的资源权限。
     *
     * @param representation 资源权限表示对象
     * @return 包含新建权限信息的 HTTP 响应
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response create(ResourcePermissionRepresentation representation);

    /**
     * 按 ID 获取单个资源权限子资源。
     *
     * @param id 资源权限 ID
     * @return 资源权限子资源
     */
    @Path("{id}")
    ResourcePermissionResource findById(@PathParam("id") String id);

    /**
     * 按名称搜索资源权限。
     *
     * @param name 权限名称
     * @return 匹配的资源权限表示对象
     */
    @Path("/search")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    ResourcePermissionRepresentation findByName(@QueryParam("name") String name);
}
