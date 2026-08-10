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
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.keycloak.representations.idm.RoleRepresentation;

/**
 * 角色作用域（Role Scope）的管理 REST 资源。
 * <p>
 * 用于管理用户或组在特定作用域（领域级或客户端级）内的角色分配，
 * 支持列出已分配、可用及有效（含组合继承）角色，并执行增删操作。
 *
 * @author rodrigo.sasaki@icarros.com.br
 */
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface RoleScopeResource {

    /** 列出当前作用域内已分配的全部角色。 */
    @GET
    List<RoleRepresentation> listAll();

    /** 列出当前作用域内可分配但尚未分配的角色。 */
    @GET
    @Path("available")
    List<RoleRepresentation> listAvailable();

    /** 列出当前作用域内的有效角色（含组合角色继承）。 */
    @GET
    @Path("composite")
    List<RoleRepresentation> listEffective();

    /**
     * 列出当前作用域内的有效角色（含组合角色继承）。
     *
     * @param briefRepresentation 是否以简要形式返回角色
     * @return 有效角色列表
     */
    @GET
    @Path("composite")
    List<RoleRepresentation> listEffective(@QueryParam("briefRepresentation") @DefaultValue("true") boolean briefRepresentation);

    /**
     * 向当前作用域添加角色。
     *
     * @param rolesToAdd 待添加的角色列表
     */
    @POST
    void add(List<RoleRepresentation> rolesToAdd);

    /**
     * 从当前作用域移除角色。
     *
     * @param rolesToRemove 待移除的角色列表
     */
    @DELETE
    void remove(List<RoleRepresentation> rolesToRemove);

}
