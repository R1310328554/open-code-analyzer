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

import org.keycloak.representations.idm.authorization.RolePolicyRepresentation;

/**
 * 角色策略（Role Policy）集合的管理 REST 资源。
 * <p>
 * 提供创建角色策略、按 ID 或名称查询，以及导航至单个策略子资源的能力。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface RolePoliciesResource {

    /**
     * 创建新的角色策略。
     *
     * @param representation 角色策略表示对象
     * @return 包含新建策略信息的 HTTP 响应
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response create(RolePolicyRepresentation representation);

    /**
     * 按 ID 获取单个角色策略子资源。
     *
     * @param id 策略 ID
     * @return 角色策略子资源
     */
    @Path("{id}")
    RolePolicyResource findById(@PathParam("id") String id);

    /**
     * 按名称搜索角色策略。
     *
     * @param name 策略名称
     * @return 匹配的角色策略表示对象
     */
    @Path("/search")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    RolePolicyRepresentation findByName(@QueryParam("name") String name);
}
