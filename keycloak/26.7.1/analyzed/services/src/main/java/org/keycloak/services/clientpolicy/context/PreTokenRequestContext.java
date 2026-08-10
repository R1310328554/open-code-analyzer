package org.keycloak.services.clientpolicy.context;

import java.util.Optional;

import jakarta.ws.rs.core.MultivaluedMap;

import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;

import org.jboss.logging.Logger;

import static org.keycloak.OAuth2Constants.CODE;
import static org.keycloak.protocol.oidc.utils.OAuth2CodeParser.CACHE_KEY_PREFIX;

/**
 * 令牌请求预处理客户端策略上下文。
 * <p>在令牌端点完整处理前触发，尽力从授权码解析客户端以便早期策略评估。</p>
 */
public class PreTokenRequestContext implements ClientModelContext {

    /** 日志记录器 */
    private static final Logger LOGGER = Logger.getLogger(PreTokenRequestContext.class);

    /** Keycloak 会话 */
    private final KeycloakSession session;
    /** 令牌端点表单参数 */
    private final MultivaluedMap<String, String> formParams;
    /** 从授权码解析的客户端；可能为 null */
    private ClientModel client;

    /**
     * @param session Keycloak 会话
     * @param formParams 令牌请求表单参数
     */
    public PreTokenRequestContext(KeycloakSession session, MultivaluedMap<String, String> formParams) {
        this.session = session;
        this.formParams = formParams;
    }

    /** {@inheritDoc} @return {@link ClientPolicyEvent#PRE_TOKEN_REQUEST} */
    @Override
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.PRE_TOKEN_REQUEST;
    }

    /** {@inheritDoc} 从授权码尽力解析客户端；解析失败时返回 null */
    public ClientModel getClient() {

        // 尽力从授权码解析客户端 UUID，且不使授权码失效。
        // 以便在完整令牌处理前基于客户端评估策略条件。

        String authCode = formParams.getFirst(CODE);
        if (client == null && authCode != null) {
            String[] parsed = authCode.split("\\.", 3);
            if (parsed.length < 3) {
                LOGGER.debug("授权码格式无效");
                return null;
            }

            String codeUUID = parsed[0];
            String userSessionId = parsed[1];
            String clientUUID = parsed[2];

            // 避免对明显非法或已使用的授权码应用客户端策略。
            if (!session.singleUseObjects().contains(CACHE_KEY_PREFIX + codeUUID)) {
                LOGGER.debug("授权码无效或已被使用");
                return null;
            }

            // 获取用户会话
            RealmModel realm = session.getContext().getRealm();
            UserSessionModel userSession = session.sessions().getUserSession(realm, userSessionId);
            if (userSession == null) {
                LOGGER.debug("授权码无效");
                return null;
            }

            AuthenticatedClientSessionModel clientSession = userSession.getAuthenticatedClientSessionByClient(clientUUID);
            client = Optional.ofNullable(clientSession)
                    .map(AuthenticatedClientSessionModel::getClient)
                    .orElse(null);
            if (client == null) {
                LOGGER.debug("无已认证的客户端会话");
                return null;
            }
        }
        return client;
    }
}
