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
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.keycloak.representations.idm.ClientScopeRepresentation;

/**
 * 单个客户端作用域（Client Scope）的管理 REST 资源。
 * <p>
 * 客户端作用域定义可复用的 OIDC 声明与角色映射，
 * 可被多个客户端引用以统一令牌内容。
 *
 * @author rodrigo.sasaki@icarros.com.br
 */
public interface ClientScopeResource {

    /** 获取协议映射器（Protocol Mappers）子资源。 */
    @Path("protocol-mappers")
    ProtocolMappersResource getProtocolMappers();

    /** 获取作用域角色映射（Scope Mappings）子资源。 */
    @Path("/scope-mappings")
    RoleMappingResource getScopeMappings();

    /** 获取当前客户端作用域的表示对象。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    ClientScopeRepresentation toRepresentation();

    /** 更新客户端作用域配置。 */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    void update(ClientScopeRepresentation rep);

    /** 删除当前客户端作用域。 */
    @DELETE
    void remove();


}
