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
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.idm.OrganizationInvitationRepresentation;

/**
 * 组织邀请的管理 REST 资源。
 * <p>
 * 支持列出、查询、删除邀请以及重新发送邀请邮件。
 *
 * @since Keycloak server 26.5.0
 */
public interface OrganizationInvitationsResource {

    /**
     * 返回组织内的全部邀请。
     *
     * @return 包含所有组织邀请的列表
     */
    default List<OrganizationInvitationRepresentation> list() {
        return list(null, null, null, null, null, null, null);
    }

    /**
     * 分页返回组织内的邀请。
     *
     * @param first 首个元素索引（分页偏移）；为 null 时从 0 开始
     * @param max 最大返回数量；为 null 时返回全部结果
     * @return 组织邀请列表
     */
    default List<OrganizationInvitationRepresentation> list(Integer first, Integer max) {
        return list(null, null, null, null, null, first, max);
    }

    /**
     * 按状态等条件过滤并分页返回组织邀请。
     *
     * @param first 首个元素索引（分页偏移）；为 null 时从 0 开始
     * @param max 最大返回数量；为 null 时返回全部结果
     * @param status 按邀请状态过滤（PENDING、EXPIRED）；为 null 时返回所有状态
     * @param email 按邮箱精确匹配过滤；为 null 时不按邮箱过滤
     * @return 符合条件的组织邀请列表
     */
    default List<OrganizationInvitationRepresentation> list(String status, String email, Integer first, Integer max) {
        return list(status, email, null, null, null, first, max);
    }

    /**
     * 按多种条件查询组织邀请。
     *
     * @param first 首个元素索引（分页偏移）；为 null 时从 0 开始
     * @param max 最大返回数量；为 null 时返回全部结果
     * @param status 按邀请状态过滤（PENDING、EXPIRED）；为 null 时返回所有状态
     * @param email 按邮箱精确匹配过滤；为 null 时不按邮箱过滤
     * @param search 在邮箱、名、姓字段上全文搜索；为 null 时不启用搜索
     * @param firstName 按名精确匹配；为 null 时不按名过滤
     * @param lastName 按姓精确匹配；为 null 时不按姓过滤
     * @return 符合条件的组织邀请列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<OrganizationInvitationRepresentation> list(
            @QueryParam("status") String status,
            @QueryParam("email") String email,
            @QueryParam("search") String search,
            @QueryParam("firstName") String firstName,
            @QueryParam("lastName") String lastName,
            @QueryParam("first") Integer first,
            @QueryParam("max") Integer max);

    /**
     * 按 ID 获取邀请详情。
     *
     * @param id 邀请 ID
     * @return 邀请表示对象
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    OrganizationInvitationRepresentation get(@PathParam("id") String id);

    /**
     * 永久删除邀请，此操作不可撤销。
     *
     * @param id 邀请 ID
     * @return 表示成功或失败的响应
     */
    @DELETE
    @Path("/{id}")
    Response delete(@PathParam("id") String id);

    /**
     * 重新发送邀请邮件。
     * <p>
     * 将生成新的邀请令牌并刷新过期时间。
     *
     * @param id 邀请 ID
     * @return 表示成功或失败的响应
     */
    @POST
    @Path("/{id}/resend")
    Response resend(@PathParam("id") String id);
}
