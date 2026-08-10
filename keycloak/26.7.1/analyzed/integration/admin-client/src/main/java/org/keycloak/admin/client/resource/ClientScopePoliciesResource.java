/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.idm.authorization.ClientScopePolicyRepresentation;

/**
 * 授权服务中客户端作用域策略（Client Scope Policy）集合的管理 REST 资源。
 * <p>
 * 用于细粒度授权场景，基于 OAuth 客户端作用域定义访问控制策略。
 *
 * @author <a href="mailto:yoshiyuki.tabata.jy@hitachi.com">Yoshiyuki Tabata</a>
 */
public interface ClientScopePoliciesResource {

    /** 创建新的客户端作用域授权策略。 */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response create(ClientScopePolicyRepresentation representation);

    /** 按名称搜索客户端作用域授权策略。 */
    @Path("/search")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    ClientScopePolicyRepresentation findByName(@QueryParam("name") String name);
}
