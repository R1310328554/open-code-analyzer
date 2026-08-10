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

package org.keycloak.organization.admin.resource;

import java.util.Objects;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelValidationException;
import org.keycloak.models.OrganizationModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.models.utils.RepresentationToModel;
import org.keycloak.organization.OrganizationProvider;
import org.keycloak.organization.validation.OrganizationsValidation;
import org.keycloak.organization.validation.OrganizationsValidation.OrganizationValidationException;
import org.keycloak.representations.idm.OrganizationRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.resources.KeycloakOpenAPI;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.NoCache;

@Extension(name = KeycloakOpenAPI.Profiles.ADMIN, value = "")
/**
 * 单个组织的管理 REST 资源：提供查询、更新、删除及成员/邀请/身份提供者/组等子资源入口。
 * <p>调用方须先通过 {@link OrganizationsResource#get(String)} 获取本资源，该路径已执行 {@code auth.orgs().requireView(organization)} 权限校验。</p>
 */
public class OrganizationResource {

    private final KeycloakSession session;
    private final OrganizationProvider provider;
    private final AdminEventBuilder adminEvent;
    private final OrganizationModel organization;
    private final AdminPermissionEvaluator auth;

    /**
     * @param session Keycloak 会话
     * @param organization 目标组织模型
     * @param adminEvent 管理事件构建器
     * @param auth 管理权限评估器
     */
    public OrganizationResource(KeycloakSession session, OrganizationModel organization, AdminEventBuilder adminEvent, AdminPermissionEvaluator auth) {
        this.session = session;
        this.provider = session == null ? null : session.getProvider(OrganizationProvider.class);
        this.organization = organization;
        this.adminEvent = adminEvent.resource(ResourceType.ORGANIZATION);
        this.auth = auth;
    }

    /**
     * 前置条件：调用方须已通过 {@link OrganizationsResource#get(String)}，该路径已执行 {@code auth.orgs().requireView(organization)}。
     */
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.ORGANIZATIONS)
    @Operation(summary = "Returns the organization representation")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = OrganizationRepresentation.class))),
        @APIResponse(responseCode = "403", description = "Forbidden")
    })
    /** @return 当前组织的 {@link OrganizationRepresentation} */
    public OrganizationRepresentation get() {
        return ModelToRepresentation.toRepresentation(organization, false);
    }

    @DELETE
    @Tag(name = KeycloakOpenAPI.Admin.Tags.ORGANIZATIONS)
    @Operation(summary = "Deletes the organization")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "No Content"),
        @APIResponse(responseCode = "400", description = "Bad Request"),
        @APIResponse(responseCode = "403", description = "Forbidden")
    })
    /** 删除当前组织；成功返回 204，失败返回 400。 */
    public Response delete() {
        auth.orgs().requireManage(organization);
        boolean removed = provider.remove(organization);
        if (removed) {
            adminEvent.operation(OperationType.DELETE).resourcePath(session.getContext().getUri()).success();
            return Response.noContent().build();
        } else {
            throw ErrorResponse.error("organization couldn't be deleted", Status.BAD_REQUEST);
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.ORGANIZATIONS)
    @Operation(summary = "Updates the organization")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "No Content"),
        @APIResponse(responseCode = "400", description = "Bad Request"),
        @APIResponse(responseCode = "403", description = "Forbidden"),
        @APIResponse(responseCode = "409", description = "Conflict")
    })
    /**
     * 更新组织属性。
     * @param organizationRep 组织表示对象
     * @return 更新结果响应
     */
    public Response update(OrganizationRepresentation organizationRep) {
        auth.orgs().requireManage(organization);
        // 尝试将组织名称修改为已存在的名称时拒绝
        if (!Objects.equals(organization.getName(), organizationRep.getName()) &&
                provider.getAllStream(organizationRep.getName(), true, -1, -1).findAny().isPresent()) {
            throw ErrorResponse.error("A organization with the same name already exists.", Status.CONFLICT);
        }

        try {
            OrganizationsValidation.validateUrl(organizationRep.getRedirectUrl());
            RepresentationToModel.toModel(organizationRep, organization);
            adminEvent.operation(OperationType.UPDATE).resourcePath(session.getContext().getUri()).representation(organizationRep).success();
            return Response.noContent().build();
        } catch (ModelValidationException | OrganizationValidationException ex) {
            throw ErrorResponse.error(ex.getMessage(), Response.Status.BAD_REQUEST);
        }
    }

    /** @return 组织成员管理子资源 */
    @Path("members")
    public OrganizationMemberResource members() {
        return new OrganizationMemberResource(session, organization, adminEvent, auth);
    }

    /** @return 组织邀请管理子资源 */
    @Path("invitations")
    public OrganizationInvitationResource invitations() {
        return new OrganizationInvitationResource(session, organization, adminEvent, auth);
    }

    /** @return 组织关联身份提供者管理子资源 */
    @Path("identity-providers")
    public OrganizationIdentityProvidersResource identityProvider() {
        return new OrganizationIdentityProvidersResource(session, organization, adminEvent, auth);
    }

    /** @return 组织组管理子资源 */
    @Path("groups")
    public OrganizationGroupsResource groups() {
        return new OrganizationGroupsResource(session, organization, adminEvent, auth);
    }
}
