package org.keycloak.protocol.oidc.token;

import org.keycloak.provider.Provider;

/**
 * 令牌后处理器。
 * <p>在访问令牌/刷新令牌签发后、响应返回前对令牌进行拦截与修改。</p>
 */
public interface TokenPostProcessor extends Provider {

    /**
     * 处理令牌上下文。
     * @param context 包含授权码、请求/响应令牌及客户端会话上下文
     * @throws TokenInterceptorException 处理失败时
     */
    void process(TokenPostProcessorContext context);

    /** 关闭资源（默认无操作） */
    default void close() {}
}
