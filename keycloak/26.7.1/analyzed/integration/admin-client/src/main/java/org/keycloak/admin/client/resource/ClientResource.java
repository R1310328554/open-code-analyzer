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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.adapters.action.GlobalRequestResult;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ClientScopeRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.ManagementPermissionReference;
import org.keycloak.representations.idm.ManagementPermissionRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.UserSessionRepresentation;

/**
 * 单个 OAuth/OIDC 客户端的管理 REST 资源。
 * <p>
 * 提供客户端 CRUD、密钥管理、会话查询、作用域映射、
 * 证书配置、适配器安装文件导出及细粒度权限等完整管理能力。
 *
 * @author rodrigo.sasaki@icarros.com.br
 */
public interface ClientResource {

    /**
     * 启用或禁用细粒度权限功能。
     * <p>
     * 返回更新后的服务器状态，封装于 {@link ManagementPermissionReference} 中。
     *
     * @param status 待应用的权限状态请求
     * @return 指示更新后状态的权限引用
     */
    @PUT
    @Path("/management/permissions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    ManagementPermissionReference setPermissions(ManagementPermissionRepresentation status);

    /**
     * 查询细粒度权限功能是否已启用。
     *
     * @return 当前权限功能的表示对象
     */
    @GET
    @Path("/management/permissions")
    @Produces(MediaType.APPLICATION_JSON)
    ManagementPermissionReference getPermissions();

    /** 获取协议映射器（Protocol Mappers）子资源。 */
    @Path("protocol-mappers")
    ProtocolMappersResource getProtocolMappers();

    /** 获取当前客户端的完整表示对象。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    ClientRepresentation toRepresentation();

    /** 更新客户端配置。 */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    void update(ClientRepresentation clientRepresentation);

    /** 删除当前客户端。 */
    @DELETE
    void remove();

    /** 生成新的客户端密钥（Client Secret）。 */
    @POST
    @Path("client-secret")
    @Produces(MediaType.APPLICATION_JSON)
    CredentialRepresentation generateNewSecret();

    /** 获取当前客户端密钥。 */
    @GET
    @Path("client-secret")
    @Produces(MediaType.APPLICATION_JSON)
    CredentialRepresentation getSecret();

    /**
     * 重新生成客户端注册访问令牌（Registration Access Token）。
     *
     * @return 包含新注册访问令牌的客户端表示对象
     */
    @Path("registration-access-token")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    ClientRepresentation regenerateRegistrationAccessToken();

    /**
     * 获取指定属性前缀的证书管理子资源。
     *
     * @param attributePrefix 证书属性前缀（如 saml.signing）
     * @return 客户端属性证书资源
     */
    @Path("certificates/{attr}")
    ClientAttributeCertificateResource getCertficateResource(@PathParam("attr") String attributePrefix);

    /**
     * 以字符串形式返回适配器安装配置。
     * <p>
     * 返回内容通常为特定提供程序的 XML 格式配置。
     *
     * @param providerId 安装提供程序 ID
     * @return 安装配置字符串
     */
    @GET
    @Path("installation/providers/{providerId}")
    String getInstallationProvider(@PathParam("providerId") String providerId);

    /**
     * 以 HTTP 响应形式返回适配器安装配置。
     *
     * @param providerId 安装提供程序 ID
     * @return Jakarta REST 响应
     */
    @GET
    @Path("installation/providers/{providerId}")
    Response getInstallationProviderAsResponse(@PathParam("providerId") String providerId);

    /** 获取当前客户端的活跃会话数量统计。 */
    @Path("session-count")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Map<String, Integer> getApplicationSessionCount();

    /** 分页列出当前客户端的在线用户会话。 */
    @Path("user-sessions")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<UserSessionRepresentation> getUserSessions(@QueryParam("first") Integer firstResult, @QueryParam("max") Integer maxResults);

    /** 获取当前客户端的离线会话数量统计。 */
    @Path("offline-session-count")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Map<String, Long> getOfflineSessionCount();

    /** 分页列出当前客户端的离线用户会话。 */
    @Path("offline-sessions")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<UserSessionRepresentation> getOfflineUserSessions(@QueryParam("first") Integer firstResult, @QueryParam("max") Integer maxResults);

    /** 向所有已注册节点推送令牌吊销通知。 */
    @POST
    @Path("push-revocation")
    @Produces(MediaType.APPLICATION_JSON)
    void pushRevocation();

    /** 获取客户端作用域映射（Scope Mappings）子资源。 */
    @Path("/scope-mappings")
    RoleMappingResource getScopeMappings();

    /** 获取客户端角色（Roles）子资源。 */
    @Path("/roles")
    RolesResource roles();

    /** 获取客户端作用域评估（Evaluate Scopes）子资源。 */
    @Path("/evaluate-scopes")
    ClientScopeEvaluateResource clientScopesEvaluate();

    /**
     * 获取默认客户端作用域列表（仅返回名称与 ID）。
     *
     * @return 默认客户端作用域列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("default-client-scopes")
    List<ClientScopeRepresentation> getDefaultClientScopes();

    /** 将指定作用域添加为默认客户端作用域。 */
    @PUT
    @Path("default-client-scopes/{clientScopeId}")
    void addDefaultClientScope(@PathParam("clientScopeId") String clientScopeId);

    /** 从默认客户端作用域中移除指定作用域。 */
    @DELETE
    @Path("default-client-scopes/{clientScopeId}")
    void removeDefaultClientScope(@PathParam("clientScopeId") String clientScopeId);

    /**
     * 获取可选客户端作用域列表（仅返回名称与 ID）。
     *
     * @return 可选客户端作用域列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("optional-client-scopes")
    List<ClientScopeRepresentation> getOptionalClientScopes();

    /** 将指定作用域添加为可选客户端作用域。 */
    @PUT
    @Path("optional-client-scopes/{clientScopeId}")
    void addOptionalClientScope(@PathParam("clientScopeId") String clientScopeId);

    /** 从可选客户端作用域中移除指定作用域。 */
    @DELETE
    @Path("optional-client-scopes/{clientScopeId}")
    void removeOptionalClientScope(@PathParam("clientScopeId") String clientScopeId);

    /** 获取客户端服务账户对应的用户表示对象。 */
    @Path("/service-account-user")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    UserRepresentation getServiceAccountUser();

    /** 注册客户端集群节点（用于适配器会话同步）。 */
    @Path("nodes")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    void registerNode(Map<String, String> formParams);

    /** 注销指定集群节点。 */
    @Path("nodes/{node}")
    @DELETE
    void unregisterNode(final @PathParam("node") String node);

    /** 测试所有已注册节点是否可达。 */
    @Path("test-nodes-available")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    GlobalRequestResult testNodesAvailable();

    /** 获取细粒度授权（Authorization）子资源。 */
    @Path("/authz/resource-server")
    AuthorizationResource authorization();


    /** 获取轮换中的客户端密钥（密钥轮换功能）。 */
    @Path("client-secret/rotated")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public CredentialRepresentation getClientRotatedSecret();

    /** 作废轮换中的客户端密钥。 */
    @Path("client-secret/rotated")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void invalidateRotatedSecret();
}
