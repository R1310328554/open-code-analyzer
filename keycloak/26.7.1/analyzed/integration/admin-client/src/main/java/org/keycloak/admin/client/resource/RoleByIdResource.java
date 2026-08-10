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
import java.util.Set;

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

import org.keycloak.representations.idm.RoleRepresentation;

/**
 * 按角色 ID 管理角色的 REST 资源。
 * <p>
 * 有时直接通过角色 ID 操作比通过容器/角色名路径更为便捷。
 * 提供角色 CRUD 及组合角色（Composite）的增删查能力。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface RoleByIdResource {

    /**
     * 按 ID 获取角色表示对象。
     *
     * @param id 角色 ID
     * @return 角色表示对象
     */
    @Path("{role-id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    RoleRepresentation getRole(final @PathParam("role-id") String id);

    /**
     * 按 ID 删除角色。
     *
     * @param id 角色 ID
     */
    @Path("{role-id}")
    @DELETE
    void deleteRole(final @PathParam("role-id") String id);

    /**
     * 按 ID 更新角色。
     *
     * @param id 角色 ID
     * @param rep 角色表示对象
     */
    @Path("{role-id}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    void updateRole(final @PathParam("role-id") String id, RoleRepresentation rep);

    /**
     * 为角色添加组合角色。
     *
     * @param id 角色 ID
     * @param roles 待添加的组合角色列表
     */
    @Path("{role-id}/composites")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    void addComposites(final @PathParam("role-id") String id, List<RoleRepresentation> roles);

    /**
     * 获取角色的全部组合角色。
     *
     * @param id 角色 ID
     * @return 组合角色集合
     */
    @Path("{role-id}/composites")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Set<RoleRepresentation> getRoleComposites(@PathParam("role-id") String id);

    /**
     * 搜索并分页获取角色的组合角色。
     *
     * @param id 角色 ID
     * @param search 搜索关键字
     * @param first 分页起始索引
     * @param max 分页最大条数
     * @return 匹配的组合角色集合
     */
    @Path("{role-id}/composites")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Set<RoleRepresentation> searchRoleComposites(@PathParam("role-id") String id,
                                                 @QueryParam("search") String search,
                                                 @QueryParam("first") Integer first,
                                                 @QueryParam("max") Integer max);

    /**
     * 获取角色的领域级组合角色。
     *
     * @param id 角色 ID
     * @return 领域级组合角色集合
     */
    @Path("{role-id}/composites/realm")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Set<RoleRepresentation> getRealmRoleComposites(@PathParam("role-id") String id);

    /**
     * 获取角色在指定客户端下的组合角色。
     *
     * @param id 角色 ID
     * @param clientUuid 客户端 UUID
     * @return 客户端级组合角色集合
     */
    @Path("{role-id}/composites/clients/{clientUuid}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Set<RoleRepresentation> getClientRoleComposites(@PathParam("role-id") String id, @PathParam("clientUuid") String clientUuid);

    /**
     * 从角色中移除组合角色。
     *
     * @param id 角色 ID
     * @param roles 待移除的组合角色列表
     */
    @Path("{role-id}/composites")
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    void deleteComposites(final @PathParam("role-id") String id, List<RoleRepresentation> roles);

}
