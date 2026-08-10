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

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.idm.MemberRepresentation;
import org.keycloak.representations.idm.MembershipType;
import org.keycloak.representations.idm.OrganizationRepresentation;

/**
 * 组织成员集合的管理 REST 资源。
 * <p>
 * 支持添加/移除成员、分页列出成员、按条件搜索、邀请用户及统计成员数量。
 */
public interface OrganizationMembersResource {

    /** 将已有用户添加为组织成员。 */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response addMember(String userId);

    /** 按成员 ID 从组织中移除成员。 */
    @Path("{member-id}")
    @DELETE
    Response removeMember(@PathParam("member-id") String memberId);

    /**
     * 返回组织内的全部成员。
     *
     * @return 组织成员列表；默认返回简要用户表示
     * @Deprecated 请改用 {@link org.keycloak.admin.client.resource.OrganizationMembersResource#list}。
     */
    @Deprecated
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<MemberRepresentation> getAll();

    /**
     * 分页返回组织成员。
     *
     * @param first 首个元素索引（分页偏移）
     * @param max 最大返回数量
     * @return 组织成员列表；默认返回简要用户表示。可通过 {@link #list(Integer, Integer, boolean)} 控制详略。
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<MemberRepresentation> list(
            @QueryParam("first") Integer firstResult,
            @QueryParam("max") Integer maxResults
    );

    /**
     * 分页返回组织成员，可控制返回字段详略。
     *
     * @param first 首个元素索引（分页偏移）
     * @param max 最大返回数量
     * @param briefRepresentation 为 false 时返回完整表示；否则仅返回基本字段，默认为 true。自 Keycloak server 26.7.0 起可用
     * @return 组织成员列表
     * @since Keycloak server 26.7
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<MemberRepresentation> list(
            @QueryParam("first") Integer firstResult,
            @QueryParam("max") Integer maxResults,
            @QueryParam("briefRepresentation") @DefaultValue("true") boolean briefRepresentation
    );

    /**
     * 按搜索条件返回匹配的组织成员。
     *
     * @param search 用户名、邮箱、名或姓的搜索文本
     * @param exact 为 true 时要求至少一个主要属性与 search 精确匹配；为 false 时部分匹配即可
     * @param first 首个元素索引（分页偏移）
     * @param max 最大返回数量
     * @return 匹配的组织成员列表；默认返回简要用户表示。可通过 {@link #search(String, Boolean, Integer, Integer, boolean)} 控制详略。
     * @since Keycloak server 26.7.0
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<MemberRepresentation> search(
            @QueryParam("search") String search,
            @QueryParam("exact") Boolean exact,
            @QueryParam("first") Integer first,
            @QueryParam("max") Integer max
    );

    /**
     * 按搜索条件返回匹配的组织成员，可控制返回字段详略。
     *
     * @param search 用户名、邮箱、名或姓的搜索文本
     * @param exact 为 true 时要求至少一个主要属性与 search 精确匹配；为 false 时部分匹配即可
     * @param first 首个元素索引（分页偏移）
     * @param max 最大返回数量
     * @param briefRepresentation 为 false 时返回完整表示；否则仅返回基本字段，默认为 true。自 Keycloak server 26.7.0 起可用
     * @return 匹配的组织成员列表
     * @since Keycloak server 26.7
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<MemberRepresentation> search(
            @QueryParam("search") String search,
            @QueryParam("exact") Boolean exact,
            @QueryParam("first") Integer first,
            @QueryParam("max") Integer max,
            @QueryParam("briefRepresentation") @DefaultValue("true") boolean briefRepresentation
    );

    /**
     * 按搜索条件与成员类型返回匹配的组织成员。
     *
     * @param search 用户名、邮箱、名或姓的搜索文本
     * @param exact 为 true 时要求至少一个主要属性与 search 精确匹配；为 false 时部分匹配即可
     * @param membershipType {@link org.keycloak.representations.idm.MembershipType}；自 Keycloak 26.1 起支持
     * @param first 首个元素索引（分页偏移）
     * @param max 最大返回数量
     * @return 匹配的组织成员列表；默认返回简要用户表示。可通过 {@link #search(String, Boolean, MembershipType, Integer, Integer, boolean)} 控制详略。
     * @since Keycloak 26.1. 旧版服务器请使用 {@link #search(String, Boolean, Integer, Integer)}
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<MemberRepresentation> search(
            @QueryParam("search") String search,
            @QueryParam("exact") Boolean exact,
            @QueryParam("membershipType") MembershipType membershipType,
            @QueryParam("first") Integer first,
            @QueryParam("max") Integer max
    );

    /**
     * 按搜索条件与成员类型返回匹配的组织成员，可控制返回字段详略。
     *
     * @param search 用户名、邮箱、名或姓的搜索文本
     * @param exact 为 true 时要求至少一个主要属性与 search 精确匹配；为 false 时部分匹配即可
     * @param membershipType {@link org.keycloak.representations.idm.MembershipType}；自 Keycloak 26.1 起支持
     * @param first 首个元素索引（分页偏移）
     * @param max 最大返回数量
     * @param briefRepresentation 为 false 时返回完整表示；否则仅返回基本字段，默认为 true
     * @return 匹配的组织成员列表
     * @since Keycloak server 26.7
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<MemberRepresentation> search(
            @QueryParam("search") String search,
            @QueryParam("exact") Boolean exact,
            @QueryParam("membershipType") MembershipType membershipType,
            @QueryParam("first") Integer first,
            @QueryParam("max") Integer max,
            @QueryParam("briefRepresentation") @DefaultValue("true") boolean briefRepresentation
    );

    /** 按成员 ID 获取单个成员管理资源。 */
    @Path("{id}")
    OrganizationMemberResource member(@PathParam("id") String id);

    /** 通过邮箱邀请新用户加入组织。 */
    @POST
    @Path("invite-user")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    Response inviteUser(@FormParam("email") String email,
                        @FormParam("firstName") String firstName,
                        @FormParam("lastName") String lastName);

    /** 邀请已有用户加入组织。 */
    @POST
    @Path("invite-existing-user")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    Response inviteExistingUser(@FormParam("id") String id);

    /**
     * 统计组织成员数量。
     *
     * @since Keycloak server 26
     * @return 组织成员总数
     */
    @Path("count")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Long count();

    /** 查询指定成员所属的组织列表。 */
    @Path("{id}/organizations")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<OrganizationRepresentation> getOrganizations(
            @PathParam("id") String id,
            @QueryParam("briefRepresentation") @DefaultValue("true") boolean briefRepresentation);
}
