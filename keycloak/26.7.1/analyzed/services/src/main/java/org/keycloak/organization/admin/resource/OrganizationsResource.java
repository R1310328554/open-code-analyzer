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


import java.util.Map;
import java.util.stream.Stream;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.keycloak.authorization.fgap.AdminPermissionsSchema;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.ModelValidationException;
import org.keycloak.models.OrganizationModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.models.utils.RepresentationToModel;
import org.keycloak.organization.OrganizationProvider;
import org.keycloak.organization.utils.Organizations;
import org.keycloak.organization.validation.OrganizationsValidation;
import org.keycloak.organization.validation.OrganizationsValidation.OrganizationValidationException;
import org.keycloak.representations.idm.OrganizationRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.resources.KeycloakOpenAPI;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;
import org.keycloak.utils.ReservedCharValidator;
import org.keycloak.utils.SearchQueryUtils;
import org.keycloak.utils.StringUtil;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.NoCache;

@Extension(name = KeycloakOpenAPI.Profiles.ADMIN, value = "")
/**
 * 组织集合的管理 REST 资源：支持创建、分页搜索、计数及按成员查询关联组织。
 * <p>所有操作均经 {@link AdminPermissionEvaluator} 的组织权限校验，并在组织功能未启用时通过 {@link Organizations#checkEnabled} 拒绝访问。</p>
 */
public class OrganizationsResource {

    private final KeycloakSession session;
    private final OrganizationProvider provider;
    private final AdminPermissionEvaluator auth;
    private final AdminEventBuilder adminEvent;

    private static final Logger logger = Logger.getLogger(OrganizationsResource.class);

    /**
     * @param session Keycloak 会话
     * @param auth 管理权限评估器
     * @param adminEvent 管理事件构建器
     */
    public OrganizationsResource(KeycloakSession session, AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        this.session = session;
        this.provider = session == null ? null : session.getProvider(OrganizationProvider.class);
        this.auth = auth;
        this.adminEvent = adminEvent.resource(ResourceType.ORGANIZATION);
    }

    /**
     * 基于指定的 {@link OrganizationRepresentation} 创建新组织。
     *
     * @param organization 包含组织数据的表示对象
     * @return 包含操作状态的 {@link Response}
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.ORGANIZATIONS)
    @Operation( summary = "Creates a new organization")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "Created"),
        @APIResponse(responseCode = "400", description = "Bad Request"),
        @APIResponse(responseCode = "403", description = "Forbidden")
    })
    public Response create(OrganizationRepresentation organization) {
        auth.orgs().requireManage();
        Organizations.checkEnabled(provider, auth);

        if (organization == null) {
            throw ErrorResponse.error("Organization cannot be null.", Response.Status.BAD_REQUEST);
        }

        ReservedCharValidator.validateNoSpace(organization.getAlias());

        try {
            OrganizationsValidation.validateUrl(organization.getRedirectUrl());

            OrganizationModel model = provider.create(organization.getName(), organization.getAlias());
            RepresentationToModel.toModel(organization, model);
            organization.setId(model.getId());
            adminEvent.operation(OperationType.CREATE).resourcePath(session.getContext().getUri(), model.getId()).representation(organization).success();
            return Response.created(session.getContext().getUri().getAbsolutePathBuilder().path(model.getId()).build()).build();
        } catch (ModelValidationException | OrganizationValidationException ex) {
            throw ErrorResponse.error(ex.getMessage(), Response.Status.BAD_REQUEST);
        } catch (ModelDuplicateException mde) {
            throw ErrorResponse.error(mde.getMessage(), Status.CONFLICT);
        }
    }

    /**
     * 按查询参数过滤并返回组织流。
     *
     * @param search 组织名称或域名
     * @param searchQuery 自定义属性查询，格式为 key1:value1 key2:value2
     * @param exact 为 true 时精确匹配 search；为 false 时部分匹配
     * @param first 分页起始位置，负数或 null 时忽略
     * @param max 最大返回条数，负数或 null 时忽略
     * @return 匹配的组织表示流，永不为 null
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.ORGANIZATIONS)
    @Operation(summary = "Returns a paginated list of organizations filtered according to the specified parameters")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = OrganizationRepresentation.class, type = SchemaType.ARRAY))),
        @APIResponse(responseCode = "403", description = "Forbidden"),
        @APIResponse(responseCode = "404", description = "Not Found")
    })
    public Stream<OrganizationRepresentation> search(
            @Parameter(description = "A String representing either an organization name or domain") @QueryParam("search") String search,
            @Parameter(description = "A query to search for custom attributes, in the format 'key1:value2 key2:value2'") @QueryParam("q") String searchQuery,
            @Parameter(description = "Boolean which defines whether the param 'search' must match exactly or not") @QueryParam("exact") Boolean exact,
            @Parameter(description = "The position of the first result to be processed (pagination offset)") @QueryParam("first") @DefaultValue("0") Integer first,
            @Parameter(description = "The maximum number of results to be returned - defaults to 10") @QueryParam("max") @DefaultValue("10") Integer max,
            @Parameter(description = "if false, return the full representation. Otherwise, only the basic fields are returned.") @QueryParam("briefRepresentation") @DefaultValue("true") boolean briefRepresentation
    ) {
        auth.orgs().requireQuery();
        Organizations.checkEnabled(provider, auth);

        // 专用管理员可查询但不可查看（且未启用 FGAP）时直接返回空列表，避免数据库往返
        if (!AdminPermissionsSchema.SCHEMA.isAdminPermissionsEnabled(session.getContext().getRealm()) && !auth.orgs().canView()) {
            return Stream.empty();
        }

        // 按自定义属性搜索组织
        if (StringUtil.isNotBlank(searchQuery)) {
            Map<String, String> attributes = SearchQueryUtils.getFields(searchQuery);
            return provider.getAllStream(attributes, first, max).map(model -> ModelToRepresentation.toRepresentation(model, briefRepresentation));
        } else {
            return provider.getAllStream(search, exact, first, max).map(model -> ModelToRepresentation.toRepresentation(model, briefRepresentation));
        }
    }

    /** 单个组织管理 REST API 的基路径。 */

    @Path("{org-id}")
    public OrganizationResource get(@PathParam("org-id") String orgId) {
        Organizations.checkEnabled(provider, auth);

        if (StringUtil.isBlank(orgId)) {
            throw ErrorResponse.error("Id cannot be null.", Response.Status.BAD_REQUEST);
        }

        OrganizationModel organizationModel = provider.getById(orgId);

        if (organizationModel == null) {
            throw (auth.orgs().canQuery()) ?
                    ErrorResponse.error("Organization not found.", Response.Status.NOT_FOUND) :
                    new ForbiddenException();
        }

        auth.orgs().requireView(organizationModel);
        session.getContext().setOrganization(organizationModel);

        return new OrganizationResource(session, organizationModel, adminEvent, auth);
    }

    /**
     * 返回符合搜索条件的组织数量。
     *
     * @return 组织计数
     */
    @GET
    @NoCache
    @Path("count")
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.ORGANIZATIONS)
    @Operation(summary = "Returns the organizations counts.")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "OK"),
        @APIResponse(responseCode = "403", description = "Forbidden")
    })
    public long getOrganizationCount(
            @Parameter(description = "A String representing either an organization name or domain") @QueryParam("search") String search,
            @Parameter(description = "A query to search for custom attributes, in the format 'key1:value2 key2:value2'") @QueryParam("q") String searchQuery,
            @Parameter(description = "Boolean which defines whether the param 'search' must match exactly or not") @QueryParam("exact") Boolean exact
    ) {
        auth.orgs().requireQuery();
        Organizations.checkEnabled(provider, auth);

        // 专用管理员可查询但不可查看（且未启用 FGAP）时直接返回 0，避免数据库往返
        if (!AdminPermissionsSchema.SCHEMA.isAdminPermissionsEnabled(session.getContext().getRealm()) && !auth.orgs().canView()) {
            return 0L;
        }

        if (StringUtil.isNotBlank(searchQuery)) {
            Map<String, String> attributes = SearchQueryUtils.getFields(searchQuery);
            return provider.count(attributes);
        }
        return provider.count(search, exact);
    }

    @Path("members/{member-id}/organizations")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.ORGANIZATIONS)
    @Operation(summary = "Returns the organizations associated with the user that has the specified id")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = OrganizationRepresentation.class, type = SchemaType.ARRAY))),
        @APIResponse(responseCode = "400", description = "Bad Request"),
        @APIResponse(responseCode = "403", description = "Forbidden")
    })
    /**
     * 返回指定用户所属的组织列表。
     * @param memberId 用户 ID
     * @param briefRepresentation 是否返回简要表示
     * @return 组织表示流
     */
    public Stream<OrganizationRepresentation> getOrganizations(
            @PathParam("member-id") String memberId,
            @Parameter(description = "if false, return the full representation. Otherwise, only the basic fields are returned.")
            @QueryParam("briefRepresentation") @DefaultValue("true") boolean briefRepresentation
    ) {
        auth.orgs().requireQuery();
        Organizations.checkEnabled(provider, auth);

        return new OrganizationMemberResource(session, null, adminEvent, auth).getOrganizations(memberId, briefRepresentation);
    }
}
