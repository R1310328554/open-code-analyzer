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

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.MemberRepresentation;
import org.keycloak.representations.idm.OrganizationRepresentation;

/**
 * 单个组织成员的管理 REST 资源。
 * <p>
 * 支持查询成员详情、移除成员，以及查看成员所属组织与组织内组。
 */
public interface OrganizationMemberResource {

    /** 获取当前成员的表示对象。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    MemberRepresentation toRepresentation();

    /** 从组织中移除当前成员。 */
    @DELETE
    Response delete();

    /**
     * 返回该用户关联的组织列表。
     *
     * @since Keycloak server 26
     * @return 用户所属的组织列表
     */
    @Path("organizations")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<OrganizationRepresentation> getOrganizations();

    /**
     * 返回该用户关联的组织列表，可控制返回字段详略。
     *
     * @param briefRepresentation 为 false 时返回完整表示；否则仅返回基本字段，默认为 true。自 Keycloak 26.3 起支持；旧版服务器默认为 false。
     * @since Keycloak server 26
     * @return 用户所属的组织列表
     */
    @Path("organizations")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<OrganizationRepresentation> getOrganizations(
            @QueryParam("briefRepresentation") @DefaultValue("true") boolean briefRepresentation);

    /**
     * 返回该成员在组织内的组归属关系。
     *
     * @param firstResult 分页起始位置
     * @param maxResults 最大返回数量
     * @param briefRepresentation 为 false 时返回完整表示；否则仅返回基本字段，默认为 true
     * @since Keycloak server 26.6.0
     * @return 成员所属的组织内组列表
     */
    @Path("groups")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<GroupRepresentation> groups(
            @QueryParam("first") Integer firstResult,
            @QueryParam("max") Integer maxResults,
            @QueryParam("briefRepresentation") @DefaultValue("true") boolean briefRepresentation);

    /**
     * 返回该成员在组织内的组归属关系，支持按名称搜索。
     *
     * @param firstResult 分页起始位置
     * @param maxResults 最大返回数量
     * @param search 按组名进行不区分大小写的搜索；为 null 或空白时不启用过滤
     * @param briefRepresentation 为 false 时返回完整表示；否则仅返回基本字段，默认为 true
     * @since Keycloak server 26.6.0
     * @return 成员所属的组织内组列表
     */
    @Path("groups")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<GroupRepresentation> groups(
            @QueryParam("first") Integer firstResult,
            @QueryParam("max") Integer maxResults,
            @QueryParam("search") String search,
            @QueryParam("briefRepresentation") @DefaultValue("true") boolean briefRepresentation);
}
