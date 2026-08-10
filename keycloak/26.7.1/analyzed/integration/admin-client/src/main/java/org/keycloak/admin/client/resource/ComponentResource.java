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
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.keycloak.representations.idm.ComponentRepresentation;
import org.keycloak.representations.idm.ComponentTypeRepresentation;

/**
 * 单个领域组件（Component）的管理 REST 资源。
 * <p>
 * 组件用于配置用户存储、LDAP 联邦、密钥提供程序等可插拔 SPI 实例，
 * 支持读取、更新、删除及查询可用子组件类型。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ComponentResource {
    /** 获取当前组件的表示对象。 */
    @GET
    ComponentRepresentation toRepresentation();

    /** 更新组件配置。 */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    void update(ComponentRepresentation rep);

    /** 删除当前组件。 */
    @DELETE
    void remove();

    /**
     * 列出指定父组件下可配置的子组件类型。
     *
     * @param subtype 提供程序 Java 类的全限定名，须为当前组件类型的子类型
     * @return 可用的子组件类型列表
     */
    @GET
    @Path("sub-component-types")
    @Produces(MediaType.APPLICATION_JSON)
    List<ComponentTypeRepresentation> getSubcomponentConfig(@QueryParam("type") String subtype);
}
