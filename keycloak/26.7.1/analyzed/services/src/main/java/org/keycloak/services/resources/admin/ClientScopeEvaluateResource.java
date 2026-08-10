/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.keycloak.authentication.actiontoken.TokenUtils;
import org.keycloak.common.ClientConnection;
import org.keycloak.common.util.CollectionUtil;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleContainerModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.protocol.saml.JaxrsSAML2BindingBuilder;
import org.keycloak.protocol.saml.SamlProtocol;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.IDToken;
import org.keycloak.saml.common.util.TransformerUtil;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.AuthenticationSessionManager;
import org.keycloak.services.managers.UserSessionManager;
import org.keycloak.services.resources.KeycloakOpenAPI;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;
import org.keycloak.services.util.ResolveRelative;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
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
import org.w3c.dom.Document;

import static org.keycloak.protocol.ProtocolMapperUtils.isEnabled;

/**
 * 客户端范围评估 REST 资源。
 * <p>模拟令牌签发：预览协议映射器、生成示例 UserInfo/ID Token/Access Token/SAML 响应。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@Extension(name = KeycloakOpenAPI.Profiles.ADMIN, value = "")
public class ClientScopeEvaluateResource {

    /** 日志记录器 */
    protected static final Logger logger = Logger.getLogger(ClientScopeEvaluateResource.class);

    /** 当前领域 */
    private final RealmModel realm;
    /** 被评估的客户端 */
    private final ClientModel client;
    /** 细粒度权限评估器 */
    private final AdminPermissionEvaluator auth;

    /** 请求 URI 信息 */
    private final UriInfo uriInfo;
    /** Keycloak 会话 */
    private final KeycloakSession session;
    /** 客户端连接信息 */
    private final ClientConnection clientConnection;

    /** 构造客户端范围评估资源。
     * @param session Keycloak 会话
     * @param uriInfo URI 信息
     * @param realm 当前领域
     * @param auth 权限评估器
     * @param client 被评估客户端
     * @param clientConnection 客户端连接
     */
    public ClientScopeEvaluateResource(KeycloakSession session, UriInfo uriInfo, RealmModel realm, AdminPermissionEvaluator auth,
                                       ClientModel client, ClientConnection clientConnection) {
        this.uriInfo = uriInfo;
        this.realm = realm;
        this.client = client;
        this.auth = auth;
        this.session = session;
        this.clientConnection = clientConnection;
    }

    /**
     * 作用域映射评估子资源。
     * @param scopeParam OIDC scope 参数
     * @param roleContainerId 领域名称或客户端 UUID
     * @return {@link ClientScopeEvaluateScopeMappingsResource}
     */
    @Path("scope-mappings/{roleContainerId}")
    public ClientScopeEvaluateScopeMappingsResource scopeMappings(@QueryParam("scope") String scopeParam, @Parameter(description = "either realm name OR client UUID") @PathParam("roleContainerId") String roleContainerId) {
        auth.clients().requireView(client);

        if (roleContainerId == null) {
            throw new NotFoundException("No roleContainerId provided");
        }

        RoleContainerModel roleContainer = roleContainerId.equals(realm.getName()) ? realm : realm.getClientById(roleContainerId);
        if (roleContainer == null) {
            throw new NotFoundException("Role Container not found");
        }

        return new ClientScopeEvaluateScopeMappingsResource(session, roleContainer, auth, client, scopeParam);
    }

    /**
     * 列出签发令牌时将生效的全部协议映射器（客户端直接 + 关联客户端范围）。
     * @return 映射器评估表示流
     */
    @GET
    @Path("protocol-mappers")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENTS)
    @Operation(summary = "Return list of all protocol mappers, which will be used when generating tokens issued for particular client.",
            description = "This means protocol mappers assigned to this client directly and protocol mappers assigned to all client scopes of this client.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "", content = @Content(schema = @Schema(implementation = ProtocolMapperEvaluationRepresentation.class, type = SchemaType.ARRAY))),
            @APIResponse(responseCode = "403", description = "Forbidden")
    })
    public Stream<ProtocolMapperEvaluationRepresentation> getGrantedProtocolMappers(@QueryParam("scope") String scopeParam) {
        auth.clients().requireView(client);

        return TokenManager.getRequestedClientScopes(session, scopeParam, client, null)
                .flatMap(mapperContainer -> mapperContainer.getProtocolMappersStream()
                        .filter(current -> isEnabled(session, current) && Objects.equals(current.getProtocol(), client.getProtocol()))
                        .map(current -> toProtocolMapperEvaluationRepresentation(current, mapperContainer)));
    }

    /** 构建协议映射器评估表示（标注来源容器类型） */
    private ProtocolMapperEvaluationRepresentation toProtocolMapperEvaluationRepresentation(ProtocolMapperModel mapper,
                                                                                            ClientScopeModel mapperContainer) {
        ProtocolMapperEvaluationRepresentation rep = new ProtocolMapperEvaluationRepresentation();
        rep.setMapperId(mapper.getId());
        rep.setMapperName(mapper.getName());
        rep.setProtocolMapper(mapper.getProtocolMapper());

        if (mapperContainer.getId().equals(client.getId())) {
            // 映射器直接挂在客户端上
            rep.setContainerId(client.getId());
            rep.setContainerName("");
            rep.setContainerType("client");
        } else {
            ClientScopeModel clientScope = mapperContainer;
            rep.setContainerId(clientScope.getId());
            rep.setContainerName(clientScope.getName());
            rep.setContainerType("client-scope");
        }
        return rep;
    }

    /**
     * 生成示例 UserInfo JSON 载荷。
     * @param scopeParam scope 参数
     * @param userId 用户 ID
     * @return UserInfo 声明映射
     */
    @GET
    @Path("generate-example-userinfo")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENTS)
    @Operation(summary = "Create JSON with payload of example user info")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "", content = @Content(schema = @Schema(implementation = Map.class))),
            @APIResponse(responseCode = "403", description = "Forbidden")
    })
    public Map<String, Object> generateExampleUserinfo(@QueryParam("scope") String scopeParam, @QueryParam("userId") String userId) {
        auth.clients().requireView(client);

        UserModel user = getUserModel(userId);

        logger.debugf("generateExampleUserinfo invoked. User: %s", user.getUsername());

        return sessionAware(OIDCLoginProtocol.LOGIN_PROTOCOL, user, scopeParam, "", (userSession, clientSessionCtx, audienceClients, authSession) -> {
            AccessToken userInfo = new AccessToken();
            TokenManager tokenManager = new TokenManager();

            userInfo = tokenManager.transformUserInfoAccessToken(session, userInfo, userSession, clientSessionCtx);
            return tokenManager.generateUserInfoClaims(userInfo, user);
        });
    }

    /**
     * 生成示例 ID Token。
     * @param scopeParam scope 参数
     * @param userId 用户 ID
     * @param audience 目标受众客户端 ID
     * @return ID Token
     */
    @GET
    @Path("generate-example-id-token")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENTS)
    @Operation(summary = "Create JSON with payload of example id token")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "", content = @Content(schema = @Schema(implementation = IDToken.class))),
            @APIResponse(responseCode = "403", description = "Forbidden"),
            @APIResponse(responseCode = "404", description = "Not Found")
    })
    public IDToken generateExampleIdToken(@QueryParam("scope") String scopeParam, @QueryParam("userId") String userId, @QueryParam("audience") String audience) {
        auth.clients().requireView(client);

        UserModel user = getUserModel(userId);

        logger.debugf("generateExampleIdToken invoked. User: %s, Scope param: %s, Target Audience: %s", user.getUsername(), scopeParam);

        return sessionAware(OIDCLoginProtocol.LOGIN_PROTOCOL, user, scopeParam, audience, (userSession, clientSessionCtx, audienceClients, authSession) ->
        {
            TokenManager tokenManager = new TokenManager();
            TokenManager.AccessTokenResponseBuilder response = tokenManager.responseBuilder(realm, client, null, session, userSession, clientSessionCtx)
                    .generateAccessToken().generateIDToken();
            IDToken idToken = response.getIdToken();

            // 取 access token 校验 audience
            AccessToken accessToken = response.getAccessToken();
            validateAudience(accessToken, audienceClients);

            return idToken;
        });
    }

    /**
     * 生成示例 Access Token。
     * @param scopeParam scope 参数
     * @param userId 用户 ID
     * @param audience 目标受众
     * @return Access Token
     */
    @GET
    @Path("generate-example-access-token")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENTS)
    @Operation(summary = "Create JSON with payload of example access token")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "", content = @Content(schema = @Schema(implementation = AccessToken.class))),
            @APIResponse(responseCode = "403", description = "Forbidden"),
            @APIResponse(responseCode = "404", description = "Not Found")
    })
    public AccessToken generateExampleAccessToken(@QueryParam("scope") String scopeParam, @QueryParam("userId") String userId, @QueryParam("audience") String audience) {
        auth.clients().requireView(client);

        UserModel user = getUserModel(userId);

        logger.debugf("generateExampleAccessToken invoked. User: %s, Scope param: %s, Target Audience: %s", user.getUsername(), scopeParam, audience);

        return sessionAware(OIDCLoginProtocol.LOGIN_PROTOCOL, user, scopeParam, audience, (userSession, clientSessionCtx, audienceClients, authSession) ->
        {
            TokenManager tokenManager = new TokenManager();
            AccessToken accessToken =  tokenManager.responseBuilder(realm, client, null, session, userSession, clientSessionCtx)
                    .generateAccessToken().getAccessToken();
            validateAudience(accessToken, audienceClients);
            return accessToken;
        });
    }

    /**
     * 为指定用户与当前客户端生成示例 SAML 响应（格式化 XML）。
     * @param scopeParam scope 参数
     * @param userId 用户 ID
     * @param audience 受众客户端 ID
     * @return SAML 示例响应
     */
    @GET
    @Path("generate-example-saml-response")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENTS)
    @Operation( summary = "Create JSON with an example SAML response as payload")
    public SamlExampleResponse generateExampleSamlResponse(@QueryParam("scope") String scopeParam, @QueryParam("userId") String userId, @QueryParam("audience") String audience) {
        auth.clients().requireView(client);

        UserModel user = getUserModel(userId);

        logger.debugf("generateExampleSamlResponse invoked. User: %s, Scope param: %s", user.getUsername(), scopeParam);

        if (audience == null) {
            // 未指定 audience 时使用当前客户端
            audience = client.getClientId();
        }

        return sessionAware(SamlProtocol.LOGIN_PROTOCOL, user, scopeParam, audience, (userSession, clientSessionCtx, audienceClients, authSession) ->
        {
            // 在绑定编码前捕获 SAML 文档，避免 deflate/base64 往返并兼容各绑定/签名配置
            // so we avoid the deflate/base64/URL round-trip and work regardless of the client's binding or signing config.
            AtomicReference<Document> capturedDocument = new AtomicReference<>();

            SamlProtocol samlProtocol = new SamlProtocol() {
                @Override
                protected Response buildAuthenticatedResponse(AuthenticatedClientSessionModel clientSession, String redirectUri,
                                                              Document samlDocument, JaxrsSAML2BindingBuilder bindingBuilder) {
                    capturedDocument.set(samlDocument);
                    return Response.ok().build(); // 丢弃响应，仅需文档
                }
            };
            samlProtocol.setSession(session);
            samlProtocol.setRealm(realm);
            samlProtocol.setUriInfo(uriInfo);

            String baseUrl = ResolveRelative.resolveRelativeUri(session, client.getRootUrl(), client.getBaseUrl());
            authSession.setRedirectUri(baseUrl);

            try (Response ignored = samlProtocol.authenticated(authSession, userSession, clientSessionCtx)) {
                Document samlDocument = capturedDocument.get();
                if (samlDocument == null) {
                    // 例如 NameID 无法解析或映射器失败
                    throw new NotFoundException("Could not generate a SAML response for the given user and client");
                }
                return new SamlExampleResponse(prettyPrintSamlResponseDocument(samlDocument));
            }
        });
    }

    /** 格式化 SAML 响应 XML 文档 */
    private static String prettyPrintSamlResponseDocument(Document document) {
        try {
            Transformer transformer = TransformerUtil.getTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            Writer out = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(out));
            return out.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 创建临时认证/用户会话并执行协议响应生成器 */
    private<R> R sessionAware(String protocol, UserModel user, String scopeParam, String audienceParam, ProtocolResponseGenerator<R> function) {
        AuthenticationSessionModel authSession = null;
        UserSessionModel userSession = null;
        AuthenticationSessionManager authSessionManager = new AuthenticationSessionManager(session);

        try {
            RootAuthenticationSessionModel rootAuthSession = authSessionManager.createAuthenticationSession(realm, false);
            authSession = rootAuthSession.createAuthenticationSession(client);

            authSession.setAuthenticatedUser(user);
            authSession.setProtocol(protocol);
            authSession.setClientNote(OIDCLoginProtocol.ISSUER, Urls.realmIssuer(uriInfo.getBaseUri(), realm.getName()));
            authSession.setClientNote(OIDCLoginProtocol.SCOPE_PARAM, scopeParam);

            userSession = new UserSessionManager(session).createUserSession(authSession.getParentSession().getId(), realm, user, user.getUsername(),
                    clientConnection.getRemoteHost(), "example-auth", false, null, null, UserSessionModel.SessionPersistenceState.PERSISTENT);

            AuthenticationManager.setClientScopesInSession(session, authSession);
            ClientSessionContext clientSessionCtx = TokenManager.attachAuthenticationSession(session, userSession, authSession);

            ClientModel[] audienceClients = getClients(audienceParam);
            if (audienceClients.length > 0) {
                clientSessionCtx.setAttribute(Constants.REQUESTED_AUDIENCE_CLIENTS, audienceClients);
            }

            return function.generateProtocolResponse(userSession, clientSessionCtx, audienceClients, authSession);

        } finally {
            if (authSession != null) {
                authSessionManager.removeAuthenticationSession(realm, authSession, false);
            }
            if (userSession != null) {
                session.sessions().removeUserSession(realm, userSession);
            }
        }
    }

    /** 解析空格分隔的客户端 ID 列表为客户端模型数组 */
    private ClientModel[] getClients(String clientsStr) {
        List<ClientModel> clients = new ArrayList<>();
        if(clientsStr != null && !clientsStr.isEmpty()) {
            for (String clientId : clientsStr.split("\\s+")) {
                ClientModel client = realm.getClientByClientId(clientId);
                if (client != null) {
                    clients.add(client);
                }
            }
        }
        return clients.toArray(ClientModel[]::new);
    }

    /** 校验 access token 是否包含请求的 audience */
    private void validateAudience(AccessToken accessToken, ClientModel[] requestedAudience) {
        List<String> requestedAudienceClientIds = Stream.of(requestedAudience)
                .map(ClientModel::getClientId)
                .collect(Collectors.toList());
        Set<String> missingAudience = TokenUtils.checkRequestedAudiences(accessToken, requestedAudienceClientIds);
        if (!missingAudience.isEmpty()) {
            String missingAudienceStr = CollectionUtil.join(missingAudience);
            throw new NotFoundException("Requested audience not available: " + missingAudienceStr);
        }
    }

    /** 按 ID 获取用户并校验查看权限 */
    private UserModel getUserModel(String userId) {
        if (userId == null) {
            throw new NotFoundException("No userId provided");
        }

        UserModel user = session.users().getUserById(realm, userId);

        try {
            auth.users().requireView(user);
        } catch (ForbiddenException e) {
            throw new ForbiddenException("You have no access to this user");
        }

        if (user == null) {
            throw new NotFoundException("No user found");
        }

        return user;
    }

    /** 协议映射器评估结果 DTO */
    public static class ProtocolMapperEvaluationRepresentation {

        /** 映射器 ID */
        @JsonProperty("mapperId")
        private String mapperId;

        /** 映射器名称 */
        @JsonProperty("mapperName")
        private String mapperName;

        /** 来源容器 ID */
        @JsonProperty("containerId")
        private String containerId;

        /** 来源容器名称 */
        @JsonProperty("containerName")
        private String containerName;

        /** 来源容器类型（client / client-scope） */
        @JsonProperty("containerType")
        private String containerType;

        /** 映射器提供者 ID */
        @JsonProperty("protocolMapper")
        private String protocolMapper;

        public String getMapperId() {
            return mapperId;
        }

        public void setMapperId(String mapperId) {
            this.mapperId = mapperId;
        }

        public String getMapperName() {
            return mapperName;
        }

        public void setMapperName(String mapperName) {
            this.mapperName = mapperName;
        }

        public String getContainerId() {
            return containerId;
        }

        public void setContainerId(String containerId) {
            this.containerId = containerId;
        }

        public String getContainerName() {
            return containerName;
        }

        public void setContainerName(String containerName) {
            this.containerName = containerName;
        }

        public String getContainerType() {
            return containerType;
        }

        public void setContainerType(String containerType) {
            this.containerType = containerType;
        }

        public String getProtocolMapper() {
            return protocolMapper;
        }

        public void setProtocolMapper(String protocolMapper) {
            this.protocolMapper = protocolMapper;
        }
    }

    /** SAML 示例响应包装（JSON 值为 XML 字符串） */
    public static class SamlExampleResponse {

        private final String samlResponse;

        public SamlExampleResponse(String samlResponse) {
            this.samlResponse = samlResponse;
        }

        @JsonValue
        public String getSamlResponse() {
            return samlResponse;
        }
    }

    /** 在模拟会话上下文中生成协议响应的回调 */
    interface ProtocolResponseGenerator<T> {

        T generateProtocolResponse(UserSessionModel userSessionModel, ClientSessionContext clientSessionContext, ClientModel[] audienceClients, AuthenticationSessionModel authSession);
    }
}
