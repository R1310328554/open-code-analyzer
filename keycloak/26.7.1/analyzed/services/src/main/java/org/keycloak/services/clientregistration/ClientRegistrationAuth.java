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

package org.keycloak.services.clientregistration;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import org.keycloak.Config;
import org.keycloak.OAuthErrorException;
import org.keycloak.authentication.AuthenticationProcessor;
import org.keycloak.common.util.ObjectUtil;
import org.keycloak.common.util.Time;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.AdminRoles;
import org.keycloak.models.ClientInitialAccessModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.utils.AuthorizeClientUtil;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.ErrorResponseException;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.context.DynamicClientRegisterContext;
import org.keycloak.services.clientpolicy.context.DynamicClientUnregisterContext;
import org.keycloak.services.clientpolicy.context.DynamicClientUpdateContext;
import org.keycloak.services.clientpolicy.context.DynamicClientViewContext;
import org.keycloak.services.clientregistration.policy.ClientRegistrationPolicyException;
import org.keycloak.services.clientregistration.policy.ClientRegistrationPolicyManager;
import org.keycloak.services.clientregistration.policy.RegistrationAuth;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.util.UserSessionUtil;
import org.keycloak.util.TokenUtil;

import static org.keycloak.models.utils.KeycloakModelUtils.removeTransientAdminRoles;

/**
 * 动态客户端注册端点的认证与授权处理。
 * <p>支持 Bearer 管理令牌、初始访问令牌、注册访问令牌及公开客户端凭据认证，并在各 CRUD 操作前触发客户端策略与注册策略。</p>
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ClientRegistrationAuth {

    /** Keycloak 会话 */
    private final KeycloakSession session;
    /** 当前注册提供者 */
    private final ClientRegistrationProvider provider;
    /** 事件构建器 */
    private final EventBuilder event;

    /** 当前领域 */
    private RealmModel realm;
    /** 解析后的访问令牌 JWT */
    private AccessToken jwt;
    /** 初始访问令牌对应的模型（若适用） */
    private ClientInitialAccessModel initialAccessModel;
    /** JWT 签名密钥 ID */
    private String kid;
    /** 原始 Bearer 令牌字符串 */
    private String token;
    /** 注册端点协议标识（如 openid-connect） */
    private String endpoint;
    /** 是否已完成 init 解析 */
    private boolean initialized;

    /**
     * @param session Keycloak 会话
     * @param provider 注册提供者
     * @param event 事件构建器
     * @param endpoint 端点协议标识
     */
    public ClientRegistrationAuth(KeycloakSession session, ClientRegistrationProvider provider, EventBuilder event, String endpoint) {
        this.session = session;
        this.provider = provider;
        this.event = event;
        this.endpoint = endpoint;
    }

    /** 懒加载：从 Authorization 头解析并校验 Bearer 令牌 */
    void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        realm = session.getContext().getRealm();

        String authorizationHeader = session.getContext().getRequestHeaders().getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null) {
            return;
        }

        int indexOfSpace = authorizationHeader.indexOf(' ');

        if (indexOfSpace <= 0) {
            return;
        }

        String typeString = authorizationHeader.substring(0, indexOfSpace);
        String tokenString = authorizationHeader.substring(indexOfSpace + 1);

        if (!typeString.equalsIgnoreCase(TokenUtil.TOKEN_TYPE_BEARER)) {
            return;
        }

        if (ObjectUtil.isBlank(tokenString) || tokenString.contains(" ")) {
            return;
        }

        token = tokenString;

        ClientRegistrationTokenUtils.TokenVerification tokenVerification = ClientRegistrationTokenUtils.verifyToken(session, realm, token);
        if (tokenVerification.getError() != null) {
            throw unauthorized(tokenVerification.getError().getMessage());
        }
        kid = tokenVerification.getKid();
        jwt = tokenVerification.getJwt();

        if (isInitialAccessToken()) {
            initialAccessModel = session.realms().getClientInitialAccessModel(session.getContext().getRealm(), jwt.getId());
            if (initialAccessModel == null) {
                throw unauthorized("Initial Access Token not found");
            }
        }
    }

    /** @return 原始令牌字符串 */
    public String getToken() {
        return token;
    }

    /** @return JWT 签名密钥 ID */
    public String getKid() {
        return kid;
    }

    /** @return 解析后的访问令牌 */
    public AccessToken getJwt() {
        return jwt;
    }

    /** @return 是否为 Bearer 类型管理令牌 */
    private boolean isBearerToken() {
        return jwt != null && TokenUtil.TOKEN_TYPE_BEARER.equals(jwt.getType());
    }

    /** @return 是否为初始访问令牌 */
    public boolean isInitialAccessToken() {
        return jwt != null && ClientRegistrationTokenUtils.TYPE_INITIAL_ACCESS_TOKEN.equals(jwt.getType());
    }

    /** @return 是否为注册访问令牌 */
    public boolean isRegistrationAccessToken() {
        return jwt != null && ClientRegistrationTokenUtils.TYPE_REGISTRATION_ACCESS_TOKEN.equals(jwt.getType());
    }

    /**
     * 校验创建客户端权限并触发注册前策略。
     * @param context 注册上下文
     * @return 解析得到的注册认证级别
     */
    public RegistrationAuth requireCreate(ClientRegistrationContext context) {
        init();

        RegistrationAuth registrationAuth = RegistrationAuth.ANONYMOUS;

        if (isBearerToken()) {

            if (hasRole(AdminRoles.MANAGE_CLIENTS, AdminRoles.CREATE_CLIENT)) {
                registrationAuth = RegistrationAuth.AUTHENTICATED;
            } else {
                throw forbidden();
            }
        } else if (isInitialAccessToken()) {
            if (initialAccessModel.getRemainingCount() > 0) {
                if (initialAccessModel.getExpiration() == 0 || (initialAccessModel.getTimestamp() + initialAccessModel.getExpiration()) > Time.currentTime()) {
                    registrationAuth = RegistrationAuth.AUTHENTICATED;
                } else {
                    throw unauthorized("Expired initial access token");
                }
            } else {
                throw unauthorized("No remaining count on initial access token");
            }
        }

        try {
            session.clientPolicy().triggerOnEvent(new DynamicClientRegisterContext(context, jwt, realm));
            ClientRegistrationPolicyManager.triggerBeforeRegister(context, registrationAuth);
        } catch (ClientRegistrationPolicyException crpe) {
            throw forbidden(crpe.getMessage());
        } catch (ClientPolicyException cpe) {
            event.detail(Details.REASON, Details.CLIENT_POLICY_ERROR);
            event.detail(Details.CLIENT_POLICY_ERROR, cpe.getError());
            event.detail(Details.CLIENT_POLICY_ERROR_DETAIL, cpe.getErrorDetail());
            event.error(cpe.getError());
            throw forbidden(cpe.getMessage());
        }

        return registrationAuth;
    }

    /** 校验查看客户端权限（不允许公开客户端凭据） */
    public void requireView(ClientModel client) {
        requireView(client, false);
    }

    /**
     * 校验查看客户端权限。
     * @param client 目标客户端
     * @param allowPublicClient 是否允许公开客户端凭据认证
     */
    public void requireView(ClientModel client, boolean allowPublicClient) {
        RegistrationAuth authType = null;
        boolean authenticated = false;

        init();

        if (isBearerToken()) {
            checkClientProtocol();

            if (hasRole(AdminRoles.MANAGE_CLIENTS, AdminRoles.VIEW_CLIENTS)) {
                if (client == null) {
                    throw notFound();
                }

                authenticated = true;
                authType = RegistrationAuth.AUTHENTICATED;
            } else {
                throw forbidden();
            }
        } else if (isRegistrationAccessToken()) {
            if (client != null && client.isEnabled() && client.getRegistrationToken() != null && client.getRegistrationToken().equals(jwt.getId())) {
                checkClientProtocol(client);
                authenticated = true;
                authType = getRegistrationAuth();
            }
        } else if (isInitialAccessToken()) {
            throw unauthorized("Not initial access token allowed");
        } else if (allowPublicClient && authenticatePublicClient(client)) {
            authenticated = true;
            authType = RegistrationAuth.AUTHENTICATED;
        }

        if (authenticated) {
            try {
                session.clientPolicy().triggerOnEvent(new DynamicClientViewContext(session, client, jwt, realm));
                ClientRegistrationPolicyManager.triggerBeforeView(session, provider, authType, client);
            } catch (ClientRegistrationPolicyException crpe) {
                throw forbidden(crpe.getMessage());
            } catch (ClientPolicyException cpe) {
                event.detail(Details.REASON, Details.CLIENT_POLICY_ERROR);
                event.detail(Details.CLIENT_POLICY_ERROR, cpe.getError());
                event.detail(Details.CLIENT_POLICY_ERROR_DETAIL, cpe.getErrorDetail());
                event.error(cpe.getError());
                throw forbidden(cpe.getMessage());
            }
        } else {
            throw unauthorized("Not authorized to view client. Not valid token or client credentials provided.");
        }
    }

    /** @return 注册访问令牌中记录的认证级别 */
    public RegistrationAuth getRegistrationAuth() {
        String str = (String) jwt.getOtherClaims().get(RegistrationAccessToken.REGISTRATION_AUTH);
        return RegistrationAuth.fromString(str);
    }

    /** @return 当前请求的有效注册认证级别 */
    public RegistrationAuth resolveRegistrationAuth() {
        init();
        if (jwt == null) {
            return RegistrationAuth.ANONYMOUS;
        }
        if (isRegistrationAccessToken()) {
            return getRegistrationAuth();
        }
        return RegistrationAuth.AUTHENTICATED;
    }

    /**
     * 校验更新客户端权限并触发更新前策略。
     * @param context 注册上下文
     * @param client 目标客户端
     * @return 注册认证级别
     */
    public RegistrationAuth requireUpdate(ClientRegistrationContext context, ClientModel client) {
        RegistrationAuth regAuth = requireUpdateAuth(client);

        try {
            session.clientPolicy().triggerOnEvent(new DynamicClientUpdateContext(context, client, jwt, realm));
            ClientRegistrationPolicyManager.triggerBeforeUpdate(context, regAuth, client);
        } catch (ClientRegistrationPolicyException crpe) {
            throw forbidden(crpe.getMessage());
        } catch (ClientPolicyException cpe) {
            event.detail(Details.REASON, Details.CLIENT_POLICY_ERROR);
            event.detail(Details.CLIENT_POLICY_ERROR, cpe.getError());
            event.detail(Details.CLIENT_POLICY_ERROR_DETAIL, cpe.getErrorDetail());
            event.error(cpe.getError());
            throw forbidden(cpe.getMessage());
        }

        return regAuth;
    }

    /** 校验删除客户端权限并触发注销前策略 */
    public void requireDelete(ClientModel client) {
        RegistrationAuth chainType = requireUpdateAuth(client);

        try {
            session.clientPolicy().triggerOnEvent(new DynamicClientUnregisterContext(session, client, jwt, realm));
            ClientRegistrationPolicyManager.triggerBeforeRemove(session, provider, chainType, client);
        } catch (ClientRegistrationPolicyException crpe) {
            throw forbidden(crpe.getMessage());
        } catch (ClientPolicyException cpe) {
            event.detail(Details.REASON, Details.CLIENT_POLICY_ERROR);
            event.detail(Details.CLIENT_POLICY_ERROR, cpe.getError());
            event.detail(Details.CLIENT_POLICY_ERROR_DETAIL, cpe.getErrorDetail());
            event.error(cpe.getError());
            throw forbidden(cpe.getMessage());
        }
    }

    /** 校验 Bearer 令牌签发客户端的协议与端点一致 */
    private void checkClientProtocol() {
        ClientModel client = session.getContext().getRealm().getClientByClientId(jwt.getIssuedFor());

        checkClientProtocol(client);
    }

    /** @param client 待校验的客户端 */
    private void checkClientProtocol(ClientModel client) {
        if (endpoint.equals("openid-connect") || endpoint.equals("saml2-entity-descriptor")) {
            if (client != null && !endpoint.contains(client.getProtocol())) {
                throw new ErrorResponseException(Errors.INVALID_CLIENT, "Wrong client protocol.", Response.Status.BAD_REQUEST);
            }
        }
    }

    /** 校验更新/删除操作的认证凭据 */
    private RegistrationAuth requireUpdateAuth(ClientModel client) {
        init();

        if (isBearerToken()) {
            checkClientProtocol();

            if (hasRole(AdminRoles.MANAGE_CLIENTS)) {
                if (client == null) {
                    throw notFound();
                }

                return RegistrationAuth.AUTHENTICATED;
            } else {
                throw forbidden();
            }
        } else if (isRegistrationAccessToken()) {
            if (client != null && client.isEnabled() && client.getRegistrationToken() != null && client.getRegistrationToken().equals(jwt.getId())) {
                return getRegistrationAuth();
            }
        }

        throw unauthorized("Not authorized to update client. Maybe missing token or bad token type.");
    }

    /** @return 初始访问令牌模型 */
    public ClientInitialAccessModel getInitialAccessModel() {
        return initialAccessModel;
    }

    /** 校验 JWT 是否包含指定管理角色（含轻量级令牌路径） */
    private boolean hasRole(String... roles) {
        try {
            boolean lightweight = AuthenticationManager.resolveLightweightAccessTokenRoles(session, jwt, session.getContext().getRealm());

            if (!lightweight) {
                // 轻量级访问令牌：角色已在 UserModel 上解析，无需额外处理
                if (isBearerToken()) {
                    String clientId = getMgmtClientId();
                    AccessToken.Access mgmtClientAccess = jwt.getResourceAccess(clientId);
                    if (mgmtClientAccess != null) {
                        ClientModel client = realm.getClientByClientId(jwt.getIssuedFor());
                        if (client == null) return false;
                        UserSessionModel userSession = UserSessionUtil.findValidSessionForAccessToken(session,realm, jwt, client, (invalidUserSession -> {})).getUserSession();
                        if (userSession == null) return false;

                        removeTransientAdminRoles(realm, clientId, userSession.getUser(), mgmtClientAccess);
                    }
                }
            }

            return hasRoleInToken(roles);
        } catch (Throwable t) {
            return false;
        }
    }

    /** 检查 JWT resource_access 中是否含指定角色 */
    private boolean hasRoleInToken(String[] role) {
        Map<String, AccessToken.Access> resourceAccess = jwt.getResourceAccess();
        if (resourceAccess == null) {
            return false;
        }

        String clientId = getMgmtClientId();

        Set<String> roles = Optional.ofNullable(resourceAccess.get(clientId))
                .map(AccessToken.Access::getRoles)
                .orElse(Collections.emptySet());

        return Arrays.stream(role).anyMatch(roles::contains);
    }

    /** @return 领域管理客户端 ID（master 或 realm-management） */
    private String getMgmtClientId() {
        return realm.getName().equals(Config.getAdminRealm())
                ? realm.getMasterAdminClient().getClientId()
                : Constants.REALM_MANAGEMENT_CLIENT_ID;
    }

    /** 通过客户端凭据认证公开或机密客户端 */
    private boolean authenticatePublicClient(ClientModel client) {
        if (client == null) {
            return false;
        }

        if (client.isPublicClient()) {
            return true;
        }

        AuthenticationProcessor processor = AuthorizeClientUtil.getAuthenticationProcessor(session, event);

        Response response = processor.authenticateClient();
        if (response != null) {
            event.client(client.getClientId()).error(Errors.NOT_ALLOWED);
            throw unauthorized("Failed to authenticate client");
        }

        ClientModel authClient = processor.getClient();
        if (authClient == null) {
            event.client(client.getClientId()).error(Errors.NOT_ALLOWED);
            throw unauthorized("No client authenticated");
        }

        if (!authClient.getClientId().equals(client.getClientId())) {
            event.client(client.getClientId()).error(Errors.NOT_ALLOWED);
            throw unauthorized("Different client authenticated");
        }

        checkClientProtocol(authClient);

        return true;
    }

    /** 构造 401 未授权响应并记录事件 */
    private WebApplicationException unauthorized(String errorDescription) {
        event.detail(Details.REASON, errorDescription).error(Errors.INVALID_TOKEN);
        throw new ErrorResponseException(OAuthErrorException.INVALID_TOKEN, errorDescription, Response.Status.UNAUTHORIZED);
    }

    /** 构造 403 禁止访问响应（默认消息） */
    private WebApplicationException forbidden() {
        return forbidden("Forbidden");
    }

    /** 构造 403 禁止访问响应 */
    private WebApplicationException forbidden(String errorDescription) {
        event.error(Errors.NOT_ALLOWED);
        throw new ErrorResponseException(OAuthErrorException.INSUFFICIENT_SCOPE, errorDescription, Response.Status.FORBIDDEN);
    }

    /** 构造 404 客户端未找到响应 */
    private WebApplicationException notFound() {
        event.error(Errors.CLIENT_NOT_FOUND);
        throw new ErrorResponseException(OAuthErrorException.INVALID_REQUEST, "Client not found", Response.Status.NOT_FOUND);
    }

}
