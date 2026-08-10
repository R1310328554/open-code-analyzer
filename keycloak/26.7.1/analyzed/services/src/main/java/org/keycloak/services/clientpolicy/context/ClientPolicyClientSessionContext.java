package org.keycloak.services.clientpolicy.context;

import org.keycloak.OAuth2Constants;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.services.clientpolicy.ClientPolicyContext;

/**
 * 携带 {@link AuthenticatedClientSessionModel} 的 {@link ClientPolicyContext}：供条件/Executor 访问已认证客户端会话。
 * <p>默认从客户端会话解析 {@link ClientModel} 与 scope 参数。</p>
 */
public interface ClientPolicyClientSessionContext extends ClientModelContext, ScopeParameterContext {

    /** @return 已认证的客户端会话 */
    AuthenticatedClientSessionModel getClientSession();

    /** {@inheritDoc} 从客户端会话获取客户端 */
    @Override
    default ClientModel getClient() {
        return getClientSession().getClient();
    }

    /** {@inheritDoc} 从客户端会话 note 读取 scope */
    @Override
    default String getScopeParameter() {
        return getClientSession().getNote(OAuth2Constants.SCOPE);
    }

}
