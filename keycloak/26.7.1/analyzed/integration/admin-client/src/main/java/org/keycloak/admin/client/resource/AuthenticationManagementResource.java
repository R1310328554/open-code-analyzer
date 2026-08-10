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
import java.util.Map;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.idm.AuthenticationExecutionInfoRepresentation;
import org.keycloak.representations.idm.AuthenticationExecutionRepresentation;
import org.keycloak.representations.idm.AuthenticationFlowRepresentation;
import org.keycloak.representations.idm.AuthenticatorConfigInfoRepresentation;
import org.keycloak.representations.idm.AuthenticatorConfigRepresentation;
import org.keycloak.representations.idm.ConfigPropertyRepresentation;
import org.keycloak.representations.idm.RequiredActionConfigInfoRepresentation;
import org.keycloak.representations.idm.RequiredActionConfigRepresentation;
import org.keycloak.representations.idm.RequiredActionProviderRepresentation;
import org.keycloak.representations.idm.RequiredActionProviderSimpleRepresentation;

/**
 * 认证流程与必需操作（Required Action）的管理 REST 资源。
 * <p>
 * 涵盖认证器/表单提供程序枚举、流程 CRUD、执行步骤管理、
 * 认证器配置及必需操作注册与优先级调整等端点。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public interface AuthenticationManagementResource {

    /** 列出可用的表单认证提供程序。 */
    @GET
    @Path("/form-providers")
    @Produces(MediaType.APPLICATION_JSON)
    List<Map<String, Object>> getFormProviders();

    /** 列出可用的认证器提供程序。 */
    @Path("/authenticator-providers")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<Map<String, Object>> getAuthenticatorProviders();

    /** 列出可用的客户端认证器提供程序。 */
    @Path("/client-authenticator-providers")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<Map<String, Object>> getClientAuthenticatorProviders();

    /** 列出可用的表单动作提供程序。 */
    @Path("/form-action-providers")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<Map<String, Object>> getFormActionProviders();

    /** 列出领域内所有认证流程。 */
    @Path("/flows")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<AuthenticationFlowRepresentation> getFlows();

    /** 创建新的认证流程。 */
    @Path("/flows")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response createFlow(AuthenticationFlowRepresentation model);

    /** 按 ID 获取认证流程。 */
    @Path("/flows/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    AuthenticationFlowRepresentation getFlow(@PathParam("id") String id);

    /** 删除指定认证流程。 */
    @Path("/flows/{id}")
    @DELETE
    void deleteFlow(@PathParam("id") String id);

    /** 复制指定别名的认证流程。 */
    @Path("/flows/{flowAlias}/copy")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response copy(@PathParam("flowAlias") String flowAlias, Map<String, Object> data);

    /** 更新指定 ID 的认证流程。 */
    @Path("/flows/{id}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    void updateFlow(@PathParam("id") String id, AuthenticationFlowRepresentation flow);

    /** 向流程中添加子流程执行步骤。 */
    @Path("/flows/{flowAlias}/executions/flow")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    void addExecutionFlow(@PathParam("flowAlias") String flowAlias, Map<String, Object> data);

    /** 向流程中添加认证器执行步骤。 */
    @Path("/flows/{flowAlias}/executions/execution")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    void addExecution(@PathParam("flowAlias") String flowAlias, Map<String, Object> data);

    /** 列出指定流程的所有执行步骤。 */
    @Path("/flows/{flowAlias}/executions")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<AuthenticationExecutionInfoRepresentation> getExecutions(@PathParam("flowAlias") String flowAlias);

    /** 更新流程中某执行步骤的配置（如启用/必需状态）。 */
    @Path("/flows/{flowAlias}/executions")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    void updateExecutions(@PathParam("flowAlias") String flowAlias, AuthenticationExecutionInfoRepresentation rep);

    /** 创建独立的认证执行步骤。 */
    @Path("/executions")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response addExecution(AuthenticationExecutionRepresentation model);

    /** 按 ID 获取认证执行步骤。 */
    @Path("/executions/{executionId}")
	@GET
    @Produces(MediaType.APPLICATION_JSON)
    AuthenticationExecutionRepresentation getExecution(final @PathParam("executionId") String executionId);

    /** 提高执行步骤在流程中的优先级。 */
    @Path("/executions/{executionId}/raise-priority")
    @POST
    void raisePriority(@PathParam("executionId") String execution);

    /** 降低执行步骤在流程中的优先级。 */
    @Path("/executions/{executionId}/lower-priority")
    @POST
    void lowerPriority(@PathParam("executionId") String execution);

    /** 删除指定认证执行步骤。 */
    @Path("/executions/{executionId}")
    @DELETE
    void removeExecution(@PathParam("executionId") String execution);

    /** 为执行步骤创建新的认证器配置。 */
    @Path("/executions/{executionId}/config")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response newExecutionConfig(@PathParam("executionId") String executionId, AuthenticatorConfigRepresentation config);

    /** 列出尚未注册的必需操作提供程序。 */
    @Path("unregistered-required-actions")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<RequiredActionProviderSimpleRepresentation> getUnregisteredRequiredActions();

    /** 注册新的必需操作提供程序。 */
    @Path("register-required-action")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    void registerRequiredAction(RequiredActionProviderSimpleRepresentation action);

    /** 列出已注册的必需操作。 */
    @Path("required-actions")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<RequiredActionProviderRepresentation> getRequiredActions();

    /** 按别名获取必需操作配置。 */
    @Path("required-actions/{alias}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    RequiredActionProviderRepresentation getRequiredAction(@PathParam("alias") String alias);

    /** 更新指定别名的必需操作。 */
    @Path("required-actions/{alias}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    void updateRequiredAction(@PathParam("alias") String alias, RequiredActionProviderRepresentation rep);

    /** 删除指定别名的必需操作。 */
    @Path("required-actions/{alias}")
    @DELETE
    void removeRequiredAction(@PathParam("alias") String alias);

    /** 提高必需操作的显示优先级。 */
    @Path("required-actions/{alias}/raise-priority")
    @POST
    void raiseRequiredActionPriority(@PathParam("alias") String alias);

    /** 降低必需操作的显示优先级。 */
    @Path("required-actions/{alias}/lower-priority")
    @POST
    void lowerRequiredActionPriority(@PathParam("alias") String alias);

    /**
     * 返回指定必需操作的配置项描述。
     *
     * @since Keycloak server 25
     * @param alias 必需操作别名
     * @return 配置项描述信息
     * @throws jakarta.ws.rs.NotFoundException 指定别名的必需操作不存在
     */
    @Path("required-actions/{alias}/config-description")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    RequiredActionConfigInfoRepresentation getRequiredActionConfigDescription(@PathParam("alias") String alias);

    /**
     * 返回指定必需操作的当前配置。
     *
     * @since Keycloak server 25
     * @param alias 必需操作别名
     * @return 必需操作配置
     * @throws jakarta.ws.rs.BadRequestException 必需操作不可配置
     * @throws jakarta.ws.rs.NotFoundException 指定别名的配置不存在
     */
    @Path("required-actions/{alias}/config")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    RequiredActionConfigRepresentation getRequiredActionConfig(@PathParam("alias") String alias);

    /**
     * 删除指定必需操作的配置。
     *
     * @since Keycloak server 25
     * @param alias 待删除配置的必需操作别名
     * @throws jakarta.ws.rs.BadRequestException 必需操作不可配置
     * @throws jakarta.ws.rs.NotFoundException 指定别名的配置不存在
     */
    @Path("required-actions/{alias}/config")
    @DELETE
    void removeRequiredActionConfig(@PathParam("alias") String alias);

    /**
     * 更新指定必需操作的配置。
     *
     * @since Keycloak server 25
     * @param alias 待更新配置的必需操作别名
     * @param rep 必需操作的 JSON 配置表示
     * @throws jakarta.ws.rs.BadRequestException 必需操作不可配置或配置内容无效
     * @throws jakarta.ws.rs.NotFoundException 指定别名的配置不存在
     */
    @Path("required-actions/{alias}/config")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    void updateRequiredActionConfig(@PathParam("alias") String alias, RequiredActionConfigRepresentation rep);

    /** 获取指定认证器提供程序的配置项描述。 */
    @Path("config-description/{providerId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    AuthenticatorConfigInfoRepresentation getAuthenticatorConfigDescription(@PathParam("providerId") String providerId);

    /** 获取各客户端认证器提供程序的配置项描述映射。 */
    @Path("per-client-config-description")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Map<String, List<ConfigPropertyRepresentation>> getPerClientConfigDescription();

    /** 按 ID 获取认证器配置。 */
    @Path("config/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    AuthenticatorConfigRepresentation getAuthenticatorConfig(@PathParam("id") String id);

    /** 删除指定 ID 的认证器配置。 */
    @Path("config/{id}")
    @DELETE
    void removeAuthenticatorConfig(@PathParam("id") String id);

    /** 更新指定 ID 的认证器配置。 */
    @Path("config/{id}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    void updateAuthenticatorConfig(@PathParam("id") String id, AuthenticatorConfigRepresentation config);
}
