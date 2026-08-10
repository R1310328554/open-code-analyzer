package org.keycloak.protocol.oidc.refresh;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.UriInfo;

import org.keycloak.OAuthErrorException;
import org.keycloak.common.ClientConnection;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.utils.SessionExpirationUtils;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.RefreshToken;
import org.keycloak.util.TokenUtil;


/**
 * 默认刷新令牌提供者：要求 refresh token 引用的用户会话仍存在于 Keycloak 存储中。
 * <p>支持标准 refresh 与 offline token 类型。</p>
 */
public class DefaultRefreshTokenProvider extends AbstractRefreshTokenProvider implements RefreshTokenProvider {

    /** @param session Keycloak 会话 */
    public DefaultRefreshTokenProvider(KeycloakSession session) {
        super(session);
    }

    /** {@inheritDoc} 默认提供者始终可用于初始签发 */
    @Override
    public boolean supports(InitialRefreshTokenContext initialRefreshTokenCtx) {
        return true;
    }

    /**
     * 初始签发 refresh token：设置过期时间、offline 标记及 requested audience。
     * @param initialRefreshTokenCtx 含客户端会话、响应构建器与 offline 请求标志
     */
    @Override
    public RefreshToken generateRefreshToken(InitialRefreshTokenContext initialRefreshTokenCtx) {
        ClientSessionContext clientSessionCtx = initialRefreshTokenCtx.clientSessionCtx();
        TokenManager.AccessTokenResponseBuilder responseBuilder = initialRefreshTokenCtx.responseBuilder();
        AccessToken accessToken = responseBuilder.getAccessToken();
        AuthenticatedClientSessionModel clientSession = clientSessionCtx.getClientSession();

        RefreshToken refreshToken = createRefreshToken(accessToken, initialRefreshTokenCtx.confirmation(), DefaultRefreshTokenProviderFactory.PROVIDER_ID);

        clientSession.setTimestamp(refreshToken.getIat().intValue());
        UserSessionModel userSession = clientSession.getUserSession();
        userSession.setLastSessionRefresh(refreshToken.getIat().intValue());
        if (initialRefreshTokenCtx.offlineTokenRequested()) {
            refreshToken.type(TokenUtil.TOKEN_TYPE_OFFLINE);
            if (userSession.getRealm().isOfflineSessionMaxLifespanEnabled()) {
                refreshToken.exp(getExpiration(clientSessionCtx, userSession,true));
            }
            responseBuilder.createOrUpdateOfflineSession();
        } else {
            refreshToken.exp(getExpiration(clientSessionCtx, userSession, false));
        }
        final ClientModel[] requestedAudienceClients = clientSessionCtx.getAttribute(Constants.REQUESTED_AUDIENCE_CLIENTS, ClientModel[].class);
        if (requestedAudienceClients != null) {
            refreshToken.getOtherClaims().put(Constants.REQUESTED_AUDIENCE, Arrays.stream(requestedAudienceClients)
                    .map(ClientModel::getClientId)
                    .collect(Collectors.toSet()));
        }

        return refreshToken;
    }

    /** {@inheritDoc} 支持标准 refresh 与 offline 类型令牌 */
    @Override
    public boolean supports(RefreshTokenContext ctx) {
        RefreshToken refreshToken = ctx.oldRefreshToken();
        return (TokenUtil.TOKEN_TYPE_REFRESH.equals(refreshToken.getType()) || TokenUtil.TOKEN_TYPE_OFFLINE.equals(refreshToken.getType()));
    }

    /** {@inheritDoc} 委托 {@link TokenManager#validateToken} 校验会话与 scope */
    @Override
    protected TokenManager.TokenValidation validateToken(KeycloakSession session, UriInfo uriInfo, ClientConnection connection, RealmModel realm,
                                                      RefreshToken oldToken, HttpHeaders headers, String scope, ClientModel client,
                                                      TokenManager tokenManager, EventBuilder event) throws OAuthErrorException {
        return tokenManager.validateToken(session, session.getContext().getUri(), connection, realm, oldToken, headers, scope);
    }

    /** {@inheritDoc} 刷新后更新客户端会话与用户会话时间戳 */
    @Override
    protected void afterRefreshTokenGenerated(RefreshTokenContext ctx, TokenManager.AccessTokenResponseBuilder responseBuilder) {
        AuthenticatedClientSessionModel clientSession = responseBuilder.getClientSessionCtx().getClientSession();
        UserSessionModel userSession = clientSession.getUserSession();
        ctx.grant().updateClientSession(clientSession);
        ctx.grant().updateUserSessionFromClientAuth(userSession);
    }

    /** 按 realm/客户端 idle 与 max lifespan 计算 refresh token 过期秒数 */
    private Long getExpiration(ClientSessionContext clientSessionCtx, UserSessionModel userSession, boolean offline) {
        ClientModel client = clientSessionCtx.getClientSession().getClient();
        RealmModel realm = client.getRealm();
        long expiration = SessionExpirationUtils.calculateClientSessionIdleTimestamp(
                offline, userSession.isRememberMe(),
                TimeUnit.SECONDS.toMillis(clientSessionCtx.getClientSession().getTimestamp()),
                realm, client);
        long lifespan = SessionExpirationUtils.calculateClientSessionMaxLifespanTimestamp(
                offline, userSession.isRememberMe(),
                TimeUnit.SECONDS.toMillis(clientSessionCtx.getClientSession().getStarted()),
                TimeUnit.SECONDS.toMillis(userSession.getStarted()),
                realm, client);
        expiration = lifespan > 0? Math.min(expiration, lifespan) : expiration;

        return TimeUnit.MILLISECONDS.toSeconds(expiration);
    }

}
