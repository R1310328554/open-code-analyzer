package org.keycloak.protocol.oidc.refresh;

import org.keycloak.OAuthErrorException;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.provider.Provider;
import org.keycloak.representations.RefreshToken;

/**
 * 刷新令牌提供者 SPI：负责 refresh token 校验与新 refresh token 签发。
 * <p>Keycloak 按工厂优先级遍历可用提供者，首个 {@link #supports} 返回 true 的实例处理请求。</p>
 */
public interface RefreshTokenProvider extends Provider {

    /**
     * 初始签发 refresh token 时调用（通常在用户认证成功后）。
     *
     * @param initialRefreshTokenCtx 用于判断是否由本提供者签发的上下文
     * @return 若返回 true，将调用 {@link #generateRefreshToken(InitialRefreshTokenContext)}；Keycloak 按 ProviderFactory order 遍历提供者
     */
    boolean supports(InitialRefreshTokenContext initialRefreshTokenCtx);

    /**
     * 由 {@link #supports(InitialRefreshTokenContext)} 选中的唯一提供者执行初始签发。
     *
     * @param initialRefreshTokenCtx 签发所需上下文
     * @return 新签发的 refresh token
     * @throws RefreshTokenException 签发失败时
     */
    RefreshToken generateRefreshToken(InitialRefreshTokenContext initialRefreshTokenCtx) throws RefreshTokenException;

    /**
     * refresh_token 请求期间调用，判断是否支持校验上下文中的 refresh token。
     *
     * @param ctx 含旧 refresh token 等数据的上下文
     * @return 若本提供者能处理该 refresh token 则返回 true
     */
    boolean supports(RefreshTokenContext ctx);

    /**
     * refresh_token 请求期间执行：校验旧 refresh token 并在成功后构建新令牌响应。
     *
     * @param ctx 含旧 refresh token 等数据的上下文
     * @return 成功时的新 access/refresh token 响应构建器
     * @throws OAuthErrorException 校验失败或刷新过程出错
     */
    TokenManager.AccessTokenResponseBuilder refreshAccessToken(RefreshTokenContext ctx) throws OAuthErrorException;

    @Override
    default void close() {
    }

}
