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
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.FormParam;
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
import org.keycloak.representations.idm.AdminEventRepresentation;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ClientScopeRepresentation;
import org.keycloak.representations.idm.EventRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.LDAPCapabilityRepresentation;
import org.keycloak.representations.idm.PartialImportRepresentation;
import org.keycloak.representations.idm.RealmEventsConfigRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.TestLdapConnectionRepresentation;

/**
 * 单个领域（Realm）的管理 REST 资源。
 * <p>
 * 提供领域配置、客户端、用户、角色、组、事件、身份提供者、
 * 缓存管理、LDAP/SMTP 连接测试及组织等全方位管理能力。
 *
 * @author rodrigo.sasaki@icarros.com.br
 */
public interface RealmResource {

    /** 获取当前领域的完整表示对象。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    RealmRepresentation toRepresentation();

    /** 更新领域配置。 */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    void update(RealmRepresentation realmRepresentation);

    /** 获取客户端集合管理子资源。 */
    @Path("clients")
    ClientsResource clients();

    /** 获取客户端作用域集合管理子资源。 */
    @Path("client-scopes")
    ClientScopesResource clientScopes();

    /** 列出领域的默认客户端作用域（default default scopes）。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("default-default-client-scopes")
    List<ClientScopeRepresentation> getDefaultDefaultClientScopes();

    /** 将客户端作用域添加为领域默认作用域。 */
    @PUT
    @Path("default-default-client-scopes/{clientScopeId}")
    void addDefaultDefaultClientScope(@PathParam("clientScopeId") String clientScopeId);

    /** 从领域默认作用域中移除指定客户端作用域。 */
    @DELETE
    @Path("default-default-client-scopes/{clientScopeId}")
    void removeDefaultDefaultClientScope(@PathParam("clientScopeId") String clientScopeId);

    /** 列出领域的默认可选客户端作用域。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("default-optional-client-scopes")
    List<ClientScopeRepresentation> getDefaultOptionalClientScopes();

    /** 将客户端作用域添加为领域默认可选作用域。 */
    @PUT
    @Path("default-optional-client-scopes/{clientScopeId}")
    void addDefaultOptionalClientScope(@PathParam("clientScopeId") String clientScopeId);

    /** 从领域默认可选作用域中移除指定客户端作用域。 */
    @DELETE
    @Path("default-optional-client-scopes/{clientScopeId}")
    void removeDefaultOptionalClientScope(@PathParam("clientScopeId") String clientScopeId);

    /** 将客户端描述文本（如 OpenID 客户端 JSON）转换为 {@link ClientRepresentation}。 */
    @Path("client-description-converter")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    ClientRepresentation convertClientDescription(String description);

    /** 获取用户集合管理子资源。 */
    @Path("users")
    UsersResource users();

    /** 获取角色集合管理子资源（按角色名访问）。 */
    @Path("roles")
    RolesResource roles();

    /** 获取按内部 ID 访问角色的管理子资源。 */
    @Path("roles-by-id")
    RoleByIdResource rolesById();

    /** 获取用户组集合管理子资源。 */
    @Path("groups")
    GroupsResource groups();

    /** 清空领域内的用户事件日志。 */
    @DELETE
    @Path("events")
    void clearEvents();

    /** 获取领域内的全部用户事件。 */
    @GET
    @Path("events")
    @Produces(MediaType.APPLICATION_JSON)
    List<EventRepresentation> getEvents();

    /**
     * 按多种条件过滤并分页查询用户事件。
     *
     * @param types 事件类型列表
     * @param client 应用或 OAuth 客户端名称
     * @param user 用户 ID
     * @param dateFrom 起始日期（含，格式 yyyy-MM-dd）
     * @param dateTo 结束日期（含，格式 yyyy-MM-dd）
     * @param ipAddress IP 地址
     * @param firstResult 分页偏移
     * @param maxResults 最大返回条数（默认 100）
     * @return 用户事件列表
     */
    @Path("events")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<EventRepresentation> getEvents(@QueryParam("type") List<String> types, @QueryParam("client") String client,
            @QueryParam("user") String user, @QueryParam("dateFrom") String dateFrom, @QueryParam("dateTo") String dateTo,
            @QueryParam("ipAddress") String ipAddress, @QueryParam("first") Integer firstResult,
            @QueryParam("max") Integer maxResults);

    /**
     * 查询用户事件，支持排序。
     *
     * 返回全部事件，或按 URL 查询参数过滤。
     *
     * @param types 要返回的事件类型
     * @param client 应用或 OAuth 客户端名称
     * @param user 用户 ID
     * @param ipAddress IP 地址
     * @param dateFrom 起始日期（含，格式 yyyy-MM-dd）
     * @param dateTo 结束日期（含，格式 yyyy-MM-dd）
     * @param firstResult 分页偏移
     * @param maxResults 最大返回条数（默认 100）
     * @param direction 排序方向，可选 "asc" 或 "desc"。自 Keycloak 26.2 起支持
     * @return 用户事件列表
     * @since Keycloak 26.2. Use method {@link #getEvents(List, String, String, String, String, String, Integer, Integer)} for the older versions of the Keycloak server
     */
    @Path("events")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<EventRepresentation> getEvents(@QueryParam("type") List<String> types, @QueryParam("client") String client,
            @QueryParam("user") String user, @QueryParam("dateFrom") String dateFrom, @QueryParam("dateTo") String dateTo,
            @QueryParam("ipAddress") String ipAddress, @QueryParam("first") Integer firstResult,
            @QueryParam("max") Integer maxResults,
            @QueryParam("direction") String direction);

    /**
     * 查询用户事件，支持 Epoch 时间戳与排序。
     *
     * 返回全部事件，或按 URL 查询参数过滤。
     *
     * @param types 要返回的事件类型
     * @param client 应用或 OAuth 客户端名称
     * @param user 用户 ID
     * @param ipAddress IP 地址
     * @param dateFrom 起始时间（Epoch 毫秒）。自 Keycloak 26.2 起支持
     * @param dateTo 结束时间（Epoch 毫秒）。自 Keycloak 26.2 起支持
     * @param firstResult 分页偏移
     * @param maxResults 最大返回条数（默认 100）
     * @param direction 排序方向，可选 "asc" 或 "desc"。自 Keycloak 26.2 起支持
     * @return 用户事件列表
     * @since Keycloak 26.2. Use method {@link #getEvents(List, String, String, String, String, String, Integer, Integer)} for the older versions of the Keycloak server
     */
    @Path("events")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<EventRepresentation> getEvents(@QueryParam("type") List<String> types, @QueryParam("client") String client,
            @QueryParam("user") String user, @QueryParam("dateFrom") long dateFrom, @QueryParam("dateTo") long dateTo,
            @QueryParam("ipAddress") String ipAddress, @QueryParam("first") Integer firstResult,
            @QueryParam("max") Integer maxResults,
            @QueryParam("direction") String direction);

    /** 清空领域内的管理员事件日志。 */
    @DELETE
    @Path("admin-events")
    void clearAdminEvents();

    /** 获取领域内的全部管理员事件。 */
    @GET
    @Path("admin-events")
    @Produces(MediaType.APPLICATION_JSON)
    List<AdminEventRepresentation> getAdminEvents();

    /**
     * 按多种条件过滤并分页查询管理员事件。
     *
     * @param operationTypes 操作类型列表
     * @param authRealm 执行操作时用户认证所在的领域
     * @param authClient 认证该管理操作的客户端
     * @param authUser 执行管理操作的用户
     * @param authIpAddress 操作来源 IP 地址
     * @param resourcePath 资源路径
     * @param dateFrom 起始日期（含，格式 yyyy-MM-dd）
     * @param dateTo 结束日期（含，格式 yyyy-MM-dd）
     * @param firstResult 分页偏移
     * @param maxResults 最大返回条数（默认 100）
     * @return 管理员事件列表
     */
    @GET
    @Path("admin-events")
    @Produces(MediaType.APPLICATION_JSON)
    List<AdminEventRepresentation> getAdminEvents(@QueryParam("operationTypes") List<String> operationTypes, @QueryParam("authRealm") String authRealm, @QueryParam("authClient") String authClient,
            @QueryParam("authUser") String authUser, @QueryParam("authIpAddress") String authIpAddress,
            @QueryParam("resourcePath") String resourcePath, @QueryParam("dateFrom") String dateFrom,
            @QueryParam("dateTo") String dateTo, @QueryParam("first") Integer firstResult,
            @QueryParam("max") Integer maxResults);

    /**
     * 按多种条件（含资源类型）过滤并分页查询管理员事件。
     *
     * @param operationTypes 操作类型列表
     * @param authRealm 执行操作时用户认证所在的领域
     * @param authClient 认证该管理操作的客户端
     * @param authUser 执行管理操作的用户
     * @param authIpAddress 操作来源 IP 地址
     * @param resourcePath 资源路径
     * @param resourceTypes 资源类型列表
     * @param dateFrom 起始日期（含，格式 yyyy-MM-dd）
     * @param dateTo 结束日期（含，格式 yyyy-MM-dd）
     * @param firstResult 分页偏移
     * @param maxResults 最大返回条数（默认 100）
     * @return 管理员事件列表
     */
    @GET
    @Path("admin-events")
    @Produces(MediaType.APPLICATION_JSON)
    List<AdminEventRepresentation> getAdminEvents(@QueryParam("operationTypes") List<String> operationTypes, @QueryParam("authRealm") String authRealm, @QueryParam("authClient") String authClient,
            @QueryParam("authUser") String authUser, @QueryParam("authIpAddress") String authIpAddress,
            @QueryParam("resourcePath") String resourcePath, @QueryParam("resourceTypes") List<String> resourceTypes, @QueryParam("dateFrom") String dateFrom,
            @QueryParam("dateTo") String dateTo, @QueryParam("first") Integer firstResult,
            @QueryParam("max") Integer maxResults);

    /**
     * 查询管理员事件，支持排序。
     *
     * 返回全部管理员事件，或按 URL 查询参数过滤。
     *
     * @param operationTypes 操作类型
     * @param authRealm 执行操作时用户认证所在的领域
     * @param authClient 认证该管理操作的客户端
     * @param authUser 执行管理操作的用户
     * @param authIpAddress 操作来源 IP 地址
     * @param resourcePath 资源路径
     * @param resourceTypes 资源类型
     * @param dateFrom 起始时间（Epoch 毫秒）。自 Keycloak 26.2 起支持
     * @param dateTo 结束时间（Epoch 毫秒）。自 Keycloak 26.2 起支持
     * @param firstResult 分页偏移
     * @param maxResults 最大返回条数（默认 100）
     * @param direction 排序方向，可选 "asc" 或 "desc"。自 Keycloak 26.2 起支持
     * @return 管理员事件列表
     * @since Keycloak 26.2. Use method {@link #getAdminEvents(List, String, String, String, String, String, List, String, String, Integer, Integer)} for the older versions of the Keycloak server
     */
    @GET
    @Path("admin-events")
    @Produces(MediaType.APPLICATION_JSON)
    List<AdminEventRepresentation> getAdminEvents(@QueryParam("operationTypes") List<String> operationTypes, @QueryParam("authRealm") String authRealm, @QueryParam("authClient") String authClient,
            @QueryParam("authUser") String authUser, @QueryParam("authIpAddress") String authIpAddress,
            @QueryParam("resourcePath") String resourcePath, @QueryParam("resourceTypes") List<String> resourceTypes, @QueryParam("dateFrom") String dateFrom,
            @QueryParam("dateTo") String dateTo, @QueryParam("first") Integer firstResult,
            @QueryParam("max") Integer maxResults, @QueryParam("direction") String direction);

    /**
     * 查询管理员事件，支持日期字符串与排序。
     *
     * 返回全部管理员事件，或按 URL 查询参数过滤。
     *
     * @param operationTypes 操作类型
     * @param authRealm 执行操作时用户认证所在的领域
     * @param authClient 认证该管理操作的客户端
     * @param authUser 执行管理操作的用户
     * @param authIpAddress 操作来源 IP 地址
     * @param resourcePath 资源路径
     * @param resourceTypes 资源类型
     * @param dateFrom 起始日期（含，格式 yyyy-MM-dd）
     * @param dateTo 结束日期（含，格式 yyyy-MM-dd）
     * @param firstResult 分页偏移
     * @param maxResults 最大返回条数（默认 100）
     * @param direction 排序方向，可选 "asc" 或 "desc"。自 Keycloak 26.2 起支持
     * @return 管理员事件列表
     * @since Keycloak 26.2. Use method {@link #getAdminEvents(List, String, String, String, String, String, List, String, String, Integer, Integer)} for the older versions of the Keycloak server
     */
    @GET
    @Path("admin-events")
    @Produces(MediaType.APPLICATION_JSON)
    List<AdminEventRepresentation> getAdminEvents(@QueryParam("operationTypes") List<String> operationTypes, @QueryParam("authRealm") String authRealm, @QueryParam("authClient") String authClient,
            @QueryParam("authUser") String authUser, @QueryParam("authIpAddress") String authIpAddress,
            @QueryParam("resourcePath") String resourcePath, @QueryParam("resourceTypes") List<String> resourceTypes, @QueryParam("dateFrom") long dateFrom,
            @QueryParam("dateTo") long dateTo, @QueryParam("first") Integer firstResult,
            @QueryParam("max") Integer maxResults, @QueryParam("direction") String direction);

    /** 获取领域事件监听与存储配置。 */
    @GET
    @Path("events/config")
    @Produces(MediaType.APPLICATION_JSON)
    RealmEventsConfigRepresentation getRealmEventsConfig();

    /** 更新领域事件监听与存储配置。 */
    @PUT
    @Path("events/config")
    @Consumes(MediaType.APPLICATION_JSON)
    void updateRealmEventsConfig(RealmEventsConfigRepresentation rep);

    /** 按层级路径查找用户组。 */
    @GET
    @Path("group-by-path/{path: .*}")
    @Produces(MediaType.APPLICATION_JSON)
    GroupRepresentation getGroupByPath(@PathParam("path") String path);

    /** 列出新用户自动加入的默认组。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("default-groups")
    List<GroupRepresentation> getDefaultGroups();

    /** 将组添加为新用户的默认组。 */
    @PUT
    @Path("default-groups/{groupId}")
    void addDefaultGroup(@PathParam("groupId") String groupId);

    /** 从默认组列表中移除指定组。 */
    @DELETE
    @Path("default-groups/{groupId}")
    void removeDefaultGroup(@PathParam("groupId") String groupId);

    /** 获取身份提供者集合管理子资源。 */
    @Path("identity-provider")
    IdentityProvidersResource identityProviders();

    /** 删除当前领域。 */
    @DELETE
    void remove();

    /** 获取各客户端的活跃会话统计信息。 */
    @Path("client-session-stats")
    @GET
    List<Map<String, String>> getClientSessionStats();

    /** 获取客户端初始访问令牌管理子资源。 */
    @Path("clients-initial-access")
    ClientInitialAccessResource clientInitialAccess();

    /** 获取客户端注册策略管理子资源。 */
    @Path("client-registration-policy")
    ClientRegistrationPolicyResource clientRegistrationPolicy();

    /** 部分导入领域配置（客户端、用户、组等）。 */
    @Path("partialImport")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response partialImport(PartialImportRepresentation rep);

    /** 部分导出领域配置，可选择是否包含组/角色与客户端。 */
    @Path("partial-export")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    RealmRepresentation partialExport(@QueryParam("exportGroupsAndRoles") Boolean exportGroupsAndRoles,
                                             @QueryParam("exportClients") Boolean exportClients);
    /** 获取认证流程管理子资源。 */
    @Path("authentication")
    @Consumes(MediaType.APPLICATION_JSON)
    AuthenticationManagementResource flows();

    /** 获取暴力破解检测管理子资源。 */
    @Path("attack-detection")
    AttackDetectionResource attackDetection();

    /** 测试 LDAP 连接（表单参数方式，已弃用）。 */
    @Path("testLDAPConnection")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Deprecated
    Response testLDAPConnection(@FormParam("action") String action, @FormParam("connectionUrl") String connectionUrl,
                                @FormParam("bindDn") String bindDn, @FormParam("bindCredential") String bindCredential,
                                @FormParam("useTruststoreSpi") String useTruststoreSpi, @FormParam("connectionTimeout") String connectionTimeout);

    /** 测试 LDAP 连接（JSON 配置方式）。 */
    @Path("testLDAPConnection")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response testLDAPConnection(TestLdapConnectionRepresentation config);

    /** 探测 LDAP 服务器支持的能力（如分页、同步等）。 */
    @POST
    @Path("ldap-server-capabilities")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
    List<LDAPCapabilityRepresentation> ldapServerCapabilities(TestLdapConnectionRepresentation config);

    /** 测试 SMTP 邮件服务器连接（表单参数方式，已弃用）。 */
    @Path("testSMTPConnection")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Deprecated
    Response testSMTPConnection(@FormParam("config") String config);

    /** 测试 SMTP 邮件服务器连接（JSON 配置方式）。 */
    @Path("testSMTPConnection")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response testSMTPConnection(Map<String, String> config);

    /** 清空领域缓存。 */
    @Path("clear-realm-cache")
    @POST
    void clearRealmCache();

    /** 清空用户缓存。 */
    @Path("clear-user-cache")
    @POST
    void clearUserCache();

    /** 清空密钥缓存。 */
    @Path("clear-keys-cache")
    @POST
    void clearKeysCache();

    /**
     * 清空 CRL 缓存（X509 认证所加载的证书吊销列表）。
     * @since Keycloak 26.2
     */
    @Path("clear-crl-cache")
    @POST
    void clearCrlCache();

    /** 向所有适配器推送令牌吊销通知。 */
    @Path("push-revocation")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    GlobalRequestResult pushRevocation();

    /** 注销领域内所有活跃用户会话。 */
    @Path("logout-all")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    GlobalRequestResult logoutAll();

    /**
     * 删除指定用户会话。
     *
     * @param sessionId 会话 ID
     * @param offline 是否为离线会话；自 Keycloak server 24.0.2 起可用，旧版服务器忽略此参数（默认 false）
     * @throws jakarta.ws.rs.NotFoundException 若用户会话不存在
     */
    @Path("sessions/{session}")
    @DELETE
    void deleteSession(@PathParam("session") String sessionId, @DefaultValue("false") @QueryParam("isOffline") boolean offline);

    /** 获取组件（Component）集合管理子资源。 */
    @Path("components")
    ComponentsResource components();

    /** 获取用户存储提供程序管理子资源。 */
    @Path("user-storage")
    UserStorageProviderResource userStorage();


    /** 获取领域密钥管理子资源。 */
    @Path("keys")
    KeyResource keys();

    /** 获取领域本地化文本管理子资源。 */
    @Path("localization")
    RealmLocalizationResource localization();

    /** 获取客户端策略（Client Policies）规则管理子资源。 */
    @Path("client-policies/policies")
    ClientPoliciesPoliciesResource clientPoliciesPoliciesResource();

    /** 获取客户端策略配置文件（Profiles）管理子资源。 */
    @Path("client-policies/profiles")
    ClientPoliciesProfilesResource clientPoliciesProfilesResource();

    /** 获取组织集合管理子资源。 */
    @Path("organizations")
    OrganizationsResource organizations();

    /** 获取客户端类型管理子资源。 */
    @Path("client-types")
    ClientTypesResource clientTypes();

    /** 获取工作流管理子资源。 */
    @Path("workflows")
    WorkflowsResource workflows();
}
