/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.authorization.authorization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.keycloak.OAuthErrorException;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.common.DefaultEvaluationContext;
import org.keycloak.authorization.common.KeycloakIdentity;
import org.keycloak.authorization.model.PermissionTicket;
import org.keycloak.authorization.model.Resource;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.model.Scope;
import org.keycloak.authorization.permission.Permissions;
import org.keycloak.authorization.permission.ResourcePermission;
import org.keycloak.authorization.policy.evaluation.EvaluationContext;
import org.keycloak.authorization.policy.evaluation.PermissionTicketAwareDecisionResultCollector;
import org.keycloak.authorization.store.ResourceServerStore;
import org.keycloak.authorization.store.ResourceStore;
import org.keycloak.authorization.store.ScopeStore;
import org.keycloak.authorization.store.StoreFactory;
import org.keycloak.authorization.util.Tokens;
import org.keycloak.common.ClientConnection;
import org.keycloak.common.constants.ServiceAccountConstants;
import org.keycloak.common.util.Base64Url;
import org.keycloak.common.util.PathMatcher;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.UserSessionProvider;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.protocol.oidc.OIDCAdvancedConfigWrapper;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.protocol.oidc.TokenManager.AccessTokenResponseBuilder;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessToken.Authorization;
import org.keycloak.representations.IDToken;
import org.keycloak.representations.RefreshToken;
import org.keycloak.representations.idm.authorization.AuthorizationRequest;
import org.keycloak.representations.idm.authorization.AuthorizationRequest.Metadata;
import org.keycloak.representations.idm.authorization.AuthorizationResponse;
import org.keycloak.representations.idm.authorization.Permission;
import org.keycloak.representations.idm.authorization.PermissionTicketToken;
import org.keycloak.services.CorsErrorResponseException;
import org.keycloak.services.ErrorResponseException;
import org.keycloak.services.Urls;
import org.keycloak.services.cors.Cors;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.AuthenticationSessionManager;
import org.keycloak.services.managers.UserSessionManager;
import org.keycloak.services.util.DefaultClientSessionContext;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;
import org.keycloak.util.JsonSerialization;

import org.jboss.logging.Logger;

/**
 * UMA/授权令牌服务：处理 entitlement 请求、评估权限并签发 RPT。
 * <p>支持 permission ticket、claim token（JWT/ID Token）及多种 response_mode。</p>
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class AuthorizationTokenService {

    /** claim_token 格式：OpenID ID Token。 */
    public static final String CLAIM_TOKEN_FORMAT_ID_TOKEN = "http://openid.net/specs/openid-connect-core-1_0.html#IDToken";
    /** claim_token 格式：JWT access token。 */
    public static final String CLAIM_TOKEN_FORMAT_JWT = "urn:ietf:params:oauth:token-type:jwt";

    private static final Logger logger = Logger.getLogger(AuthorizationTokenService.class);
    private static final String RESPONSE_MODE_DECISION = "decision";
    private static final String RESPONSE_MODE_PERMISSIONS = "permissions";
    private static final String RESPONSE_MODE_DECISION_RESULT = "result";
    private static Map<String, BiFunction<KeycloakAuthorizationRequest, AuthorizationProvider, EvaluationContext>> SUPPORTED_CLAIM_TOKEN_FORMATS;

    static {
        SUPPORTED_CLAIM_TOKEN_FORMATS = new HashMap<>();
        SUPPORTED_CLAIM_TOKEN_FORMATS.put(CLAIM_TOKEN_FORMAT_JWT, (request, authorization) -> {
            Map claims = request.getClaims();
            String claimToken = request.getClaimToken();

            if (claimToken != null) {
                try {
                    claims = JsonSerialization.readValue(Base64Url.decode(request.getClaimToken()), Map.class);
                    request.setClaims(claims);
                } catch (Exception cause) {
                    throw new CorsErrorResponseException(request.getCors(), "invalid_request", "Invalid claims",
                            Status.BAD_REQUEST);
                }
            }

            KeycloakIdentity identity;

            try {
                identity = new KeycloakIdentity(authorization.getKeycloakSession(),
                        Tokens.getAccessToken(request.getSubjectToken(), authorization.getKeycloakSession()));
            } catch (Exception cause) {
                fireErrorEvent(request.getEvent(), Errors.INVALID_TOKEN, cause);
                throw new CorsErrorResponseException(request.getCors(), "unauthorized_client", "Invalid identity", Status.BAD_REQUEST);
            }

            return new DefaultEvaluationContext(identity, claims, authorization.getKeycloakSession());
        });
        SUPPORTED_CLAIM_TOKEN_FORMATS.put(CLAIM_TOKEN_FORMAT_ID_TOKEN, (request, authorization) -> {
            KeycloakSession keycloakSession = authorization.getKeycloakSession();
            String subjectToken = request.getSubjectToken();

            if (subjectToken == null) {
                throw new CorsErrorResponseException(request.getCors(), "invalid_request", "Subject token can not be null and must be a valid ID or Access Token",
                        Status.BAD_REQUEST);
            }

            IDToken idToken;

            try {
                idToken = new TokenManager().verifyIDTokenSignature(keycloakSession, subjectToken);
            } catch (Exception cause) {
                fireErrorEvent(request.getEvent(), Errors.INVALID_SIGNATURE, cause);
                throw new CorsErrorResponseException(request.getCors(), "unauthorized_client", "Invalid signature", Status.BAD_REQUEST);
            }

            String clientId = keycloakSession.getContext().getClient().getClientId();

            if (!clientId.equals(idToken.getIssuedFor())) {
                CorsErrorResponseException exception = new CorsErrorResponseException(request.getCors(), "invalid_claim_token", "Token issued to a different client", Status.BAD_REQUEST);
                fireErrorEvent(request.getEvent(), Errors.INVALID_REQUEST, exception);
                throw exception;
            }

            if (idToken.isExpired()) {
                CorsErrorResponseException exception = new CorsErrorResponseException(request.getCors(), "invalid_claim_token", "Expired token", Status.BAD_REQUEST);
                fireErrorEvent(request.getEvent(), Errors.INVALID_REQUEST, exception);
                throw exception;
            }

            KeycloakIdentity identity;

            try {
                identity = new KeycloakIdentity(keycloakSession, idToken);
            } catch (Exception cause) {
                fireErrorEvent(request.getEvent(), Errors.INVALID_TOKEN, cause);
                throw new CorsErrorResponseException(request.getCors(), "unauthorized_client", "Invalid identity", Status.BAD_REQUEST);
            }

            return new DefaultEvaluationContext(identity, request.getClaims(), keycloakSession);
        });
    }

    private static final AuthorizationTokenService INSTANCE = new AuthorizationTokenService();

    /** @return 单例服务实例 */
    public static AuthorizationTokenService instance() {
        return INSTANCE;
    }

    private static void fireErrorEvent(EventBuilder event, String error, Exception cause) {
        if (cause instanceof CorsErrorResponseException) {
            // 转换异常以填充更详细的事件原因
            CorsErrorResponseException originalCause = (CorsErrorResponseException) cause;
            event.detail(Details.REASON, originalCause.getErrorDescription() == null ? "<unknown>" : originalCause.getErrorDescription())
                    .error(error);
        } else {
            event.detail(Details.REASON, cause == null || cause.getMessage() == null ? "<unknown>" : cause.getMessage())
                    .error(error);
        }

        logger.debug(event.getEvent().getType(), cause);
    }

    /** 授权入口：解析 ticket、评估权限并按 response_mode 返回 RPT/permissions/decision。 */
    public Response authorize(KeycloakAuthorizationRequest request) {
        EventBuilder event = request.getEvent();

        // 公共客户端推送任意 claims 不安全（消息可被篡改）
        if (isPublicClientRequestingEntitlementWithClaims(request)) {
            CorsErrorResponseException forbiddenClientException = new CorsErrorResponseException(request.getCors(), OAuthErrorException.INVALID_GRANT, "Public clients are not allowed to send claims", Status.FORBIDDEN);
            fireErrorEvent(event, Errors.INVALID_REQUEST, forbiddenClientException);
            throw forbiddenClientException;
        }

        try {
            PermissionTicketToken ticket = getPermissionTicket(request);

            request.setClaims(ticket.getClaims());

            EvaluationContext evaluationContext = createEvaluationContext(request);
            KeycloakIdentity identity = KeycloakIdentity.class.cast(evaluationContext.getIdentity());

            if (identity != null) {
                event.user(identity.getId());
                request.getKeycloakSession().getContext().setBearerToken(identity.getAccessToken());
            }

            ResourceServer resourceServer = getResourceServer(ticket, request);

            Collection<Permission> permissions;

            if (request.getTicket() != null) {
                permissions = evaluateUserManagedPermissions(request, ticket, resourceServer, evaluationContext);
            } else if (ticket.getPermissions().isEmpty() && request.getRpt() == null) {
                permissions = evaluateAllPermissions(request, resourceServer, evaluationContext);
            } else {
                permissions = evaluatePermissions(request, ticket, resourceServer, evaluationContext, identity);
            }

            if (isGranted(ticket, request, permissions)) {
                AuthorizationProvider authorization = request.getAuthorization();
                ClientModel targetClient = authorization.getRealm().getClientById(resourceServer.getClientId());
                Metadata metadata = request.getMetadata();
                String responseMode = metadata != null ? metadata.getResponseMode() : null;

                if (responseMode != null) {
                    if (RESPONSE_MODE_DECISION.equals(metadata.getResponseMode())) {
                        Map<String, Object> responseClaims = new HashMap<>();

                        responseClaims.put(RESPONSE_MODE_DECISION_RESULT, true);

                        return createSuccessfulResponse(responseClaims, request);
                    } else if (RESPONSE_MODE_PERMISSIONS.equals(metadata.getResponseMode())) {
                        return createSuccessfulResponse(permissions, request);
                    } else {
                        CorsErrorResponseException invalidResponseModeException = new CorsErrorResponseException(request.getCors(), OAuthErrorException.INVALID_REQUEST, "Invalid response_mode", Status.BAD_REQUEST);
                        fireErrorEvent(event, Errors.INVALID_REQUEST, invalidResponseModeException);
                        throw invalidResponseModeException;
                    }
                } else {
                    return createSuccessfulResponse(createAuthorizationResponse(identity, permissions, request, targetClient), request);
                }
            }

            if (request.isSubmitRequest()) {
                CorsErrorResponseException submittedRequestException = new CorsErrorResponseException(request.getCors(), OAuthErrorException.ACCESS_DENIED, "request_submitted", Status.FORBIDDEN);
                fireErrorEvent(event, Errors.ACCESS_DENIED, submittedRequestException);
                throw submittedRequestException;
            } else {
                CorsErrorResponseException notAuthorizedException = new CorsErrorResponseException(request.getCors(), OAuthErrorException.ACCESS_DENIED, "not_authorized", Status.FORBIDDEN);
                fireErrorEvent(event, Errors.ACCESS_DENIED, notAuthorizedException);
                throw notAuthorizedException;
            }
        } catch (ErrorResponseException | CorsErrorResponseException cause) {
            if (logger.isDebugEnabled()) {
                logger.debug("Error while evaluating permissions", cause);
            }
            throw cause;
        } catch (Exception cause) {
            logger.error("Unexpected error while evaluating permissions", cause);
            throw new CorsErrorResponseException(request.getCors(), OAuthErrorException.SERVER_ERROR, "Unexpected error while evaluating permissions", Status.INTERNAL_SERVER_ERROR);
        }
    }

    /** 构建带 CORS 头的成功 JSON 响应。 */
    private Response createSuccessfulResponse(Object response, KeycloakAuthorizationRequest request) {
        return Cors.builder()
                .checkAllowedOrigins(request.getKeycloakSession(), request.getKeycloakSession().getContext().getClient())
                .allowedMethods(HttpMethod.POST)
                .exposedHeaders(Cors.ACCESS_CONTROL_ALLOW_METHODS)
                .add(Response.status(Status.OK).type(MediaType.APPLICATION_JSON_TYPE).entity(response));
    }

    /** @return 公共客户端是否在 entitlement 请求中携带 claim_token */
    private boolean isPublicClientRequestingEntitlementWithClaims(KeycloakAuthorizationRequest request) {
        return request.getClaimToken() != null && request.getKeycloakSession().getContext().getClient().isPublicClient() && request.getTicket() == null;
    }

    /** 按 ticket 中指定权限评估并返回已授予权限。 */
    private Collection<Permission> evaluatePermissions(KeycloakAuthorizationRequest request, PermissionTicketToken ticket, ResourceServer resourceServer, EvaluationContext evaluationContext, KeycloakIdentity identity) {
        AuthorizationProvider authorization = request.getAuthorization();
        return authorization.evaluators()
                .from(createPermissions(ticket, request, resourceServer, authorization, evaluationContext), evaluationContext)
                .evaluate(resourceServer, request);
    }

    /** 评估用户托管权限（UMA permission ticket 流程）。 */
    private Collection<Permission> evaluateUserManagedPermissions(KeycloakAuthorizationRequest request, PermissionTicketToken ticket, ResourceServer resourceServer, EvaluationContext evaluationContext) {
        AuthorizationProvider authorization = request.getAuthorization();
        return authorization.evaluators()
                .from(createPermissions(ticket, request, resourceServer, authorization, evaluationContext), evaluationContext)
                .evaluate(new PermissionTicketAwareDecisionResultCollector(request, ticket, evaluationContext.getIdentity(), resourceServer, authorization)).results();
    }

    /** 评估资源服务器下全部可授予权限（entitlement 扩展）。 */
    private Collection<Permission> evaluateAllPermissions(KeycloakAuthorizationRequest request, ResourceServer resourceServer, EvaluationContext evaluationContext) {
        AuthorizationProvider authorization = request.getAuthorization();
        return authorization.evaluators()
                .from(evaluationContext, resourceServer, request)
                .evaluate(resourceServer, request);
    }

    /** 组装含 authorization claim 的 RPT 及可选 refresh token。 */
    private AuthorizationResponse createAuthorizationResponse(KeycloakIdentity identity, Collection<Permission> entitlements, KeycloakAuthorizationRequest request, ClientModel targetClient) {
        KeycloakSession keycloakSession = request.getKeycloakSession();
        AccessToken accessToken = identity.getAccessToken();
        RealmModel realm = request.getRealm();
        UserSessionProvider sessions = keycloakSession.sessions();
        UserSessionModel userSessionModel;
        if (accessToken.getSessionState() == null) {
            // 无 sessionState 时创建临时 transient 用户会话
            UserModel user = TokenManager.lookupUserFromStatelessToken(keycloakSession, realm, accessToken);
            userSessionModel = new UserSessionManager(keycloakSession).createUserSession(KeycloakModelUtils.generateId(), realm, user, user.getUsername(), request.getClientConnection().getRemoteHost(),
                    ServiceAccountConstants.CLIENT_AUTH, false, null, null, UserSessionModel.SessionPersistenceState.TRANSIENT);
        } else {
            userSessionModel = sessions.getUserSession(realm, accessToken.getSessionState());

            if (userSessionModel == null) {
                userSessionModel = sessions.getOfflineUserSession(realm, accessToken.getSessionState());
            }
        }

        ClientModel client = realm.getClientByClientId(accessToken.getIssuedFor());
        AuthenticatedClientSessionModel clientSession = userSessionModel.getAuthenticatedClientSessionByClient(targetClient.getId());
        ClientSessionContext clientSessionCtx;

        if (clientSession == null) {
            RootAuthenticationSessionModel rootAuthSession = keycloakSession.authenticationSessions().getRootAuthenticationSession(realm, userSessionModel.getId());

            if (rootAuthSession == null) {
                if (userSessionModel.getUser().getServiceAccountClientLink() == null) {
                    rootAuthSession = keycloakSession.authenticationSessions().createRootAuthenticationSession(realm, userSessionModel.getId());
                } else {
                    // if the user session is associated with a service account
                    rootAuthSession = new AuthenticationSessionManager(keycloakSession).createAuthenticationSession(realm, false);
                }
            }

            AuthenticationSessionModel authSession = rootAuthSession.createAuthenticationSession(targetClient);

            authSession.setAuthenticatedUser(userSessionModel.getUser());
            authSession.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
            authSession.setClientNote(OIDCLoginProtocol.ISSUER, Urls.realmIssuer(keycloakSession.getContext().getUri().getBaseUri(), realm.getName()));

            AuthenticationManager.setClientScopesInSession(keycloakSession, authSession);
            clientSessionCtx = TokenManager.attachAuthenticationSession(keycloakSession, userSessionModel, authSession, true);
        } else {
            clientSessionCtx = DefaultClientSessionContext.fromClientSessionScopeParameter(clientSession, keycloakSession);
        }

        TokenManager tokenManager = request.getTokenManager();
        EventBuilder event = request.getEvent();
        AccessTokenResponseBuilder responseBuilder = tokenManager.responseBuilder(realm, client, event, keycloakSession, userSessionModel, clientSessionCtx)
                .generateAccessToken();

        AccessToken rpt = responseBuilder.getAccessToken();
        Authorization authorization = new Authorization();

        authorization.setPermissions(entitlements);

        rpt.setAuthorization(authorization);

        if (accessToken.getSessionState() == null) {
            // 无 sessionState 的无状态 token 不签发 refresh token
            rpt.setSessionId(null);
        } else {
            if (OIDCAdvancedConfigWrapper.fromClientModel(client).isUseRefreshToken()) {
                responseBuilder.generateRefreshToken();
                RefreshToken refreshToken = responseBuilder.getRefreshToken();

                refreshToken.issuedFor(client.getClientId());
                refreshToken.setAuthorization(authorization);
            }
        }

        if (!rpt.hasAudience(targetClient.getClientId())) {
            rpt.audience(targetClient.getClientId());
        }

        return new AuthorizationResponse(responseBuilder.build(), isUpgraded(request, authorization));
    }

    /** @return 新 RPT 权限是否为旧 RPT 的超集（升级） */
    private boolean isUpgraded(AuthorizationRequest request, Authorization authorization) {
        AccessToken previousRpt = request.getRpt();

        if (previousRpt == null) {
            return false;
        }

        Authorization previousAuthorization = previousRpt.getAuthorization();

        if (previousAuthorization != null) {
            Collection<Permission> previousPermissions = previousAuthorization.getPermissions();

            if (previousPermissions != null) {
                for (Permission previousPermission : previousPermissions) {
                    if (!authorization.getPermissions().contains(previousPermission)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /** 从 UMA ticket 或客户端 permissions 参数解析 {@link PermissionTicketToken}。 */
    private PermissionTicketToken getPermissionTicket(KeycloakAuthorizationRequest request) {
        // 有 ticket 表示 UMA 流程，客户端已从资源服务器获取票据
        if (request.getTicket() != null) {
            return verifyPermissionTicket(request);
        }

        // 无 ticket 时使用客户端请求的 permissions（Keycloak UMA 扩展）
        // This is a Keycloak extension to UMA flow where clients are capable of obtaining a RPT without a ticket
        PermissionTicketToken permissions = request.getPermissions();

        // an issuedFor must be set by the client when doing this method of obtaining RPT, that is how we know the target resource server
        permissions.issuedFor(request.getAudience());

        return permissions;
    }

    /** 按 issuedFor 解析并校验资源服务器客户端。 */
    private ResourceServer getResourceServer(PermissionTicketToken ticket, KeycloakAuthorizationRequest request) {
        AuthorizationProvider authorization = request.getAuthorization();
        StoreFactory storeFactory = authorization.getStoreFactory();
        ResourceServerStore resourceServerStore = storeFactory.getResourceServerStore();
        String issuedFor = ticket.getIssuedFor();

        if (issuedFor == null) {
            CorsErrorResponseException missingIssuedForException = new CorsErrorResponseException(request.getCors(), OAuthErrorException.INVALID_REQUEST, "You must provide the issuedFor", Status.BAD_REQUEST);
            fireErrorEvent(request.getEvent(), Errors.INVALID_REQUEST, missingIssuedForException);
            throw missingIssuedForException;
        }

        ClientModel clientModel = request.getRealm().getClientByClientId(issuedFor);

        if (clientModel == null) {
            CorsErrorResponseException unknownServerIdException = new CorsErrorResponseException(request.getCors(), OAuthErrorException.INVALID_REQUEST, "Unknown resource server id: [" + issuedFor + "]", Status.BAD_REQUEST);
            fireErrorEvent(request.getEvent(), Errors.INVALID_REQUEST, unknownServerIdException);
            throw unknownServerIdException;
        }

        ResourceServer resourceServer = resourceServerStore.findByClient(clientModel);

        if (resourceServer == null) {
            CorsErrorResponseException unsupportedPermissionsException = new CorsErrorResponseException(request.getCors(), OAuthErrorException.INVALID_REQUEST, "Client does not support permissions", Status.BAD_REQUEST);
            fireErrorEvent(request.getEvent(), Errors.INVALID_REQUEST, unsupportedPermissionsException);
            throw unsupportedPermissionsException;
        }

        return resourceServer;
    }

    /** 按 claim_token_format 构建 {@link EvaluationContext}。 */
    private EvaluationContext createEvaluationContext(KeycloakAuthorizationRequest request) {
        String claimTokenFormat = request.getClaimTokenFormat();

        if (claimTokenFormat == null) {
            claimTokenFormat = CLAIM_TOKEN_FORMAT_JWT;
        }

        BiFunction<KeycloakAuthorizationRequest, AuthorizationProvider, EvaluationContext> evaluationContextProvider = SUPPORTED_CLAIM_TOKEN_FORMATS.get(claimTokenFormat);

        if (evaluationContextProvider == null) {
            CorsErrorResponseException unsupportedClaimTokenFormatException = new CorsErrorResponseException(request.getCors(), OAuthErrorException.INVALID_REQUEST, "Claim token format [" + claimTokenFormat + "] not supported", Status.BAD_REQUEST);
            fireErrorEvent(request.getEvent(), Errors.INVALID_REQUEST, unsupportedClaimTokenFormatException);
            throw unsupportedClaimTokenFormatException;
        }

        return evaluationContextProvider.apply(request, request.getAuthorization());
    }

    /** 将 ticket 权限解析为待评估 {@link ResourcePermission} 集合。 */
    private Collection<ResourcePermission> createPermissions(PermissionTicketToken ticket, KeycloakAuthorizationRequest request, ResourceServer resourceServer, AuthorizationProvider authorization, EvaluationContext context) {
        KeycloakIdentity identity = (KeycloakIdentity) context.getIdentity();
        StoreFactory storeFactory = authorization.getStoreFactory();
        Map<String, ResourcePermission> permissionsToEvaluate = new LinkedHashMap<>();
        ResourceStore resourceStore = storeFactory.getResourceStore();
        ScopeStore scopeStore = storeFactory.getScopeStore();
        Metadata metadata = request.getMetadata();
        final AtomicInteger limit = metadata != null && metadata.getLimit() != null ? new AtomicInteger(metadata.getLimit()) : null;

        for (Permission permission : ticket.getPermissions()) {
            if (limit != null && limit.get() <= 0) {
                break;
            }

            Set<Scope> requestedScopesModel = resolveRequestedScopes(request, resourceServer, scopeStore, permission);
            String resourceId = permission.getResourceId();

            if (resourceId != null) {
                resolveResourcePermission(request, resourceServer, identity, authorization, storeFactory, permissionsToEvaluate,
                        resourceStore,
                        limit, permission, requestedScopesModel, resourceId);
            } else {
                resolveScopePermissions(request, resourceServer, authorization, permissionsToEvaluate, resourceStore, limit,
                        requestedScopesModel);
            }
        }

        resolvePreviousGrantedPermissions(request, resourceServer, permissionsToEvaluate, resourceStore, scopeStore, limit);

        return permissionsToEvaluate.values();
    }

    /** 将现有 RPT 中已授予权限合并进待评估集合。 */
    private void resolvePreviousGrantedPermissions(KeycloakAuthorizationRequest request, ResourceServer resourceServer,
                                                   Map<String, ResourcePermission> permissionsToEvaluate, ResourceStore resourceStore, ScopeStore scopeStore,
                                                   AtomicInteger limit) {
        AccessToken rpt = request.getRpt();

        if (rpt != null && rpt.isActive()) {
            Authorization authorizationData = rpt.getAuthorization();

            if (authorizationData != null) {
                Collection<Permission> permissions = authorizationData.getPermissions();

                if (permissions != null) {
                    for (Permission grantedPermission : permissions) {
                        if (limit != null && limit.get() <= 0) {
                            break;
                        }

                        Resource resource = resourceStore.findById(resourceServer, grantedPermission.getResourceId());

                        if (resource != null) {
                            ResourcePermission permission = permissionsToEvaluate.get(resource.getId());

                            if (permission == null) {
                                permission = new ResourcePermission(resource, new ArrayList<>(), resourceServer, grantedPermission.getClaims());
                                permissionsToEvaluate.put(resource.getId(), permission);
                                if (limit != null) {
                                    limit.decrementAndGet();
                                }
                            } else {
                                if (grantedPermission.getClaims() != null) {
                                    for (Entry<String, Set<String>> entry : grantedPermission.getClaims().entrySet()) {
                                        Set<String> claims = permission.getClaims().get(entry.getKey());

                                        if (claims != null) {
                                            claims.addAll(entry.getValue());
                                        }
                                    }
                                }
                            }

                            for (String scopeName : grantedPermission.getScopes()) {
                                Scope scope = scopeStore.findByName(resourceServer, scopeName);

                                if (scope != null) {
                                    if (!permission.getScopes().contains(scope)) {
                                        permission.getScopes().add(scope);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /** 仅指定 scope 时解析关联资源或 scope 级权限。 */
    private void resolveScopePermissions(KeycloakAuthorizationRequest request,
            ResourceServer resourceServer, AuthorizationProvider authorization,
            Map<String, ResourcePermission> permissionsToEvaluate, ResourceStore resourceStore, AtomicInteger limit,
            Set<Scope> requestedScopesModel) {
        AtomicBoolean processed = new AtomicBoolean();

        resourceStore.findByScopes(resourceServer, requestedScopesModel, resource -> {
            if (limit != null && limit.get() <= 0) {
                return;
            }

            ResourcePermission perm = permissionsToEvaluate.get(resource.getId());

            if (perm == null) {
                perm = Permissions.createResourcePermissions(resource, resourceServer, requestedScopesModel, authorization, request);
                permissionsToEvaluate.put(resource.getId(), perm);
                if (limit != null) {
                    limit.decrementAndGet();
                }
            } else {
                for (Scope scope : requestedScopesModel) {
                    perm.addScope(scope);
                }
            }

            processed.compareAndSet(false, true);
        });

        if (!processed.get()) {
            for (Scope scope : requestedScopesModel) {
                if (limit != null && limit.getAndDecrement() <= 0) {
                    break;
                }
                permissionsToEvaluate.computeIfAbsent(scope.getId(), s -> new ResourcePermission(null, new ArrayList<>(Arrays.asList(scope)), resourceServer, request.getClaims()));
            }
        }
    }

    /** 按资源 ID/类型/名称/owner 等前缀解析资源权限。 */
    private void resolveResourcePermission(KeycloakAuthorizationRequest request,
            ResourceServer resourceServer, KeycloakIdentity identity, AuthorizationProvider authorization,
            StoreFactory storeFactory, Map<String, ResourcePermission> permissionsToEvaluate, ResourceStore resourceStore,
            AtomicInteger limit, Permission permission, Set<Scope> requestedScopesModel, String resourceId) {
        Resource resource;

        if (resourceId.indexOf('-') != -1) {
            resource = resourceStore.findById(resourceServer, resourceId);
        } else {
            resource = null;
        }

        if (resource != null) {
            addPermission(request, resourceServer, authorization, permissionsToEvaluate, limit, requestedScopesModel, resource);
        } else if (resourceId.startsWith("resource-type:")) {
            // 仅资源类型实例，类型由资源服务器持有
            String resourceType = resourceId.substring("resource-type:".length());
            resourceStore.findByType(resourceServer, resourceType, resourceServer.getClientId(),
                    resource1 -> addPermission(request, resourceServer, authorization, permissionsToEvaluate, limit, requestedScopesModel, resource1));
        } else if (resourceId.startsWith("resource-type-any:")) {
            // 任意给定类型的资源
            String resourceType = resourceId.substring("resource-type-any:".length());
            resourceStore.findByType(resourceServer, resourceType, null,
                    resource12 -> addPermission(request, resourceServer, authorization, permissionsToEvaluate, limit, requestedScopesModel, resource12));
        } else if (resourceId.startsWith("resource-type-instance:")) {
            // 仅给定类型的资源实例
            String resourceType = resourceId.substring("resource-type-instance:".length());
            resourceStore.findByTypeInstance(resourceServer, resourceType,
                    resource13 -> addPermission(request, resourceServer, authorization, permissionsToEvaluate, limit, requestedScopesModel, resource13));
        } else if (resourceId.startsWith("resource-type-owner:")) {
            // 仅当前身份为 owner 的资源
            String resourceType = resourceId.substring("resource-type-owner:".length());
            resourceStore.findByType(resourceServer, resourceType, identity.getId(),
                    resource14 -> addPermission(request, resourceServer, authorization, permissionsToEvaluate, limit, requestedScopesModel, resource14));
        } else {
            Resource ownerResource = resourceStore.findByName(resourceServer, resourceId, identity.getId());

            if (ownerResource != null) {
                permission.setResourceId(ownerResource.getId());
                addPermission(request, resourceServer, authorization, permissionsToEvaluate, limit, requestedScopesModel, ownerResource);
            }

            if (!identity.isResourceServer() || !identity.getId().equals(resourceServer.getClientId())) {
                List<PermissionTicket> tickets = storeFactory.getPermissionTicketStore().findGranted(resourceServer, resourceId, identity.getId());

                if (!tickets.isEmpty()) {
                    List<Scope> scopes = new ArrayList<>();
                    Resource grantedResource = null;
                    for (PermissionTicket permissionTicket : tickets) {
                        if (grantedResource == null) {
                            grantedResource = permissionTicket.getResource();
                        }
                        scopes.add(permissionTicket.getScope());
                    }
                    requestedScopesModel.retainAll(scopes);
                    ResourcePermission resourcePermission = addPermission(request, resourceServer, authorization,
                            permissionsToEvaluate, limit,
                            requestedScopesModel, grantedResource);
                    if (resourcePermission != null) {
                        Collection<Scope> permissionScopes = resourcePermission.getScopes();
                        if (permissionScopes != null) {
                            permissionScopes.retainAll(scopes);
                        }
                        // 所有者显式授予，标记为已授予以跳过策略引擎
                        resourcePermission.setGranted(true);
                    }
                }

                Resource serverResource = resourceStore.findByName(resourceServer, resourceId);

                if (serverResource != null) {
                    permission.setResourceId(serverResource.getId());
                    addPermission(request, resourceServer, authorization, permissionsToEvaluate, limit, requestedScopesModel, serverResource);
                }

                if (permissionsToEvaluate.isEmpty()) {
                    CorsErrorResponseException invalidResourceException = new CorsErrorResponseException(request.getCors(), "invalid_resource", "Resource with id [" + resourceId + "] does not exist.", Status.BAD_REQUEST);
                    fireErrorEvent(request.getEvent(), Errors.INVALID_REQUEST, invalidResourceException);
                    throw invalidResourceException;
                }
            }
        }
    }

    /** 解析并校验请求 scope 名称。 */
    private Set<Scope> resolveRequestedScopes(KeycloakAuthorizationRequest request,
            ResourceServer resourceServer, ScopeStore scopeStore, Permission permission) {
        String clientAdditionalScopes = request.getScope();
        Set<String> requestedScopes = permission.getScopes();

        if (permission.getScopes() == null) {
            requestedScopes = new HashSet<>();
        }

        if (clientAdditionalScopes != null) {
            requestedScopes.addAll(Arrays.asList(clientAdditionalScopes.split(" ")));
        }

        Set<Scope> requestedScopesModel = requestedScopes.stream().map(s -> scopeStore.findByName(resourceServer, s)).filter(
                Objects::nonNull).collect(Collectors.toSet());

        if (!requestedScopes.isEmpty() && requestedScopesModel.isEmpty()) {
            CorsErrorResponseException invalidScopeException = new CorsErrorResponseException(request.getCors(), "invalid_scope", "One of the given scopes " + permission.getScopes() + " is invalid", Status.BAD_REQUEST);
            fireErrorEvent(request.getEvent(), Errors.INVALID_REQUEST, invalidScopeException);
            throw invalidScopeException;
        }
        return requestedScopesModel;
    }

    /** 向待评估映射添加资源权限（受 limit 约束）。 */
    private ResourcePermission addPermission(KeycloakAuthorizationRequest request, ResourceServer resourceServer,
            AuthorizationProvider authorization, Map<String, ResourcePermission> permissionsToEvaluate, AtomicInteger limit,
            Set<Scope> requestedScopesModel, Resource resource) {
        ResourcePermission permission = permissionsToEvaluate.get(resource.getId());

        if (permission == null) {
            permission = new ResourcePermission(resource,
                    Permissions.resolveScopes(resource, resourceServer, requestedScopesModel, authorization), resourceServer,
                    request.getClaims());
            // 若请求了 scope，校验解析结果非空
            // if it is not the case, then the requested scope is invalid and we don't need to evaluate
            if (!requestedScopesModel.isEmpty() && permission.getScopes().isEmpty()) {
                return null;
            }
            permissionsToEvaluate.put(resource.getId(), permission);
            if (limit != null) {
                limit.decrementAndGet();
            }
        }

        return permission;
    }

    /** 解码并校验 UMA permission ticket。 */
    private PermissionTicketToken verifyPermissionTicket(KeycloakAuthorizationRequest request) {
        String ticketString = request.getTicket();

        PermissionTicketToken ticket = request.getKeycloakSession().tokens().decode(ticketString, PermissionTicketToken.class);
        if (ticket == null) {
            CorsErrorResponseException ticketVerificationException = new CorsErrorResponseException(request.getCors(), "invalid_ticket", "Ticket verification failed", Status.FORBIDDEN);
            fireErrorEvent(request.getEvent(), Errors.INVALID_PERMISSION_TICKET, ticketVerificationException);
            throw ticketVerificationException;
        }

        if (!ticket.isActive()) {
            CorsErrorResponseException invalidTicketException = new CorsErrorResponseException(request.getCors(), "invalid_ticket", "Invalid permission ticket.", Status.FORBIDDEN);
            fireErrorEvent(request.getEvent(), Errors.INVALID_PERMISSION_TICKET, invalidTicketException);
            throw invalidTicketException;
        }

        return ticket;
    }

    /** @return 是否至少授予一项请求权限且 RPT 升级校验通过 */
    private boolean isGranted(PermissionTicketToken ticket, AuthorizationRequest request, Collection<Permission> permissions) {
        List<Permission> requestedPermissions = ticket.getPermissions();

        // 携带 RPT 时若任一请求权限未授予则拒绝
        if (request.getRpt() != null && !requestedPermissions.isEmpty() && requestedPermissions.stream().anyMatch(permission -> !permissions.contains(permission))) {
            return false;
        }

        return !permissions.isEmpty();
    }

    /** Keycloak 扩展的 {@link AuthorizationRequest}，绑定会话、CORS 与事件上下文。 */
    public static class KeycloakAuthorizationRequest extends AuthorizationRequest {

        private final AuthorizationProvider authorization;
        private final TokenManager tokenManager;
        private final EventBuilder event;
        private final HttpRequest httpRequest;
        private final Cors cors;
        private final ClientConnection clientConnection;

        public KeycloakAuthorizationRequest(AuthorizationProvider authorization, TokenManager tokenManager, EventBuilder event, HttpRequest request, Cors cors, ClientConnection clientConnection) {
            this.authorization = authorization;
            this.tokenManager = tokenManager;
            this.event = event;
            httpRequest = request;
            this.cors = cors;
            this.clientConnection = clientConnection;
        }

        TokenManager getTokenManager() {
            return tokenManager;
        }

        EventBuilder getEvent() {
            return event;
        }

        HttpRequest getHttpRequest() {
            return httpRequest;
        }

        AuthorizationProvider getAuthorization() {
            return authorization;
        }

        Cors getCors() {
            return cors;
        }

        KeycloakSession getKeycloakSession() {
            return getAuthorization().getKeycloakSession();
        }

        RealmModel getRealm() {
            return getKeycloakSession().getContext().getRealm();
        }

        ClientConnection getClientConnection() {
            return clientConnection;
        }

        /** 按 id 或 uri 格式批量添加权限到请求。 */
        public void addPermissions(List<String> permissionList, String permissionResourceFormat, boolean matchingUri, Integer maxResults) {
            if (permissionResourceFormat == null) {
                permissionResourceFormat = "id";
            }

            switch (permissionResourceFormat) {
                case "id":
                    addPermissionsById(permissionList);
                    break;
                case "uri":
                    addPermissionsByUri(permissionList, matchingUri, maxResults);
                    break;
            }

        }

        /** 解析 rsid#scope1,scope2 格式权限。 */
        private void addPermissionsById(List<String> permissionList) {
            for (String permission : permissionList) {
                String[] parts = permission.split("#");
                String rsid = parts[0];

                if (parts.length == 1) {
                    addPermission(rsid);
                } else {
                    String[] scopes = parts[1].split(",");
                    addPermission(rsid, scopes);
                }
            }
        }

        /** 按 URI（可选模板匹配）解析资源并添加权限。 */
        private void addPermissionsByUri(List<String> permissionList, boolean matchingUri, Integer maxResults) {
            StoreFactory storeFactory = authorization.getStoreFactory();

            for (String permission : permissionList) {
                String[] parts = permission.split("#");
                String uri = parts[0];

                if (parts.length == 1) {
                    // 仅指定资源 URI
                    if (uri.isEmpty()) {
                        CorsErrorResponseException invalidResourceException = new CorsErrorResponseException(getCors(),
                            OAuthErrorException.INVALID_REQUEST, "You must provide the uri", Status.BAD_REQUEST);
                        fireErrorEvent(getEvent(), Errors.INVALID_REQUEST, invalidResourceException);
                        throw invalidResourceException;
                    }

                    List<Resource> resources = getResourceListByUri(uri, storeFactory, matchingUri, maxResults);

                    if (resources == null || resources.isEmpty()) {
                        CorsErrorResponseException invalidResourceException = new CorsErrorResponseException(getCors(),
                            "invalid_resource", "Resource with uri [" + uri + "] does not exist.", Status.BAD_REQUEST);
                        fireErrorEvent(getEvent(), Errors.INVALID_REQUEST, invalidResourceException);
                        throw invalidResourceException;
                    }

                    resources.stream().forEach(resource -> addPermission(resource.getId()));
                } else {
                    // 指定 URI 与 scope，或仅 scope
                    String[] scopes = parts[1].split(",");

                    if (uri.isEmpty()) {
                        // 仅指定 scope
                        addPermission("", scopes);
                        return;
                    }

                    List<Resource> resources = getResourceListByUri(uri, storeFactory, matchingUri, maxResults);

                    if (resources == null || resources.isEmpty()) {
                        CorsErrorResponseException invalidResourceException = new CorsErrorResponseException(getCors(),
                            "invalid_resource", "Resource with uri [" + uri + "] does not exist.", Status.BAD_REQUEST);
                        fireErrorEvent(getEvent(), Errors.INVALID_REQUEST, invalidResourceException);
                        throw invalidResourceException;
                    }

                    resources.stream().forEach(resource -> addPermission(resource.getId(), scopes));
                }
            }
        }

        /** 按 URI 精确或 PathMatcher 模板匹配查找资源。 */
        private List<Resource> getResourceListByUri(String uri, StoreFactory storeFactory, boolean matchingUri, Integer maxResults) {
            Map<Resource.FilterOption, String[]> search = new EnumMap<>(Resource.FilterOption.class);
            search.put(Resource.FilterOption.URI, new String[] { uri });
            ResourceServer resourceServer = storeFactory.getResourceServerStore()
                .findByClient(getRealm().getClientByClientId(getAudience()));

            List<Resource> resources = storeFactory.getResourceStore().find(resourceServer, search, -1, maxResults);

            if (!matchingUri || !resources.isEmpty()) {
                return resources;
            }

            search = new EnumMap<>(Resource.FilterOption.class);
            search.put(Resource.FilterOption.URI_NOT_NULL, new String[] { "true" });
            search.put(Resource.FilterOption.OWNER, new String[] { resourceServer.getClientId() });

            List<Resource> serverResources = storeFactory.getResourceStore().find(resourceServer, search, -1, -1);

            PathMatcher<Map.Entry<String, Resource>> pathMatcher = new PathMatcher<Map.Entry<String, Resource>>() {
                @Override
                protected String getPath(Map.Entry<String, Resource> entry) {
                    return entry.getKey();
                }

                @Override
                protected Collection<Map.Entry<String, Resource>> getPaths() {
                    Map<String, Resource> result = new HashMap<>();
                    serverResources.forEach(resource -> resource.getUris().forEach(uri -> {
                        result.put(uri, resource);
                    }));

                    return result.entrySet();
                }
            };

            Map.Entry<String, Resource> matches = pathMatcher.matches(uri);

            if (matches != null) {
                return Collections.singletonList(matches.getValue());
            }

            return null;
        }
    }
}
