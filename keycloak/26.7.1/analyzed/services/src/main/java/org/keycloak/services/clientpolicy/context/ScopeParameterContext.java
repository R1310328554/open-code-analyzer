package org.keycloak.services.clientpolicy.context;

import org.keycloak.services.clientpolicy.ClientPolicyContext;

/**
 * 携带 scope 参数的 {@link ClientPolicyContext}：供底层条件与 Executor 读取请求中使用的 scope。
 * <p>常见于令牌刷新、令牌交换等需按 scope 评估策略的事件。</p>
 */
public interface ScopeParameterContext extends ClientPolicyContext {

    /** @return 请求中的 scope 参数字符串 */
    String getScopeParameter();
}
