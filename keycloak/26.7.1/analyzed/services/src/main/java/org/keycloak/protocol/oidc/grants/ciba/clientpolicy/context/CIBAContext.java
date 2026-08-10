package org.keycloak.protocol.oidc.grants.ciba.clientpolicy.context;

import org.keycloak.models.ClientModel;
import org.keycloak.protocol.oidc.grants.ciba.channel.CIBAAuthenticationRequest;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.context.ClientModelContext;
import org.keycloak.services.clientpolicy.context.ScopeParameterContext;

/**
 * CIBA 客户端策略上下文接口：在后台认证/令牌请求与响应阶段向 Executor 暴露解析后的认证请求。
 * <p>继承 {@link ScopeParameterContext} 与 {@link ClientModelContext}，默认从 {@link #getParsedRequest()} 推导客户端与 scope。</p>
 */
public interface CIBAContext extends ScopeParameterContext, ClientModelContext {

    /** @return 解析后的 CIBA 认证请求，可能为 null */
    CIBAAuthenticationRequest getParsedRequest();

    /** 从解析请求中获取客户端模型 */
    @Override
    default ClientModel getClient() {
        if (getParsedRequest() == null) return null;
        return getParsedRequest().getClient();
    }

    /** 从解析请求中获取 scope 参数 */
    @Override
    default String getScopeParameter() {
        if (getParsedRequest() == null) return null;
        return getParsedRequest().getScope();
    }
}
