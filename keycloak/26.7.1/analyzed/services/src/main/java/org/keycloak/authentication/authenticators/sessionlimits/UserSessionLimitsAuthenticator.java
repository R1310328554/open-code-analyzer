package org.keycloak.authentication.authenticators.sessionlimits;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.ws.rs.core.Response;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.AuthenticationFlowException;
import org.keycloak.authentication.Authenticator;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.utils.StringUtil;

import org.jboss.logging.Logger;

/**
 * 用户会话数量限制认证器：在登录时检查用户在 realm 或 client 上的并发会话数，超出限制时拒绝新会话或终止最旧会话。
 * <p>需在认证流程中配置 {@link UserSessionLimitsAuthenticatorFactory} 提供的各项阈值。</p>
 */
public class UserSessionLimitsAuthenticator implements Authenticator {

    private static final Logger logger = Logger.getLogger(UserSessionLimitsAuthenticator.class);
    /** 默认会话超限错误消息键。 */
    public static final String SESSION_LIMIT_EXCEEDED = "sessionLimitExceeded";
    /** 当前 Keycloak 会话。 */
    protected final KeycloakSession session;

    /** 超限时的行为策略（拒绝或终止最旧会话）。 */
    String behavior;

    /** @param session Keycloak 会话 */
    public UserSessionLimitsAuthenticator(KeycloakSession session) {
        this.session = session;
    }

    /** 统计 realm/client 会话数并在超限时执行配置的行为策略。 */
    @Override
    public void authenticate(AuthenticationFlowContext context) {
        AuthenticatorConfigModel authenticatorConfig = context.getAuthenticatorConfig();
        if (authenticatorConfig == null) {
            throw new AuthenticationFlowException("No configuration found of 'User Session Count Limiter' authenticator. Please make sure to configure this authenticator in your authentication flow in the realm '" + context.getRealm().getName() + "'!"
                    , AuthenticationFlowError.INTERNAL_ERROR);
        }
        Map<String, String> config = authenticatorConfig.getConfig();

        // 获取当前正在认证的客户端
        ClientModel currentClient = context.getAuthenticationSession().getClient();
        logger.debugf("session-limiter's current keycloak clientId: %s", currentClient.getClientId());

        // 判断是否需要新建用户会话或客户端会话
        AuthenticationManager.AuthResult authResult = AuthenticationManager.authenticateIdentityCookie(context.getSession(), context.getRealm(), true);
        final boolean newUserSession = authResult == null || authResult.session() == null;
        final boolean newClientSession = authResult == null || authResult.session() == null
                || authResult.session().getAuthenticatedClientSessionByClient(currentClient.getId()) == null;

        // 读取认证器配置
        behavior = config.get(UserSessionLimitsAuthenticatorFactory.BEHAVIOR);
        int userRealmLimit = getIntConfigProperty(UserSessionLimitsAuthenticatorFactory.USER_REALM_LIMIT, config);
        int userClientLimit = getIntConfigProperty(UserSessionLimitsAuthenticatorFactory.USER_CLIENT_LIMIT, config);

        if (context.getRealm() != null && context.getUser() != null) {

            // 统计该用户在 realm 内的会话总数
            List<UserSessionModel> userSessionsForRealm = session.sessions()
                    .getUserSessionsStream(context.getRealm(), context.getUser())
                    .collect(Collectors.toList());
            int userSessionCountForRealm = userSessionsForRealm.size();

            // 统计该用户在当前客户端上的会话数
            List<UserSessionModel> userSessionsForClient = getUserSessionsForClientIfEnabled(userSessionsForRealm, currentClient, userClientLimit);
            int userSessionCountForClient = userSessionsForClient.size();
            logger.debugf("session-limiter's configured realm session limit: %s", userRealmLimit);
            logger.debugf("session-limiter's configured client session limit: %s", userClientLimit);
            logger.debugf("session-limiter's count of total user sessions for the entire realm (could be apps other than web apps): %s", userSessionCountForRealm);
            logger.debugf("session-limiter's count of total user sessions for this keycloak client: %s", userSessionCountForClient);

            // 优先检查 realm 级会话是否超限
            if (newUserSession && exceedsLimit(userSessionCountForRealm, userRealmLimit)) {
                logger.infof("Too many session in this realm for the current user. Session count: %s", userSessionCountForRealm);
                String eventDetails = String.format("Realm session limit exceeded. Realm: %s, Realm limit: %s. Session count: %s, User id: %s",
                        context.getRealm().getName(), userRealmLimit, userSessionCountForRealm, context.getUser().getId());

                var removedClientSessions = handleLimitExceeded(context, userSessionsForClient, eventDetails, userClientLimit);
                if (exceedsLimit(userSessionCountForRealm - removedClientSessions.size(), userRealmLimit))
                {
                    List<UserSessionModel> remainingSessionsToBeRemoved = userSessionsForRealm
                            .stream()
                            .filter(userSessionModel -> !removedClientSessions.contains(userSessionModel))
                            .collect(Collectors.toList());
                    handleLimitExceeded(context, remainingSessionsToBeRemoved, eventDetails, userRealmLimit);
                }
            } // realm 未超限时再检查 client 级会话限制
            else if (newClientSession && exceedsLimit(userSessionCountForClient, userClientLimit)) {
                logger.infof("Too many sessions related to the current client for this user. Session count: %s", userSessionCountForClient);
                String eventDetails = String.format("Client session limit exceeded. Realm: %s, Client limit: %s. Session count: %s, User id: %s",
                        context.getRealm().getName(), userClientLimit, userSessionCountForClient, context.getUser().getId());
                handleLimitExceeded(context, userSessionsForClient, eventDetails, userClientLimit);
            } else {
                context.success();
            }
        } else {
            context.success();
        }
    }

    /** 判断会话数是否超过限制（limit ≤ 0 表示禁用）。 */
    private boolean exceedsLimit(long count, long limit) {
        if (limit <= 0) { // 零或负值表示禁用该限制
            return false;
        }
        return getNumberOfSessionsThatNeedToBeLoggedOut(count, limit) > 0;
    }

    /** 计算需要注销的会话数量。 */
    private long getNumberOfSessionsThatNeedToBeLoggedOut(long count, long limit) {
        return Math.max(0, count - (limit - 1));
    }

    /** 从配置 map 读取整型属性，空白时返回 -1。 */
    private int getIntConfigProperty(String key, Map<String, String> config) {
        String value = config.get(key);
        if (StringUtil.isBlank(value)) {
            return -1;
        }
        return Integer.parseInt(value);
    }

    /** 若启用 client 限制则过滤出当前客户端的会话列表。 */
    private List<UserSessionModel> getUserSessionsForClientIfEnabled(List<UserSessionModel> userSessionsForRealm, ClientModel currentClient, int userClientLimit) {
        // 仅当配置了 client 限制时才统计，否则跳过开销较大的过滤
        if (userClientLimit <= 0) {
            logger.debugf("total user sessions for this keycloak client will not be counted. Will be logged as 0 (zero)");
            return Collections.emptyList();
        }
        List<UserSessionModel> userSessionsForClient = userSessionsForRealm.stream().filter(session -> session.getAuthenticatedClientSessionByClient(currentClient.getId()) != null).collect(Collectors.toList());
        return userSessionsForClient;
    }

    @Override
    public void action(AuthenticationFlowContext context) {

    }

    /** @return 不需要已认证用户 */
    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }

    @Override
    public void close() {

    }

    /**
     * 处理会话超限：拒绝新会话或终止最旧会话。
     *
     * @return 已注销的用户会话列表（若有）
     */
    /** 按 behavior 配置执行拒绝或终止最旧会话。 */
    private List<UserSessionModel> handleLimitExceeded(AuthenticationFlowContext context, List<UserSessionModel> userSessions, String eventDetails, long limit) {
        switch (behavior) {
            case UserSessionLimitsAuthenticatorFactory.DENY_NEW_SESSION:
                logger.info("Denying new session");
                String errorMessage = Optional.ofNullable(context.getAuthenticatorConfig())
                        .map(AuthenticatorConfigModel::getConfig)
                        .map(f -> f.get(UserSessionLimitsAuthenticatorFactory.ERROR_MESSAGE))
                        .orElse(SESSION_LIMIT_EXCEEDED);

                context.getEvent().error(Errors.GENERIC_AUTHENTICATION_ERROR);
                Response challenge = context.form().setError(errorMessage).createErrorPage(Response.Status.FORBIDDEN);
                context.failure(AuthenticationFlowError.GENERIC_AUTHENTICATION_ERROR, challenge, eventDetails, errorMessage);
                return Collections.emptyList();

            case UserSessionLimitsAuthenticatorFactory.TERMINATE_OLDEST_SESSION:
                logger.info("Terminating oldest session");
                var removedSessions = logoutOldestSessions(userSessions, limit, context.getEvent());
                context.success();
                return removedSessions;
        }

        return Collections.emptyList();
    }

    /**
     * @return A list of logged-out user sessions, if any.
     */
    /** 按 lastSessionRefresh 排序并注销最旧的若干会话。 */
    private List<UserSessionModel> logoutOldestSessions(List<UserSessionModel> userSessions, long limit, EventBuilder eventBuilder) {
        long numberOfSessionsThatNeedToBeLoggedOut = getNumberOfSessionsThatNeedToBeLoggedOut(userSessions.size(), limit);

        if (numberOfSessionsThatNeedToBeLoggedOut == 0) {
            logger.debug("No additional sessions that need to be logged out");
            return Collections.emptyList();
        } else if (numberOfSessionsThatNeedToBeLoggedOut == 1) {
            logger.info("Logging out oldest session");
        } else {
            logger.infof("Logging out oldest %s sessions", numberOfSessionsThatNeedToBeLoggedOut);
        }

        List<UserSessionModel> userSessionsToBeRemoved = userSessions
            .stream()
            .sorted(Comparator.comparingInt(UserSessionModel::getLastSessionRefresh))
            .limit(numberOfSessionsThatNeedToBeLoggedOut)
            .toList();

        for (UserSessionModel userSession : userSessionsToBeRemoved) {
            AuthenticationManager.backchannelLogout(session, userSession, true);
            eventBuilder.clone()
                .event(EventType.LOGOUT)
                .user(userSession.getUser())
                .session(userSession.getId())
                .success();
        }

        return userSessionsToBeRemoved;
    }
}
