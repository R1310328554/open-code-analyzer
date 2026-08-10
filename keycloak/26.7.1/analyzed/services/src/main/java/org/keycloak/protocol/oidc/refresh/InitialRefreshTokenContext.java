package org.keycloak.protocol.oidc.refresh;

import org.keycloak.events.EventBuilder;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.representations.AccessToken;

/**
 * 初始 refresh token 签发上下文：聚合客户端会话、令牌响应构建器与 DPoP 确认等数据。
 *
 * @param clientSessionCtx 客户端会话上下文
 * @param responseBuilder 访问令牌响应构建器（含已签发的 access token）
 * @param event 事件构建器
 * @param offlineTokenRequested 是否请求 offline token
 * @param confirmation DPoP 或 mTLS 确认信息
 */
public record InitialRefreshTokenContext(ClientSessionContext clientSessionCtx, TokenManager.AccessTokenResponseBuilder responseBuilder,
                                         EventBuilder event, boolean offlineTokenRequested, AccessToken.Confirmation confirmation) {
}
