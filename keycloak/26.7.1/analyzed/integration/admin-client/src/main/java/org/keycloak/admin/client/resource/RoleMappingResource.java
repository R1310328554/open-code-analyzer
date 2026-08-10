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
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.keycloak.representations.idm.MappingsRepresentation;

/**
 * 用户或组角色映射的管理 REST 资源。
 * <p>
 * 提供查询全部角色映射，以及分别访问领域级和客户端级角色作用域子资源的能力。
 *
 * @author rodrigo.sasaki@icarros.com.br
 */
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface RoleMappingResource {

    /** 获取领域级与客户端级的全部角色映射。 */
    @GET
    MappingsRepresentation getAll();

    /** 获取领域级角色作用域子资源。 */
    @Path("realm")
    RoleScopeResource realmLevel();

    /**
     * 获取指定客户端的角色作用域子资源。
     *
     * @param clientUUID 客户端 UUID
     * @return 客户端级角色作用域子资源
     */
    @Path("clients/{clientUUID}")
    RoleScopeResource clientLevel(@PathParam("clientUUID") String clientUUID);

}
