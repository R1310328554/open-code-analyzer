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
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.idm.ProtocolMapperRepresentation;

/**
 * 协议映射器（Protocol Mapper）集合的管理 REST 资源。
 * <p>
 * 用于创建、查询、更新与删除客户端或客户端作用域上的协议映射器配置。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface ProtocolMappersResource {

    /** 按协议类型列出映射器。 */
    @GET
    @Path("protocol/{protocol}")
    @Produces("application/json")
    List<ProtocolMapperRepresentation> getMappersPerProtocol(@PathParam("protocol") String protocol);

    /** 创建单个协议映射器。 */
    @Path("models")
    @POST
    @Consumes("application/json")
    Response createMapper(ProtocolMapperRepresentation rep);

    /** 批量创建协议映射器。 */
    @Path("add-models")
    @POST
    @Consumes("application/json")
    void createMapper(List<ProtocolMapperRepresentation> reps);

    /** 列出全部协议映射器。 */
    @GET
    @Path("models")
    @Produces("application/json")
    List<ProtocolMapperRepresentation> getMappers();

    /** 按 ID 获取协议映射器。 */
    @GET
    @Path("models/{id}")
    @Produces("application/json")
    ProtocolMapperRepresentation getMapperById(@PathParam("id") String id);

    /** 更新指定 ID 的协议映射器。 */
    @PUT
    @Path("models/{id}")
    @Consumes("application/json")
    void update(@PathParam("id") String id, ProtocolMapperRepresentation rep);

    /** 删除指定 ID 的协议映射器。 */
    @DELETE
    @Path("models/{id}")
    void delete(@PathParam("id") String id);
}
