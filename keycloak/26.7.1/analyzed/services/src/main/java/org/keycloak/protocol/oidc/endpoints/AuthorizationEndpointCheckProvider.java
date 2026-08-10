package org.keycloak.protocol.oidc.endpoints;

import org.keycloak.protocol.oidc.endpoints.AuthorizationEndpointChecker.AuthorizationCheckException;
import org.keycloak.provider.Provider;

/**
 * 授权端点扩展校验 SPI：在标准 OIDC 授权请求校验之外执行额外检查。
 * <p>实现类通过 {@link AuthorizationEndpointChecker} 访问请求上下文。</p>
 */
public interface AuthorizationEndpointCheckProvider extends Provider {

    /**
     * 执行扩展校验。
     * @param context 授权端点校验器上下文
     * @throws AuthorizationCheckException 校验失败时抛出
     */
    void check(AuthorizationEndpointChecker context) throws AuthorizationCheckException;
}
