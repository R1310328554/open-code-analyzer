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
import org.keycloak.representations.idm.IdentityProviderRepresentation;

/**
 * 组织内单个身份提供程序的管理 REST 资源。
 * <p>
 * 支持读取 IdP 配置、解除关联及查询与该 IdP 关联的有效组织组。
 */
public interface OrganizationIdentityProviderResource {

    /** 获取当前组织身份提供程序的表示对象。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    IdentityProviderRepresentation toRepresentation();

    /** 从组织中移除当前身份提供程序关联。 */
    @DELETE
    Response delete();

    /**
     * 返回与当前身份提供程序关联的组织组列表。
     * <p>
     * 仅当 IdP 已关联到组织且组织已启用时返回组；否则返回错误或空列表。
     *
     * @param search 组名搜索字符串
     * @param searchQuery 属性查询表达式，格式 {@code key1:value1 key2:value2}
     * @param exact 若为 true，对 {@code search} 进行精确匹配
     * @param first 分页起始位置
     * @param max 最大返回数量
     * @param briefRepresentation 若为 true，返回简要组表示；否则返回完整表示
     * @return 与组织关联的组列表
     * @since Keycloak server 26.6.0
     */
    @Path("groups")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<GroupRepresentation> getGroups(@QueryParam("search") String search,
                                        @QueryParam("q") String searchQuery,
                                        @QueryParam("exact") @DefaultValue("false") Boolean exact,
                                        @QueryParam("first") Integer first,
                                        @QueryParam("max") Integer max,
                                        @QueryParam("briefRepresentation") @DefaultValue("true") boolean briefRepresentation,
                                        @QueryParam("subGroupsCount") @DefaultValue("false") boolean subGroupsCount);
}
