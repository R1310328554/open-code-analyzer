package org.keycloak.protocol.oidc.refresh;

import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.protocol.oidc.grants.OAuth2GrantType;
import org.keycloak.protocol.oidc.grants.RefreshTokenGrantType;
import org.keycloak.representations.RefreshToken;

/**
 * refresh_token grant 处理上下文：携带旧 refresh token 与 grant 相关依赖。
 *
 * @param oldRefreshToken 待校验的旧 refresh token
 * @param grantContext OAuth2 grant 上下文（realm、client、事件等）
 * @param grant refresh_token grant 处理器
 * @param tokenManager 令牌管理器
 * @param scopeParameter 请求中的 scope 参数（可为 null）
 * @param resourceParameter 请求中的 resource 指标参数（可为 null）
 */
public record RefreshTokenContext(RefreshToken oldRefreshToken, OAuth2GrantType.Context grantContext, RefreshTokenGrantType grant, TokenManager tokenManager,
                                  String scopeParameter, String resourceParameter) {
}
