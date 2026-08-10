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

import org.keycloak.representations.idm.authorization.ClientPolicyRepresentation;

/**
 * 授权服务中客户端策略（Client Policy）集合的管理 REST 资源。
 * <p>
 * 用于细粒度授权（Fine-Grained Authorization）场景，
 * 基于客户端身份定义访问控制策略。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface ClientPoliciesResource {

    /** 创建新的客户端授权策略。 */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response create(ClientPolicyRepresentation representation);

    /** 按 ID 获取单个客户端授权策略资源。 */
    @Path("{id}")
    ClientPolicyResource findById(@PathParam("id") String id);

    /** 按名称搜索客户端授权策略。 */
    @Path("/search")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    ClientPolicyRepresentation findByName(@QueryParam("name") String name);
}
