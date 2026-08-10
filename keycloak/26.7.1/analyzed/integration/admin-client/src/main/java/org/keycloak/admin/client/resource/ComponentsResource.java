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
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.idm.ComponentRepresentation;

/**
 * 领域组件（Component）集合的管理 REST 资源。
 * <p>
 * 组件用于配置用户存储、LDAP 联邦、密钥提供程序等可插拔 SPI 实例；
 * 支持按父级、类型、名称查询，以及创建与删除操作。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ComponentsResource {
    /** 列出领域内所有组件。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<ComponentRepresentation> query();

    /** 按父级 ID 过滤并列出组件。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<ComponentRepresentation> query(@QueryParam("parent") String parent);

    /** 按父级 ID 与组件类型过滤并列出组件。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<ComponentRepresentation> query(@QueryParam("parent") String parent, @QueryParam("type") String type);

    /** 按父级、类型与名称精确过滤并列出组件。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<ComponentRepresentation> query(@QueryParam("parent") String parent,
                                        @QueryParam("type") String type,
                                        @QueryParam("name") String name);

    /** 创建新组件。 */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response add(ComponentRepresentation rep);

    /** 按 ID 获取单个组件资源。 */
    @Path("{id}")
    ComponentResource component(@PathParam("id") String id);

    /** 删除指定 ID 的组件。 */
    @Path("{id}")
    @DELETE
    ComponentResource removeComponent(@PathParam("id") String id);


}
