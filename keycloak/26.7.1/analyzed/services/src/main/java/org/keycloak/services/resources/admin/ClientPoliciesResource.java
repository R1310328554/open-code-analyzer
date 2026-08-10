/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.services.resources.admin;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.http.HttpRequest;
import org.keycloak.http.HttpResponse;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.ClientPoliciesRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.resources.KeycloakOpenAPI;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.NoCache;

/**
 * 客户端策略（Client Policies）管理 REST 资源。
 * <p>读取与更新领域级客户端策略配置，支持合并全局策略。</p>
 */
@Extension(name = KeycloakOpenAPI.Profiles.ADMIN, value = "")
public class ClientPoliciesResource {
    /** 日志记录器 */
    protected static final Logger logger = Logger.getLogger(ClientPoliciesResource.class);

    /** HTTP 请求 */
    protected final HttpRequest request;

    /** HTTP 响应 */
    protected final HttpResponse response;

    /** Keycloak 会话 */
    protected final KeycloakSession session;

    /** 当前领域 */
    protected final RealmModel realm;
    /** 细粒度权限评估器 */
    private final AdminPermissionEvaluator auth;

    /** 构造客户端策略资源。 */
    public ClientPoliciesResource(KeycloakSession session, AdminPermissionEvaluator auth) {
        this.session = session;
        this.realm = session.getContext().getRealm();
        this.auth = auth;
        this.request = session.getContext().getHttpRequest();
        this.response = session.getContext().getHttpResponse();
    }

    /**
     * 获取领域客户端策略。
     * @param includeGlobalPolicies 是否合并全局策略
     * @return 客户端策略表示
     */
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.REALMS_ADMIN)
    @Operation()
    public ClientPoliciesRepresentation getPolicies(@QueryParam("include-global-policies") boolean includeGlobalPolicies) {
        auth.realm().requireViewRealm();

        try {
            return session.clientPolicy().getClientPolicies(realm, includeGlobalPolicies);
        } catch (ClientPolicyException e) {
            throw ErrorResponse.error(e.getError(), Response.Status.BAD_REQUEST);
        }
    }

    /** 更新领域客户端策略配置。 */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.REALMS_ADMIN)
    @Operation()
    public Response updatePolicies(final ClientPoliciesRepresentation clientPolicies) {
        auth.realm().requireManageRealm();

        try {
            session.clientPolicy().updateClientPolicies(realm, clientPolicies);
        } catch (ClientPolicyException e) {
            throw ErrorResponse.error(e.getError(), Response.Status.BAD_REQUEST);
        }
        return Response.noContent().build();
    }
}
