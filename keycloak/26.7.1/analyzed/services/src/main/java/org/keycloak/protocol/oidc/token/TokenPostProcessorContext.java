package org.keycloak.protocol.oidc.token;

import org.keycloak.models.ClientSessionContext;
import org.keycloak.protocol.oidc.utils.OAuth2Code;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.RefreshToken;

/**
 * 令牌后处理器上下文。
 * <p>携带授权码、请求中的刷新令牌、新生成的刷新/访问令牌及客户端会话上下文。</p>
 *
 * @param code 授权码
 * @param requestRefreshToken 请求中的刷新令牌
 * @param refreshToken 新生成的刷新令牌
 * @param accessToken 新生成的访问令牌
 * @param clientSessionCtx 客户端会话上下文
 */
public record TokenPostProcessorContext(OAuth2Code code, RefreshToken requestRefreshToken, RefreshToken refreshToken, AccessToken accessToken, ClientSessionContext clientSessionCtx) {
}
