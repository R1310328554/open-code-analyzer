package org.keycloak.authentication;

/**
 * 客户端认证流程共享状态的懒加载供应器。
 * <p>由 {@link ClientAuthenticationFlowContext#getState} 在状态尚未存在时调用。</p>
 * @param <T> 状态对象类型
 */
public interface ClientAuthenticationFlowContextSupplier<T> {

    /**
     * 基于当前上下文创建或计算状态对象。
     *
     * @param context 客户端认证流程上下文
     * @return 共享状态实例
     * @throws Exception 初始化失败时抛出
     */
    T get(ClientAuthenticationFlowContext context) throws Exception;

}
