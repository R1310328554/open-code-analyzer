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
package org.keycloak.services.resources.admin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.keycloak.OAuthErrorException;
import org.keycloak.authorization.admin.AuthorizationService;
import org.keycloak.client.clienttype.ClientTypeException;
import org.keycloak.common.ClientConnection;
import org.keycloak.common.Profile;
import org.keycloak.common.util.Time;
import org.keycloak.events.Errors;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.ClientSecretConstants;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.ModelValidationException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.models.utils.RepresentationToModel;
import org.keycloak.protocol.ClientInstallationProvider;
import org.keycloak.protocol.LoginProtocol;
import org.keycloak.protocol.LoginProtocolFactory;
import org.keycloak.protocol.oidc.OIDCClientSecretConfigWrapper;
import org.keycloak.representations.adapters.action.GlobalRequestResult;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ClientScopeRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.ManagementPermissionReference;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.UserSessionRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.ErrorResponseException;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.context.AdminClientUnregisterContext;
import org.keycloak.services.clientpolicy.context.AdminClientUpdateContext;
import org.keycloak.services.clientpolicy.context.AdminClientUpdatedContext;
import org.keycloak.services.clientpolicy.context.AdminClientViewContext;
import org.keycloak.services.clientpolicy.context.ClientSecretRotationContext;
import org.keycloak.services.clientregistration.ClientRegistrationTokenUtils;
import org.keycloak.services.clientregistration.policy.RegistrationAuth;
import org.keycloak.services.managers.ClientManager;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.managers.ResourceAdminManager;
import org.keycloak.services.resources.KeycloakOpenAPI;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;
import org.keycloak.services.resources.admin.fgap.AdminPermissionManagement;
import org.keycloak.services.resources.admin.fgap.AdminPermissions;
import org.keycloak.utils.ProfileHelper;
import org.keycloak.utils.ReservedCharValidator;
import org.keycloak.validation.ValidationUtil;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.NoCache;


/**
 * 单个客户端 REST 资源。
 * <p>管理客户端 CRUD、密钥/证书、客户端范围、会话、集群节点、授权服务及细粒度权限。</p>
 *
 * @resource Clients
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@Extension(name = KeycloakOpenAPI.Profiles.ADMIN, value = "")
public class ClientResource {
    /** 日志记录器 */
    protected static final Logger logger = Logger.getLogger(ClientResource.class);
    /** 当前领域 */
    protected final RealmModel realm;
    /** 细粒度权限评估器 */
    private final AdminPermissionEvaluator auth;
    /** 管理事件构建器 */
    private final AdminEventBuilder adminEvent;
    /** 目标客户端 */
    protected final ClientModel client;
    /** Keycloak 会话 */
    protected final KeycloakSession session;

    /** 客户端连接信息 */
    protected final ClientConnection clientConnection;

    /** 构造单个客户端资源。
     * @param realm 当前领域
     * @param auth 权限评估器
     * @param clientModel 目标客户端
     * @param session Keycloak 会话
     * @param adminEvent 管理事件构建器
     */
    public ClientResource(RealmModel realm, AdminPermissionEvaluator auth, ClientModel clientModel, KeycloakSession session, AdminEventBuilder adminEvent) {
        this.realm = realm;
        this.auth = auth;
        this.client = clientModel;
        this.session = session;
        this.adminEvent = adminEvent.resource(ResourceType.CLIENT);
        this.clientConnection = session.getContext().getConnection();
    }

    /** 协议映射器子资源 */
    @Path("protocol-mappers")
    public ProtocolMappersResource getProtocolMappers() {
        AdminPermissionEvaluator.RequirePermissionCheck manageCheck = () -> auth.clients().requireManage(client);
        AdminPermissionEvaluator.RequirePermissionCheck viewCheck = () -> auth.clients().requireView(client);
        return new ProtocolMappersResource(session, client, auth, adminEvent, manageCheck, viewCheck);
    }

    /**
     * 更新客户端配置（触发客户端策略与校验）。
     * @param rep 客户端表示
     * @return 204 No Content
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENTS)
    @Operation( summary = "Update the client")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "No Content"),
        @APIResponse(responseCode = "400", description = "Bad Request"),
        @APIResponse(responseCode = "409", description = "Conflict")
    })
    public Response update(final ClientRepresentation rep) {
        auth.clients().requireConfigure(client);

        try {
            session.setAttribute(ClientSecretConstants.CLIENT_SECRET_ROTATION_ENABLED,Boolean.FALSE);
            session.clientPolicy().triggerOnEvent(new AdminClientUpdateContext(rep, client, auth.adminAuth()));

            updateClientFromRep(rep, client, session);

            ValidationUtil.validateClient(session, client, false, r -> {
                session.getTransactionManager().setRollbackOnly();
                throw new ErrorResponseException(
                        Errors.INVALID_INPUT,
                        r.getAllLocalizedErrorsAsString(AdminRoot.getMessages(session, realm, auth.adminAuth().getToken().getLocale())),
                        Response.Status.BAD_REQUEST);
            });

            session.clientPolicy().triggerOnEvent(new AdminClientUpdatedContext(rep, client, auth.adminAuth()));

            if (!(boolean) session.getAttribute(ClientSecretConstants.CLIENT_SECRET_ROTATION_ENABLED)){
                logger.debugv("Removing the previous rotation info for client {0}{1}, if there is",client.getClientId(),client.getName());
                OIDCClientSecretConfigWrapper.fromClientModel(client).removeClientSecretRotationInfo();
            }
            session.removeAttribute(ClientSecretConstants.CLIENT_SECRET_ROTATION_ENABLED);

            adminEvent.operation(OperationType.UPDATE).resourcePath(session.getContext().getUri()).representation(rep).success();
            return Response.noContent().build();
        } catch (ModelDuplicateException e) {
            throw ErrorResponse.exists("Client already exists");
        } catch (ClientTypeException cte) {
            throw ErrorResponse.error(cte.getMessage(), cte.getParameters(), Response.Status.BAD_REQUEST);
        } catch (ClientPolicyException cpe) {
            throw new ErrorResponseException(cpe.getError(), cpe.getErrorDetail(), Response.Status.BAD_REQUEST);
        } catch (ModelValidationException e) {
            throw new ErrorResponseException(Errors.INVALID_INPUT, e.getMessage(), Response.Status.BAD_REQUEST);
        }
    }

    /**
     * 获取客户端表示（含 access 与客户端范围）。
     * @return 客户端表示
     */
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENTS)
    @Operation( summary = "Get representation of the client")
    public ClientRepresentation getClient() {
        viewClientModel();

        ClientRepresentation representation = ModelToRepresentation.toRepresentation(client, session);

        if (!auth.clients().canViewClientScopes()) {
            representation.setDefaultClientScopes(Collections.emptyList());
            representation.setOptionalClientScopes(Collections.emptyList());
        }

        representation.setAccess(auth.clients().getAccess(client));

        return representation;
    }

    /** 校验查看权限并触发 AdminClientView 客户端策略事件 */
    public ClientModel viewClientModel() {
        auth.clients().requireView(client);

        try {
            session.clientPolicy().triggerOnEvent(new AdminClientViewContext(client, auth.adminAuth()));
        } catch (ClientPolicyException cpe) {
            throw new ErrorResponseException(cpe.getError(), cpe.getErrorDetail(), Response.Status.BAD_REQUEST);
        }

        return client;
    }

    /**
     * 客户端属性证书子资源（签名/加密等）。
     * @param attributePrefix 属性前缀
     * @return {@link ClientAttributeCertificateResource}
     */
    @Path("certificates/{attr}")
    public ClientAttributeCertificateResource getCertficateResource(@PathParam("attr") String attributePrefix) {
        return new ClientAttributeCertificateResource(auth, client, session, attributePrefix, adminEvent);
    }

    /** 按安装提供者 ID 生成客户端适配器安装配置 */
    @GET
    @NoCache
    @Path("installation/providers/{providerId}")
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENTS)
    @Operation()
    public Response getInstallationProvider(@PathParam("providerId") String providerId) {
        auth.clients().requireView(client);

        ClientInstallationProvider provider = session.getProvider(ClientInstallationProvider.class, providerId);
        if (provider == null) throw new NotFoundException("Unknown Provider");
        return provider.generateInstallation(session, realm, client, session.getContext().getUri().getBaseUri());
    }

    /** 删除客户端（触发 AdminClientUnregister 策略） */
    @DELETE
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENTS)
    @Operation( summary = "Delete the client")
    public void deleteClient() {
        auth.clients().requireManage(client);

        if (client == null) {
            throw new NotFoundException("Could not find client");
        }

        ClientRepresentation clientRepresentation = new ClientRepresentation();
        clientRepresentation.setId(client.getId());
        clientRepresentation.setClientId(client.getClientId());

        try {
            session.clientPolicy().triggerOnEvent(new AdminClientUnregisterContext(client, auth.adminAuth()));
        } catch (ClientPolicyException cpe) {
            throw new ErrorResponseException(cpe.getError(), cpe.getErrorDetail(), Response.Status.BAD_REQUEST);
        }

        if (new ClientManager(new RealmManager(session)).removeClient(realm, client)) {
            adminEvent.operation(OperationType.DELETE).representation(clientRepresentation).resourcePath(session.getContext().getUri()).success();
        }
        else {
            throw new ErrorResponseException(OAuthErrorException.INVALID_REQUEST, "Could not delete client",
                    Response.Status.BAD_REQUEST);
        }
    }


    /**
     * 重新生成客户端密钥。
     * @return 密钥凭证表示
     */
    @Path("client-secret")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENTS)
    @Operation( summary = "Generate a new secret for the client")
    public CredentialRepresentation regenerateSecret() {
        try{
            auth.clients().requireConfigure(client);

            logger.debug("regenerateSecret");
            session.setAttribute(ClientSecretConstants.CLIENT_SECRET_ROTATION_ENABLED,Boolean.FALSE);

            ClientRepresentation representation = ModelToRepresentation.toRepresentation(client, session);
            ClientSecretRotationContext secretRotationContext = new ClientSecretRotationContext(
                representation, client, client.getSecret(), auth.adminAuth());

            String secret = KeycloakModelUtils.generateSecret(client);

            session.clientPolicy().triggerOnEvent(secretRotationContext);

            CredentialRepresentation rep = new CredentialRepresentation();
            rep.setType(CredentialRepresentation.SECRET);
            rep.setValue(secret);

            if (!(boolean) session.getAttribute(ClientSecretConstants.CLIENT_SECRET_ROTATION_ENABLED)){
                logger.debugv("Removing the previous rotation info for client {0}{1}, if there is",client.getClientId(),client.getName());
                OIDCClientSecretConfigWrapper.fromClientModel(client).removeClientSecretRotationInfo();
            }

            adminEvent.operation(OperationType.ACTION).resourcePath(session.getContext().getUri()).representation(rep).success();
            session.removeAttribute(ClientSecretConstants.CLIENT_SECRET_ROTATION_ENABLED);
            rep.setValue(secret);
            return rep;
        } catch (ClientPolicyException cpe) {
            throw new ErrorResponseException(cpe.getError(), cpe.getErrorDetail(),
                Response.Status.BAD_REQUEST);
        }
    }

    /**
     * 重新生成客户端注册访问令牌。
     * @return 含 registrationAccessToken 的客户端表示
     */
    @Path("registration-access-token")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENTS)
    @Operation( summary = "Generate a new registration access token for the client")
    public ClientRepresentation regenerateRegistrationAccessToken() {
        auth.clients().requireManage(client);

        String token = ClientRegistrationTokenUtils.updateRegistrationAccessToken(session, realm, client, RegistrationAuth.AUTHENTICATED, null);

        ClientRepresentation rep = ModelToRepresentation.toRepresentation(client, session);
        rep.setRegistrationAccessToken(token);

        adminEvent.operation(OperationType.ACTION).resourcePath(session.getContext().getUri()).representation(rep).success();
        return rep;
    }

    /**
     * 获取当前客户端密钥。
     * @return 密钥凭证表示
     */
    @Path("client-secret")
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENTS)
    @Operation( summary = "Get the client secret")
    public CredentialRepresentation getClientSecret() {
        auth.clients().requireView(client);

        logger.debug("getClientSecret");
        UserCredentialModel model = UserCredentialModel.secret(client.getSecret());
        return ModelToRepresentation.toRepresentation(model);
    }

    /** 客户端作用域映射（角色→客户端）子资源。
     * @return {@link ScopeMappedResource}
     */
    @Path("scope-mappings")
    public ScopeMappedResource getScopeMappedResource() {
        AdminPermissionEvaluator.RequirePermissionCheck manageCheck = () -> auth.clients().requireManage(client);
        AdminPermissionEvaluator.RequirePermissionCheck viewCheck = () -> auth.clients().requireView(client);
        return new ScopeMappedResource(realm, auth, client, session, adminEvent, manageCheck, viewCheck);
    }

    /** 客户端角色容器子资源 */
    @Path("roles")
    public RoleContainerResource getRoleContainerResource() {
        return new RoleContainerResource(session, session.getContext().getUri(), realm, auth, client, adminEvent);
    }


    /** 获取默认客户端范围（仅 id 与 name）。
     * @return 范围表示流
     */
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Path("default-client-scopes")
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENTS)
    @Operation( summary = "Get default client scopes.  Only name and ids are returned.")
    public Stream<ClientScopeRepresentation> getDefaultClientScopes() {
        return getDefaultClientScopes(true);
    }

    /** 内部：获取默认或可选客户端范围流 */
    private Stream<ClientScopeRepresentation> getDefaultClientScopes(boolean defaultScope) {
        auth.clients().requireView(client);

        if (!auth.clients().canViewClientScopes()) {
            return Stream.empty();
        }

        return client.getClientScopes(defaultScope).values().stream().map(ClientResource::toRepresentation);
    }


    /** 将客户端范围添加为默认范围 */
    @PUT
    @NoCache
    @Path("default-client-scopes/{clientScopeId}")
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENTS)
    @Operation()
    public void addDefaultClientScope(@PathParam("clientScopeId") String clientScopeId) {
        addDefaultClientScope(clientScopeId,true);
    }

    private void addDefaultClientScope(String clientScopeId, boolean defaultScope) {
        auth.clients().requireManage(client);

        ClientScopeModel clientScope = realm.getClientScopeById(clientScopeId);
        if (clientScope == null) {
            throw new jakarta.ws.rs.NotFoundException("Client scope not found");
        }
        
        auth.clients().requireManage(clientScope);
        
        // 参数化范围须调用方显式传参（如 scope_name:value），不可作为默认范围自动包含
        // so they cannot be included automatically as default scopes. This restriction may be lifted in the future.
        if (defaultScope && clientScope.isParameterizedScope()) {
            throw new ErrorResponseException("invalid_request", "Can't assign a Parameterized Scope to a Client as a Default Scope", Response.Status.BAD_REQUEST);
        }

        validateClientScopeAssignment(session, clientScope, defaultScope, realm);

        client.addClientScope(clientScope, defaultScope);

        adminEvent.operation(OperationType.CREATE).resource(ResourceType.CLIENT_SCOPE_CLIENT_MAPPING).resourcePath(session.getContext().getUri()).success();
    }


    /** 移除默认客户端范围 */
    @DELETE
    @NoCache
    @Path("default-client-scopes/{clientScopeId}")
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENTS)
    @Operation()
    public void removeDefaultClientScope(@PathParam("clientScopeId") String clientScopeId) {
        auth.clients().requireManage(client);

        ClientScopeModel clientScope = realm.getClientScopeById(clientScopeId);
        if (clientScope == null) {
            throw new jakarta.ws.rs.NotFoundException("Client scope not found");
        }
        
        auth.clients().requireManage(clientScope);
        
        client.removeClientScope(clientScope);

        adminEvent.operation(OperationType.DELETE).resource(ResourceType.CLIENT_SCOPE_CLIENT_MAPPING).resourcePath(session.getContext().getUri()).success();
    }


    /** 获取可选客户端范围（仅 id 与 name）。
     * @return 范围表示流
     */
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Path("optional-client-scopes")
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENTS)
    @Operation( summary = "Get optional client scopes.  Only name and ids are returned.")
    public Stream<ClientScopeRepresentation> getOptionalClientScopes() {
        return getDefaultClientScopes(false);
    }

    /** 添加可选客户端范围 */
    @PUT
    @NoCache
    @Path("optional-client-scopes/{clientScopeId}")
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENTS)
    @Operation()
    public void addOptionalClientScope(@PathParam("clientScopeId") String clientScopeId) {
        addDefaultClientScope(clientScopeId, false);
    }

    /** 移除可选客户端范围 */
    @DELETE
    @NoCache
    @Path("optional-client-scopes/{clientScopeId}")
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENTS)
    @Operation()
    public void removeOptionalClientScope(@PathParam("clientScopeId") String clientScopeId) {
        removeDefaultClientScope(clientScopeId);
    }

    /** 客户端范围/token 评估子资源 */
    @Path("evaluate-scopes")
    public ClientScopeEvaluateResource clientScopeEvaluateResource() {
        return new ClientScopeEvaluateResource(session, session.getContext().getUri(), realm, auth, client, clientConnection);
    }

    /**
     * 获取客户端服务账号对应用户。
     * @return 用户表示
     */
    @Path("service-account-user")
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENTS)
    @Operation( summary = "Get a user dedicated to the service account")
    public UserRepresentation getServiceAccountUser() {
        auth.clients().requireView(client);

        UserModel user = new ClientManager(new RealmManager(session)).getServiceAccountUser(client)
                .orElseThrow(() -> new BadRequestException("Service account not enabled for the client '" + client.getClientId() + "'"));

        return ModelToRepresentation.toRepresentation(session, realm, user);
    }

    /** 向客户端 admin URL 推送吊销策略 */
    @Path("push-revocation")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENTS)
    @Operation( summary = "Push the client's revocation policy to its admin URL If the client has an admin URL, push revocation policy to it.")
    public GlobalRequestResult pushRevocation() {
        auth.clients().requireConfigure(client);

        adminEvent.operation(OperationType.ACTION).resourcePath(session.getContext().getUri()).resource(ResourceType.CLIENT).success();
        return new ResourceAdminManager(session).pushClientRevocationPolicy(realm, client);

    }

    /**
     * 获取客户端活跃用户会话数量。
     * @return {"count": number}
     */
    @Path("session-count")
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENTS)
    @Operation( summary = "Get application session count Returns a number of user sessions associated with this client { \"count\": number }")
    public Map<String, Long> getApplicationSessionCount() {
        auth.clients().requireView(client);

        Map<String, Long> map = new HashMap<>();
        map.put("count", session.sessions().getActiveUserSessions(client.getRealm(), client));
        return map;
    }

    /**
     * 分页获取客户端关联的在线用户会话。
     * @param firstResult 分页偏移
     * @param maxResults 最大条数
     * @return 用户会话表示流
     */
    @Path("user-sessions")
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENTS)
    @Operation( summary = "Get user sessions for client. Returns a list of user sessions associated with this client.\n")
    public Stream<UserSessionRepresentation> getUserSessions(@Parameter(description = "Paging offset") @QueryParam("first") Integer firstResult, @Parameter(description = "Maximum results size.") @QueryParam("max") @DefaultValue(Constants.DEFAULT_MAX_RESULTS_STR) Integer maxResults) {
        auth.clients().requireView(client);
        return session.sessions()
                .readOnlyStreamUserSessions(client.getRealm(), client, computeFirstResult(firstResult), computeMaxResults(maxResults))
                .map(ModelToRepresentation::toRepresentation);
    }

    /**
     * 获取客户端离线会话数量。
     * @return {"count": number}
     */
    @Path("offline-session-count")
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENTS)
    @Operation( summary = "Get application offline session count Returns a number of offline user sessions associated with this client { \"count\": number }")
    public Map<String, Long> getOfflineSessionCount() {
        auth.clients().requireView(client);

        Map<String, Long> map = new HashMap<>();
        map.put("count", session.sessions().getOfflineSessionsCount(client.getRealm(), client));
        return map;
    }

    /**
     * 分页获取客户端离线用户会话。
     * @param firstResult 分页偏移
     * @param maxResults 最大条数
     * @return 用户会话表示流
     */
    @Path("offline-sessions")
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENTS)
    @Operation( summary = "Get offline sessions for client. Returns a list of offline user sessions associated with this client")
    public Stream<UserSessionRepresentation> getOfflineUserSessions(@Parameter(description = "Paging offset") @QueryParam("first") Integer firstResult, @Parameter(description = "Maximum results size.") @QueryParam("max") @DefaultValue(Constants.DEFAULT_MAX_RESULTS_STR) Integer maxResults) {
        auth.clients().requireView(client);
        return session.sessions()
                .readOnlyStreamOfflineUserSessions(client.getRealm(), client, computeFirstResult(firstResult), computeMaxResults(maxResults))
                .map(this::toUserSessionRepresentation);
    }

    /**
     * 手动注册客户端集群节点（通常由适配器自动注册）。
     * @param formParams 含 node 字段的表单
     */
    @Path("nodes")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENTS)
    @Operation( summary = "Register a cluster node with the client Manually register cluster node to this client - usually it’s not needed to call this directly as adapter should handle by sending registration request to Keycloak")
    @APIResponse(responseCode = "204", description = "No Content")
    public void registerNode(Map<String, String> formParams) {
        auth.clients().requireConfigure(client);

        String node = formParams.get("node");
        if (node == null) {
            throw new BadRequestException("Node not found in params");
        }

        ReservedCharValidator.validate(node);

        logger.debugf("Register node: %s", node);
        client.registerNode(node, Time.currentTime());
        adminEvent.operation(OperationType.CREATE).resource(ResourceType.CLUSTER_NODE).resourcePath(session.getContext().getUri(), node).success();
    }

    /**
     * 注销客户端集群节点。
     * @param node 节点标识
     */
    @Path("nodes/{node}")
    @DELETE
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENTS)
    @Operation( summary = "Unregister a cluster node from the client")
    public void unregisterNode(final @PathParam("node") String node) {
        auth.clients().requireConfigure(client);

        logger.debugf("Unregister node: %s", node);

        Integer time = client.getRegisteredNodes().get(node);
        if (time == null) {
            throw new NotFoundException("Client does not have node ");
        }
        client.unregisterNode(node);
        adminEvent.operation(OperationType.DELETE).resource(ResourceType.CLUSTER_NODE).resourcePath(session.getContext().getUri()).success();
    }

    /**
     * 向所有已注册节点发送 ping 测试可用性。
     * @return 全局请求结果
     */
    @Path("test-nodes-available")
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENTS)
    @Operation( summary = "Test if registered cluster nodes are available Tests availability by sending 'ping' request to all cluster nodes.")
    public GlobalRequestResult testNodesAvailable() {
        auth.clients().requireConfigure(client);

        logger.debug("Test availability of cluster nodes");
        GlobalRequestResult result = new ResourceAdminManager(session).testNodesAvailability(realm, client);
        adminEvent.operation(OperationType.ACTION).resource(ResourceType.CLUSTER_NODE).resourcePath(session.getContext().getUri()).representation(result).success();
        return result;
    }

    /** 客户端授权服务子资源（需 AUTHORIZATION 特性） */
    @Path("/authz")
    public AuthorizationService authorization() {
        ProfileHelper.requireFeature(Profile.Feature.AUTHORIZATION);

        return new AuthorizationService(this.session, this.client, this.auth, adminEvent);
    }

    /**
     * Return object stating whether client Authorization permissions have been initialized or not and a reference
     *
     * @return
     */
    /** 获取客户端细粒度管理权限状态。
     * @return 管理权限引用
     */
    @Path("management/permissions")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENTS)
    @Operation( summary = "Return object stating whether client Authorization permissions have been initialized or not and a reference")
    public ManagementPermissionReference getManagementPermissions() {
        ProfileHelper.requireFeature(Profile.Feature.ADMIN_FINE_GRAINED_AUTHZ);
        auth.roles().requireView(client);

        AdminPermissionManagement permissions = AdminPermissions.management(session, realm);
        if (!permissions.clients().isPermissionsEnabled(client)) {
            return new ManagementPermissionReference();
        }
        return toMgmtRef(client, permissions);
    }

    /** 构建已启用的客户端管理权限引用 */
    private ManagementPermissionReference toMgmtRef(ClientModel client, AdminPermissionManagement permissions) {
        ManagementPermissionReference ref = new ManagementPermissionReference();
        ref.setEnabled(true);
        ref.setResource(permissions.clients().resource(client).getId());
        ref.setScopePermissions(permissions.clients().getPermissions(client));
        return ref;
    }


    /**
     * Return object stating whether client Authorization permissions have been initialized or not and a reference
     *
     *
     * @return initialized manage permissions reference
     */
    /** 启用或禁用客户端细粒度管理权限。
     * @param ref 管理权限引用
     * @return 更新后的权限引用
     */
    @Path("management/permissions")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENTS)
    @Operation( summary = "Return object stating whether client Authorization permissions have been initialized or not and a reference")
    public ManagementPermissionReference setManagementPermissionsEnabled(ManagementPermissionReference ref) {
        ProfileHelper.requireFeature(Profile.Feature.ADMIN_FINE_GRAINED_AUTHZ);
        auth.clients().requireManage(client);
        AdminPermissionManagement permissions = AdminPermissions.management(session, realm);
        permissions.clients().setPermissionsEnabled(client, ref.isEnabled());
        if (ref.isEnabled()) {
            return toMgmtRef(client, permissions);
        } else {
            return new ManagementPermissionReference();
        }
    }

    /**
     * 作废客户端轮换中的旧密钥。
     * @return 204 No Content
     */
    @Path("client-secret/rotated")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENTS)
    @Operation( summary = "Invalidate the rotated secret for the client")
    public Response invalidateRotatedSecret() {
        try{
            auth.clients().requireConfigure(client);

            logger.debug("delete rotated secret");

            OIDCClientSecretConfigWrapper wrapper = OIDCClientSecretConfigWrapper.fromClientModel(client);

            CredentialRepresentation rep = new CredentialRepresentation();
            rep.setType(CredentialRepresentation.SECRET);
            rep.setValue(wrapper.getClientRotatedSecret(session));

            adminEvent.operation(OperationType.DELETE).resourcePath(session.getContext().getUri()).representation(rep).success();

            wrapper.removeClientSecretRotated();

            return Response.noContent().build();
        } catch (RuntimeException rte) {
            throw new ErrorResponseException(rte.getCause().getMessage(), rte.getMessage(),
                Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取轮换中的客户端密钥。
     * @return 密钥凭证表示
     */
    @Path("client-secret/rotated")
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENTS)
    @Operation( summary = "Get the rotated client secret")
    public CredentialRepresentation getClientRotatedSecret() {
        auth.clients().requireView(client);

        logger.debug("getClientRotatedSecret");
        OIDCClientSecretConfigWrapper wrapper = OIDCClientSecretConfigWrapper.fromClientModel(client);
        if (!wrapper.hasRotatedSecret())
            throw new NotFoundException("Client does not have a rotated secret");
        else {
            UserCredentialModel model = UserCredentialModel.secret(wrapper.getClientRotatedSecret(session));
            return ModelToRepresentation.toRepresentation(model);
        }
    }

    /** 将客户端表示同步到模型（含服务账号、clientId 变更、授权设置） */
    private void updateClientFromRep(ClientRepresentation rep, ClientModel client, KeycloakSession session) throws ModelDuplicateException {
        updateClientServiceAccount(session, client, rep.isServiceAccountsEnabled());

        if (rep.getClientId() != null && !rep.getClientId().equals(client.getClientId())) {
            new ClientManager(new RealmManager(session)).clientIdChanged(client, rep);
        }

        if (rep.isFullScopeAllowed() != null && rep.isFullScopeAllowed() != client.isFullScopeAllowed()) {
            auth.clients().requireManage(client);
        }

        if ((rep.isBearerOnly() != null && rep.isBearerOnly()) || (rep.isPublicClient() != null && rep.isPublicClient())) {
            rep.setAuthorizationServicesEnabled(false);
        }

        RepresentationToModel.updateClient(rep, client, session);
        RepresentationToModel.updateClientProtocolMappers(rep, client);
        updateAuthorizationSettings(rep);
    }

    /**
     * 校验客户端范围分配（委托登录协议工厂）。
     * @param session Keycloak 会话
     * @param clientScope 待分配范围
     * @param defaultScope 是否为默认范围
     * @param realm 当前领域
     */
    public static void validateClientScopeAssignment(KeycloakSession session, ClientScopeModel clientScope,
                                                     boolean defaultScope, RealmModel realm) {
        LoginProtocolFactory loginProtocolFactory = (LoginProtocolFactory) session.getKeycloakSessionFactory()
                .getProviderFactory(LoginProtocol.class, clientScope.getProtocol());
        if (loginProtocolFactory != null) {
            loginProtocolFactory.validateClientScopeAssignment(session, clientScope, defaultScope, realm);
        }
    }

    /** 更新客户端服务账号启用状态 */
    public static void updateClientServiceAccount(KeycloakSession session, ClientModel client, Boolean isServiceAccountEnabled) {
        ClientManager.updateClientServiceAccount(session, client, isServiceAccountEnabled);
    }

    /** 根据表示启用或禁用客户端授权服务 */
    private void updateAuthorizationSettings(ClientRepresentation rep) {
        if (Profile.isFeatureEnabled(Profile.Feature.AUTHORIZATION)) {
            if (Boolean.TRUE.equals(rep.getAuthorizationServicesEnabled())) {
                authorization().enable(false);
            } else {
                authorization().disable();
            }
        }
    }

    /**
     * 将 {@link UserSessionModel} 转为表示，并用客户端会话时间戳更新 lastAccess。
     * @param userSession 用户会话模型
     * @return 用户会话表示
     */
    private UserSessionRepresentation toUserSessionRepresentation(final UserSessionModel userSession) {
        UserSessionRepresentation rep = ModelToRepresentation.toRepresentation(userSession);

        // 用客户端会话时间戳更新 lastSessionRefresh
        var clientSession = userSession.getAuthenticatedClientSessionByClient(client.getClientId());
        if (clientSession != null) {
            rep.setLastAccess(Time.toMillis(clientSession.getTimestamp()));
        }
        return rep;
    }

    /** 客户端范围简要表示（仅 id/name） */
    private static ClientScopeRepresentation toRepresentation(ClientScopeModel clientScopeModel) {
        ClientScopeRepresentation rep = new ClientScopeRepresentation();
        rep.setId(clientScopeModel.getId());
        rep.setName(clientScopeModel.getName());
        return rep;
    }

    /** 计算分页起始索引 */
    private static int computeFirstResult(Integer firstResult) {
        return Objects.requireNonNullElse(firstResult, -1);
    }

    /** 计算分页最大条数 */
    private static int computeMaxResults(Integer maxResults) {
        return Objects.requireNonNullElse(maxResults, Constants.DEFAULT_MAX_RESULTS);
    }
}
