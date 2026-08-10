/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.keycloak.representations.idm.OrganizationRepresentation;

/**
 * 跨组织成员关系查询 REST 资源。
 * <p>
 * 用于按用户 ID 查询其所属的组织列表。
 */
public interface OrganizationsMembersResource {

    /**
     * 返回指定用户 ID 关联的组织列表。
     *
     * @param id 用户 ID
     * @return 用户所属的组织列表
     */
    @Path("{id}/organizations")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<OrganizationRepresentation> getOrganizations(
            @PathParam("id") String id);

    /**
     * 返回指定用户 ID 关联的组织列表，可控制返回字段详略。
     *
     * @param id 用户 ID
     * @param briefRepresentation 为 false 时返回完整表示；否则仅返回基本字段，默认为 true。自 Keycloak 26.3 起支持；旧版服务器默认为 false。
     * @return 用户所属的组织列表
     */
    @Path("{id}/organizations")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<OrganizationRepresentation> getOrganizations(
            @PathParam("id") String id,
            @QueryParam("briefRepresentation") @DefaultValue("true") boolean briefRepresentation);
}
