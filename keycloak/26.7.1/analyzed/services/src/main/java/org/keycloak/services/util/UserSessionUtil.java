package org.keycloak.services.util;

import java.util.Objects;
import java.util.function.Consumer;

import org.keycloak.OAuth2Constants;
import org.keycloak.common.ClientConnection;
import org.keycloak.common.Profile;
import org.keycloak.common.constants.ServiceAccountConstants;
import org.keycloak.events.Errors;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.Constants;
import org.keycloak.models.ImpersonationSessionNote;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.models.utils.UserSessionModelDelegate;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.protocol.oidc.encode.AccessTokenContext;
import org.keycloak.protocol.oidc.encode.TokenContextEncoderProvider;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.RefreshToken;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.UserSessionManager;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;
import org.keycloak.util.TokenUtil;

import org.jboss.logging.Logger;

/**
 * 用户会话查找与校验工具类。
 * <p>支持身份 Cookie、刷新令牌、访问令牌等场景下的在线/离线会话验证，
 * 以及临时（transient）会话创建与 impersonation 回退逻辑。</p>
 */
public class UserSessionUtil {

    private static final Logger logger = Logger.getLogger(UserSessionUtil.class);

    /**
     * 为身份 Cookie 中的访问令牌查找有效用户会话。
     *
     * @param invalidSessionCallback 会话无效时的回调
     */
    public static UserSessionValidationResult findValidSessionForIdentityCookie(KeycloakSession session, RealmModel realm, AccessToken token, Consumer<UserSessionModel> invalidSessionCallback) {
        return findValidSession(session, realm, token,  null, AccessTokenContext.SessionType.ONLINE, false, true, invalidSessionCallback);
    }


    /**
     * 为刷新令牌查找有效用户会话（区分 online/offline 类型）。
     *
     * @param invalidSessionCallback 会话无效时的回调
     */
    public static UserSessionValidationResult findValidSessionForRefreshToken(KeycloakSession session, RealmModel realm, RefreshToken token, ClientModel client, Consumer<UserSessionModel> invalidSessionCallback) {
        AccessTokenContext.SessionType sessionType;
        if (TokenUtil.TOKEN_TYPE_OFFLINE.equals(token.getType())) {
            sessionType = AccessTokenContext.SessionType.OFFLINE;
        } else if (TokenUtil.TOKEN_TYPE_REFRESH.equals(token.getType())) {
            sessionType = AccessTokenContext.SessionType.ONLINE;
        } else {
            return UserSessionValidationResult.error(Errors.INVALID_TOKEN_TYPE);
        }

        return findValidSession(session, realm, token, client, sessionType, Profile.isFeatureEnabled(Profile.Feature.TOKEN_EXCHANGE), false, invalidSessionCallback);
    }


    /**
     * 为访问令牌查找有效用户会话（从令牌上下文解析 online/offline 类型）。
     *
     * @param invalidSessionCallback 会话无效时的回调
     */
    public static UserSessionValidationResult findValidSessionForAccessToken(KeycloakSession session, RealmModel realm, AccessToken token, ClientModel client, Consumer<UserSessionModel> invalidSessionCallback) {
        AccessTokenContext accessTokenContext = session.getProvider(TokenContextEncoderProvider.class).getTokenContextFromTokenId(token.getId());
        AccessTokenContext.SessionType sessionType = accessTokenContext.getSessionType();
        return findValidSession(session, realm, token, client, sessionType, Profile.isFeatureEnabled(Profile.Feature.TOKEN_EXCHANGE), false, invalidSessionCallback);
    }

    /**
     * 查找并校验有效用户会话（在线或离线，依 sessionType 决定）。
     * <p>校验会话有效性、客户端会话绑定，以及令牌签发时间不得早于会话启动时间。
     * 成功时将 userSession 写入 {@link org.keycloak.models.KeycloakContext}。</p>
     *
     * @param session 不可为 null
     * @param realm 不可为 null
     * @param token 不可为 null
     * @param client 除非 skipCheckClient 为 true，否则不可为 null
     * @param sessionType 令牌中的会话类型，决定在线/离线查找及是否允许 transient 会话
     * @param allowImpersonationFallback 为 true 时可在 impersonation 场景下不要求 client 已绑定到 userSession
     * @param skipCheckClient 是否跳过 clientSession 查找（如身份 Cookie 场景）
     * @param invalidSessionCallback 找到会话但校验失败时调用；未找到或全部成功时不调用
     * @return 校验结果；session 与 error 不会同时存在，error 为 {@link Errors} 中的代码
     */
    private static UserSessionValidationResult findValidSession(KeycloakSession session, RealmModel realm,
                                                    AccessToken token, ClientModel client,
                                                    AccessTokenContext.SessionType sessionType, boolean allowImpersonationFallback, boolean skipCheckClient, Consumer<UserSessionModel> invalidSessionCallback) {
        logger.tracef("Lookup user session with the sessionType '%s'. Token session id: %s", sessionType, token.getSessionId());
        if (token.getSessionId() == null) {
            if (sessionType.isAllowTransientUserSession()) {
                return createTransientSessionForClient(session, realm, token, client);
            } else {
                return UserSessionValidationResult.error(Errors.USER_SESSION_NOT_FOUND);
            }
        }

        var userSessionProvider = session.sessions();

        UserSessionModel userSession = null;
        if (sessionType.isAllowLookupOnlineUserSession()) {
            AuthenticatedClientSessionModel clientSession = null;
            if (skipCheckClient || sessionType.isAllowTransientClientSession()) {
                userSession = userSessionProvider.getUserSession(realm, token.getSessionId());
            } else {
                userSession = userSessionProvider.getUserSessionIfClientExists(realm, token.getSessionId(), false, client.getId());
                if (userSession != null) {
                    clientSession = userSession.getAuthenticatedClientSessionByClient(client.getId());
                    if (!checkTokenIssuedAt(token, clientSession)) {
                        return UserSessionValidationResult.error(Errors.INVALID_TOKEN, userSession, invalidSessionCallback);
                    }
                }
                if (userSession == null && allowImpersonationFallback) {
                    // 令牌交换 impersonation 场景：尝试通过 impersonator client note 解析会话
                    userSession = getUserSessionWithImpersonatorClient(session, realm, token.getSessionId(), false, client.getId());
                }
            }

            if (AuthenticationManager.isSessionValid(realm, userSession)) {
                if (!checkTokenIssuedAt(token, userSession)) {
                    return UserSessionValidationResult.error(Errors.INVALID_TOKEN, userSession, invalidSessionCallback);
                }

                if (sessionType.isAllowTransientClientSession()) {
                    userSession = createTransientSessionForClient(session, userSession, client);
                    return UserSessionValidationResult.validSession(session, userSession);
                } else {
                    return UserSessionValidationResult.validSession(session, userSession);
                }

            }
        }

        UserSessionModel offlineUserSession = null;
        if (sessionType.isAllowLookupOfflineUserSession()) {
            AuthenticatedClientSessionModel offlineClientSession = null;
            if (sessionType.isAllowTransientClientSession()) {
                offlineUserSession = userSessionProvider.getOfflineUserSession(realm, token.getSessionId());
            } else {
                offlineUserSession = userSessionProvider.getUserSessionIfClientExists(realm, token.getSessionId(), true, client.getId());
                if (offlineUserSession != null) {
                    offlineClientSession = offlineUserSession.getAuthenticatedClientSessionByClient(client.getId());
                    if (!checkTokenIssuedAt(token, offlineClientSession)) {
                        return UserSessionValidationResult.error(Errors.INVALID_TOKEN, offlineUserSession, invalidSessionCallback);
                    }
                }
            }

            if (AuthenticationManager.isSessionValid(realm, offlineUserSession)) {
                if (!checkTokenIssuedAt(token, offlineUserSession)) {
                    return UserSessionValidationResult.error(Errors.INVALID_TOKEN, offlineUserSession, invalidSessionCallback);
                }

                if (sessionType.isAllowTransientClientSession()) {
                    offlineUserSession = createTransientSessionForClient(session, offlineUserSession, client);
                    return UserSessionValidationResult.validSession(session, offlineUserSession);
                } else {
                    return UserSessionValidationResult.validSession(session, offlineUserSession);
                }
            }
        }

        if (userSession == null && offlineUserSession == null) {
            logger.debugf("User session '%s' not found or doesn't have client attached on it", token.getSessionId());
            return UserSessionValidationResult.error(Errors.USER_SESSION_NOT_FOUND);
        }

        logger.debugf("Session '%s' expired", token.getSessionId());
        return UserSessionValidationResult.error(Errors.SESSION_EXPIRED, userSession != null ? userSession : offlineUserSession, invalidSessionCallback);
    }


    /**
     * 基于已有持久化会话创建 transient 用户会话，保留原 started 时间。
     *
     * @throws IllegalArgumentException 若传入的已是 transient 会话
     */
    public static UserSessionModel createTransientUserSession(KeycloakSession session, UserSessionModel userSession) {
        if (userSession.getPersistenceState() == UserSessionModel.SessionPersistenceState.TRANSIENT) {
            throw new IllegalArgumentException("Not expected to invoke this method with the transient session");
        }

        UserSessionModel transientSession = new UserSessionManager(session).createUserSession(userSession.getId(), userSession.getRealm(),
                userSession.getUser(), userSession.getLoginUsername(), userSession.getIpAddress(), userSession.getAuthMethod(), userSession.isRememberMe(),
                userSession.getBrokerSessionId(), userSession.getBrokerUserId(), UserSessionModel.SessionPersistenceState.TRANSIENT);
        userSession.getNotes().entrySet().forEach(e -> transientSession.setNote(e.getKey(), e.getValue()));

        String noteValue = userSession.isOffline() ? Constants.CREATED_FROM_PERSISTENT_OFFLINE : Constants.CREATED_FROM_PERSISTENT_ONLINE;
        transientSession.setNote(Constants.CREATED_FROM_PERSISTENT, noteValue);

        // 保留原持久化会话的 started 时间
        return new UserSessionModelDelegate(transientSession) {

            @Override
            public int getStarted() {
                return userSession.getStarted();
            }

        };
    }

    /** 判断客户端会话是否已授予 offline_access scope。 */
    public static boolean isOfflineAccessGranted(KeycloakSession session, AuthenticatedClientSessionModel clientSession) {
        if (clientSession == null) {
            return false;
        }

        ClientSessionContext clientSessionCtx = DefaultClientSessionContext.fromClientSessionAndScopeParameter(
                clientSession, OAuth2Constants.OFFLINE_ACCESS, session);
        return clientSessionCtx.getClientScopesStream().anyMatch((s -> OAuth2Constants.OFFLINE_ACCESS.equals(s.getName())));
    }

    /** 为 transient 用户会话附加认证会话并绑定客户端 scope。 */
    private static void attachAuthenticationSession(KeycloakSession session, UserSessionModel userSession, ClientModel client) {
        RootAuthenticationSessionModel rootAuthSession = session.authenticationSessions().createRootAuthenticationSession(userSession.getRealm());
        AuthenticationSessionModel authSession = rootAuthSession.createAuthenticationSession(client);
        authSession.setAuthenticatedUser(userSession.getUser());
        authSession.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        authSession.setClientNote(OIDCLoginProtocol.ISSUER, Urls.realmIssuer(session.getContext().getUri().getBaseUri(), userSession.getRealm().getName()));
        AuthenticationManager.setClientScopesInSession(session, authSession);
        TokenManager.attachAuthenticationSession(session, userSession, authSession);
    }

    /** 从已有 userSession 创建带 client 绑定的 transient 会话。 */
    private static UserSessionModel createTransientSessionForClient(KeycloakSession session, UserSessionModel userSession, ClientModel client) {
        UserSessionModel transientSession = createTransientUserSession(session, userSession);
        attachAuthenticationSession(session, transientSession, client);
        return transientSession;
    }

    /** 为无 sessionId 的无状态令牌创建 transient 用户会话并绑定 client。 */
    private static UserSessionValidationResult createTransientSessionForClient(KeycloakSession session, RealmModel realm, AccessToken token, ClientModel client) {
        // 创建 transient 会话
        UserModel user = TokenManager.lookupUserFromStatelessToken(session, realm, token);
        if (user == null) {
            logger.debug("Transient User not found");
            return UserSessionValidationResult.error(Errors.USER_NOT_FOUND);
        }
        if (!user.isEnabled()) {
            logger.debugf("User '%s' disabled", user.getUsername());
            return UserSessionValidationResult.error(Errors.USER_DISABLED);
        }

        ClientConnection clientConnection = session.getContext().getConnection();
        UserSessionModel userSession = new UserSessionManager(session).createUserSession(KeycloakModelUtils.generateId(), realm, user, user.getUsername(), clientConnection.getRemoteHost(),
                ServiceAccountConstants.CLIENT_AUTH, false, null, null, UserSessionModel.SessionPersistenceState.TRANSIENT);
        // 为 client 附加 auth session
        attachAuthenticationSession(session, userSession, client);
        return UserSessionValidationResult.validSession(session, userSession);
    }

    /** 校验令牌签发时间不得早于用户会话启动时间。 */
    private static boolean checkTokenIssuedAt(AccessToken token, UserSessionModel userSession) {
        if (token.isIssuedBeforeSessionStart(userSession.getStarted())) {
            logger.debug("Stale token for user session");
            return false;
        } else {
            return true;
        }
    }

    /** 校验令牌签发时间不得早于客户端会话启动时间。 */
    private static boolean checkTokenIssuedAt(AccessToken token, AuthenticatedClientSessionModel clientSession) {
        if (token.isIssuedBeforeSessionStart(clientSession.getStarted())) {
            logger.debug("Stale token for client session");
            return false;
        } else {
            return true;
        }
    }

    /**
     * 查找带有 impersonator client note 的用户会话（用于令牌交换 impersonation）。
     */
    public static UserSessionModel getUserSessionWithImpersonatorClient(KeycloakSession session, RealmModel realm, String userSessionId, boolean offline, String clientUUID) {
        return session.sessions().getUserSessionWithPredicate(realm, userSessionId, offline, userSession -> Objects.equals(clientUUID, userSession.getNote(ImpersonationSessionNote.IMPERSONATOR_CLIENT.toString())));
    }


    /** 用户会话校验结果：成功时含 userSession，失败时含 {@link Errors} 错误码。 */
    public static class UserSessionValidationResult {
        /** 校验通过的用户会话，失败时为 null */
        private final UserSessionModel userSession;
        /** 错误码，成功时为 null */
        private final String error;

        /** 构造成功结果并将 userSession 写入 KeycloakContext。 */
        private static UserSessionValidationResult validSession(KeycloakSession session, UserSessionModel userSession) {
            session.getContext().setUserSession(userSession);
            return new UserSessionValidationResult(userSession, null);
        }

        /** 构造仅含错误码的失败结果。 */
        private static UserSessionValidationResult error(String error) {
            return new UserSessionValidationResult(null, error);
        }

        /** 构造失败结果并触发 invalidSessionCallback。 */
        private static UserSessionValidationResult error(String error, UserSessionModel invalidUserSession, Consumer<UserSessionModel> invalidSessionCallback) {
            invalidSessionCallback.accept(invalidUserSession);
            return new UserSessionValidationResult(null, error);
        }

        /** 仅由静态工厂方法调用。 */
        private UserSessionValidationResult(UserSessionModel userSession, String error) {
            this.userSession = userSession;
            this.error = error;
        }

        public UserSessionModel getUserSession() {
            return userSession;
        }

        public String getError() {
            return error;
        }
    }
}
