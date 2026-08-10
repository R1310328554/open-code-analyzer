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
import java.util.Map;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.idm.IdentityProviderMapperRepresentation;
import org.keycloak.representations.idm.IdentityProviderMapperTypeRepresentation;
import org.keycloak.representations.idm.IdentityProviderRepresentation;

/**
 * 单个身份提供程序（Identity Provider）的管理 REST 资源。
 * <p>
 * 支持 IdP CRUD、配置导出、映射器管理及密钥重载等操作。
 *
 * @author pedroigor
 */
public interface IdentityProviderResource {

    /** 获取当前身份提供程序的表示对象。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    IdentityProviderRepresentation toRepresentation();

    /** 更新身份提供程序配置。 */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    void update(IdentityProviderRepresentation identityProviderRepresentation);

    /** 删除当前身份提供程序。 */
    @DELETE
    void remove();

    /**
     * 导出身份提供程序配置。
     *
     * @param format 导出格式
     */
    @GET
    @Path("export")
    Response export(@QueryParam("format") String format);

    /** 获取可用的映射器类型及其元数据。 */
    @GET
    @Path("mapper-types")
    @Produces(MediaType.APPLICATION_JSON)
    Map<String, IdentityProviderMapperTypeRepresentation> getMapperTypes();

    /** 列出当前 IdP 的所有属性/角色映射器。 */
    @GET
    @Path("mappers")
    @Produces(MediaType.APPLICATION_JSON)
    List<IdentityProviderMapperRepresentation> getMappers();

    /** 为当前 IdP 添加新的映射器。 */
    @POST
    @Path("mappers")
    @Consumes(MediaType.APPLICATION_JSON)
    Response addMapper(IdentityProviderMapperRepresentation mapper);

    /** 按 ID 获取映射器。 */
    @GET
    @Path("mappers/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    IdentityProviderMapperRepresentation getMapperById(@PathParam("id") String id);

    /** 更新指定 ID 的映射器。 */
    @PUT
    @Path("mappers/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    void update(@PathParam("id") String id, IdentityProviderMapperRepresentation rep);

    /** 删除指定 ID 的映射器。 */
    @DELETE
    @Path("mappers/{id}")
    void delete(@PathParam("id") String id);

    /** 重新加载 IdP 的签名/加密密钥，返回是否成功。 */
    @GET
    @Path("reload-keys")
    boolean reloadKeys();
}
