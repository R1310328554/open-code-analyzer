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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.idm.ClientScopeRepresentation;

/**
 * 客户端作用域（Client Scope）集合的管理 REST 资源。
 * <p>
 * 支持列出、创建客户端作用域，并按 ID 访问单个作用域的详细管理接口。
 *
 * @author rodrigo.sasaki@icarros.com.br
 */
public interface ClientScopesResource {

    /** 按 ID 获取单个客户端作用域资源。 */
    @Path("{id}")
    ClientScopeResource get(@PathParam("id") String id);

    /** 创建新的客户端作用域。 */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response create(ClientScopeRepresentation clientScopeRepresentation);

    /** 列出领域内所有客户端作用域。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<ClientScopeRepresentation> findAll();

}
