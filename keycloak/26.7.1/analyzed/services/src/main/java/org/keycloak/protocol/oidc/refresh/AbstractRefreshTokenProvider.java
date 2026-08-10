package org.keycloak.protocol.oidc.refresh;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.UriInfo;

import org.keycloak.OAuth2Constants;
import org.keycloak.OAuthErrorException;
import org.keycloak.TokenVerifier;
import org.keycloak.common.ClientConnection;
import org.keycloak.common.Profile;
import org.keycloak.common.VerificationException;
import org.keycloak.common.util.Retry;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.events.Details;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.AbstractKeycloakTransaction;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.organization.protocol.mappers.oidc.OrganizationScope;
import org.keycloak.protocol.oidc.OIDCAdvancedConfigWrapper;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AuthorizationDetailsJSONRepresentation;
import org.keycloak.representations.RefreshToken;
import org.keycloak.util.TokenUtil;

import org.jboss.logging.Logger;

import static org.keycloak.models.Constants.AUTHORIZATION_DETAILS_RESPONSE;

/**
 * 刷新令牌提供者抽象基类：校验旧 refresh token 并签发新的 access/refresh token。
 * <p>封装 scope 裁剪、会话锁定、令牌重用检测及 authorization_details 传递等通用逻辑；子类实现 {@link #validateToken} 与 {@link #afterRefreshTokenGenerated}。</p>
 */
public abstract class AbstractRefreshTokenProvider implements RefreshTokenProvider {

    private static final Logger logger = Logger.getLogger(AbstractRefreshTokenProvider.class);

    protected final KeycloakSession session;

    /** @param session Keycloak 会话 */
    protected AbstractRefreshTokenProvider(KeycloakSession session) {
        this.session = session;
    }

    /**
     * 执行 refresh_token grant：校验旧令牌、重建 access token，并按客户端配置轮换 refresh token。
     * @param ctx 含旧 refresh token、grant 上下文与 scope/resource 参数
     * @return 成功时的新令牌响应构建器
     * @throws OAuthErrorException 校验失败或 consent 失效
     */
    @Override
    public TokenManager.AccessTokenResponseBuilder refreshAccessToken(RefreshTokenContext ctx) throws OAuthErrorException {
        RealmModel realm = ctx.grantContext().getRealm();
        TokenManager tokenManager = ctx.tokenManager();
        RefreshToken oldRefreshToken = ctx.oldRefreshToken();
        EventBuilder event = ctx.grantContext().getEvent();
        ClientModel authorizedClient = ctx.grantContext().getClient();
        String scopeParameter = ctx.scopeParameter();

        if (realm.isRevokeRefreshToken()) {
            // 启用 refresh token 吊销时需串行化请求，避免并发误判
            // This needs to be called before we load the user session from the database or the cache
            createTemporaryExclusiveLockForTokenRefreshOperation(session, oldRefreshToken, tokenManager);
        }

        event.session(oldRefreshToken.getSessionState())
                .detail(Details.REFRESH_TOKEN_ID, oldRefreshToken.getId())
                .detail(Details.REFRESH_TOKEN_TYPE, oldRefreshToken.getType());

        if (oldRefreshToken.getSubject() != null) {
            event.detail(Details.REFRESH_TOKEN_SUB, oldRefreshToken.getSubject());
        }

        // 从 refresh token 恢复客户端 scope 到上下文
        String oldTokenScope = oldRefreshToken.getScope();
        // 请求的 scope 不得包含资源所有者未 originally 授权的范围
        // 若传入 scope 参数，则过滤掉不在其中的 scope
        if (scopeParameter != null && ! scopeParameter.isEmpty()) {
            Set<String> scopeParamScopes = Arrays.stream(scopeParameter.split(" ")).collect(Collectors.toSet());
            oldTokenScope = Arrays.stream(oldTokenScope.split(" "))
                    .map(transformScopes(session, scopeParamScopes))
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(" "));
        }

        TokenManager.TokenValidation validation = validateToken(session, session.getContext().getUri(), ctx.grantContext().getClientConnection(), realm,
                                                                oldRefreshToken, ctx.grantContext().getHeaders(), oldTokenScope, authorizedClient, tokenManager, event);
        UserModel user = validation.user;
        ClientSessionContext clientSessionCtx = validation.clientSessionCtx;
        UserSessionModel userSession = validation.userSession;

        tokenManager.validateSelectedOrganization(session, oldRefreshToken, user);

        try {
            TokenVerifier.createWithoutSignature(oldRefreshToken)
                    .withChecks(TokenManager.NotBeforeCheck.forModel(realm), TokenManager.NotBeforeCheck.forModel(authorizedClient), TokenManager.NotBeforeCheck.forModel(session, realm, user))
                    .verify();
        } catch (VerificationException e) {
            throw new OAuthErrorException(OAuthErrorException.INVALID_GRANT, "Stale token");
        }

        // 检查用户是否已撤销先前授权 consent
        if (!TokenManager.verifyConsentStillAvailable(session, user, authorizedClient, clientSessionCtx.getClientSession(), oldTokenScope)) {
            throw new OAuthErrorException(OAuthErrorException.INVALID_SCOPE, "Client no longer has requested consent from user");
        }

        if (oldRefreshToken.getNonce() != null) {
            clientSessionCtx.setAttribute(OIDCLoginProtocol.NONCE_PARAM, oldRefreshToken.getNonce());
        }
        clientSessionCtx.setAttribute(Constants.GRANT_TYPE, OAuth2Constants.REFRESH_TOKEN);

        // 重建 access token
        AccessToken newToken = tokenManager.createClientAccessToken(session, realm, authorizedClient, user, userSession, clientSessionCtx, userSession.isOffline());

        session.getContext().setUserSession(validation.userSession);
        AuthenticatedClientSessionModel clientSession = validation.clientSessionCtx.getClientSession();
        OIDCAdvancedConfigWrapper clientConfig = OIDCAdvancedConfigWrapper.fromClientModel(authorizedClient);

        // 校验授权客户端与令牌内客户端一致
        if (!clientSession.getClient().getId().equals(authorizedClient.getId())) {
            throw new OAuthErrorException(OAuthErrorException.INVALID_GRANT, "Invalid refresh token. Token client and authorized client don't match");
        }

        validateTokenReuseForRefresh(session, realm, oldRefreshToken, validation, tokenManager);

        event.user(validation.userSession.getUser());

        if (oldRefreshToken.getAuthorization() != null) {
            newToken.setAuthorization(oldRefreshToken.getAuthorization());
        }

        final Collection<String> requestedAud = (Collection<String>) oldRefreshToken.getOtherClaims().get(Constants.REQUESTED_AUDIENCE);
        if (requestedAud != null) {
            validation.clientSessionCtx.setAttribute(Constants.REQUESTED_AUDIENCE_CLIENTS,
                    requestedAud.stream()
                            .map(clientId -> session.clients().getClientByClientId(realm, clientId))
                            .filter(Objects::nonNull)
                            .toArray(ClientModel[]::new));
        }

        validation.clientSessionCtx.setAttribute(OAuth2Constants.RESOURCE, ctx.resourceParameter());

        TokenManager.AccessTokenResponseBuilder responseBuilder = tokenManager.responseBuilder(realm, authorizedClient, event, session,
                validation.userSession, validation.clientSessionCtx).offlineToken( TokenUtil.TOKEN_TYPE_OFFLINE.equals(oldRefreshToken.getType())).accessToken(newToken);

        // 将 authorization_details 从 refresh token 复制到新 access token 与响应
        List<AuthorizationDetailsJSONRepresentation> authorizationDetails = oldRefreshToken.getAuthorizationDetails();
        if (authorizationDetails != null) {
            newToken.setAuthorizationDetails(authorizationDetails);
            validation.clientSessionCtx.setAttribute(AUTHORIZATION_DETAILS_RESPONSE, authorizationDetails);
        }

        if (clientConfig.isUseRefreshToken()) {
            // 新 refresh token 须与旧令牌保持相同 type、scope 与过期策略
            responseBuilder.generateRefreshToken(oldRefreshToken, clientSession);
        }

        if (newToken.getAuthorization() != null
                && clientConfig.isUseRefreshToken()) {
            responseBuilder.getRefreshToken().setAuthorization(newToken.getAuthorization());
        }

        String scopeParam = clientSession.getNote(OAuth2Constants.SCOPE);
        if (TokenUtil.isOIDCRequest(scopeParam)) {
            responseBuilder.generateIDToken().generateAccessTokenHash();
        }

        storeRefreshTimingInformation(event, oldRefreshToken, newToken);

        responseBuilder.requestRefreshToken(oldRefreshToken);

        afterRefreshTokenGenerated(ctx, responseBuilder);

        return responseBuilder;
    }

    /**
     * 特定 refresh token 提供者类型的校验逻辑（如用户会话是否仍存在且未过期）。
     *
     * @return token validation with successful context information
     * @throws OAuthErrorException In case that some validation failed
     */
    protected abstract TokenManager.TokenValidation validateToken(KeycloakSession session, UriInfo uriInfo, ClientConnection connection, RealmModel realm,
                                                                  RefreshToken oldToken, HttpHeaders headers, String scope, ClientModel client,
                                                                  TokenManager tokenManager, EventBuilder event) throws OAuthErrorException;


    /**
     * 新 refresh token 生成后的回调，供子类更新会话等状态
     *
     * @param ctx 刷新上下文
     * @param responseBuilder 已填充 refresh token 与客户端会话上下文的响应构建器
     */
    protected abstract void afterRefreshTokenGenerated(RefreshTokenContext ctx, TokenManager.AccessTokenResponseBuilder responseBuilder);

    /** 基于 access token 创建 refresh token 并分配 ID 与签发时间 */
    protected RefreshToken createRefreshToken(AccessToken accessToken, AccessToken.Confirmation confirmation, String provider) {
        RefreshToken refreshToken = new RefreshToken(accessToken, confirmation, provider);
        refreshToken.id(SecretGenerator.getInstance().generateSecureID());
        refreshToken.issuedNow();
        return refreshToken;
    }

    private Function<String, String> transformScopes(KeycloakSession session, Set<String> requestedScopes) {
        return scope -> {
            if (requestedScopes.contains(scope)) {
                return scope;
            }

            if (Profile.isFeatureEnabled(Profile.Feature.ORGANIZATION)) {
                OrganizationScope oldScope = OrganizationScope.valueOfScope(session, scope);
                return oldScope == null ? null : oldScope.resolveName(session, requestedScopes, scope);
            }

            return null;
        };
    }

    private void createTemporaryExclusiveLockForTokenRefreshOperation(KeycloakSession session, RefreshToken refreshToken, TokenManager tokenManager) {
        String lockId = "refreshLock:" + refreshToken.getSessionId() + ":" + tokenManager.getReuseIdKey(refreshToken);
        Retry.executeWithBackoff((int iteration) -> {
            // 假定刷新操作最长 60 秒
            if (!session.singleUseObjects().putIfAbsent(lockId, 60)) {
                throw new RuntimeException("Unable to acquire serialization lock for token refresh");
            }

            // 触发会话 provider，确保其在 enlistAfterCompletion 中优先登记
            session.sessions();

            KeycloakSessionFactory factory = session.getKeycloakSessionFactory();
            session.getTransactionManager().enlistAfterCompletion(new AbstractKeycloakTransaction() {
                @Override
                protected void commitImpl() {
                    KeycloakModelUtils.runJobInTransaction(factory, s -> s.singleUseObjects().remove(lockId));
                }

                @Override
                protected void rollbackImpl() {
                    KeycloakModelUtils.runJobInTransaction(factory, s -> s.singleUseObjects().remove(lockId));
                }
            });
        }, Duration.of(10, ChronoUnit.SECONDS), 10);
    }

    /**
     * 记录刷新时序信息，用于识别过早刷新令牌的客户端。
     */
    private void storeRefreshTimingInformation(EventBuilder event, RefreshToken refreshToken, AccessToken newToken) {
        long expirationAccessToken = newToken.getExp() - newToken.getIat();
        long ageOfRefreshToken = newToken.getIat() - refreshToken.getIat();
        event.detail(Details.ACCESS_TOKEN_EXPIRATION_TIME, Long.toString(expirationAccessToken));
        event.detail(Details.AGE_OF_REFRESH_TOKEN, Long.toString(ageOfRefreshToken));
    }

    private void validateTokenReuseForRefresh(KeycloakSession session, RealmModel realm, RefreshToken refreshToken,
                                              TokenManager.TokenValidation validation, TokenManager tokenManager) throws OAuthErrorException {
        if (realm.isRevokeRefreshToken()) {
            AuthenticatedClientSessionModel clientSession = validation.clientSessionCtx.getClientSession();
            try {
                tokenManager.validateTokenReuse(session, realm, refreshToken, clientSession, true);
                String key = tokenManager.getReuseIdKey(refreshToken);
                int currentCount = clientSession.getRefreshTokenUseCount(key);
                clientSession.setRefreshTokenUseCount(key, currentCount + 1);
            } catch (OAuthErrorException oee) {
                // 重用检测失败：记录调试信息并将客户端会话从用户会话分离
                            refreshToken.getId(), realm.getName(), clientSession.getClient().getClientId(), clientSession.getUserSession().getUser().getUsername(), clientSession.getUserSession().getId());
                }
                clientSession.detachFromUserSession();
                throw oee;
            }
        }
    }
}
