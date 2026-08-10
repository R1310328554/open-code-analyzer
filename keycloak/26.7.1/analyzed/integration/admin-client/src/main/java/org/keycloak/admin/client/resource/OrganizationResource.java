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

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.idm.OrganizationRepresentation;

/**
 * 单个组织的管理 REST 资源。
 * <p>
 * 支持读取、更新、删除组织，并提供成员、邀请、身份提供者与组等子资源访问入口。
 */
public interface OrganizationResource {

    /** 获取当前组织的表示对象。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    OrganizationRepresentation toRepresentation();

    /** 更新组织配置。 */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    Response update(OrganizationRepresentation organization);

    /** 删除当前组织。 */
    @DELETE
    Response delete();

    /** 获取组织成员管理子资源。 */
    @Path("members")
    OrganizationMembersResource members();

    /**
     * 获取组织邀请管理子资源。
     *
     * @since Keycloak server 26.5.0.
     * @return 用于管理组织邀请的 {@link OrganizationInvitationsResource}
     */
    @Path("invitations")
    OrganizationInvitationsResource invitations();

    /** 获取组织身份提供者管理子资源。 */
    @Path("identity-providers")
    OrganizationIdentityProvidersResource identityProviders();

    /** 获取组织内用户组管理子资源。 */
    @Path("groups")
    OrganizationGroupsResource groups();
}
