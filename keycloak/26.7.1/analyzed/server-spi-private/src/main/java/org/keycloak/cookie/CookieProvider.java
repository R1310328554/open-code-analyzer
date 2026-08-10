package org.keycloak.cookie;

import org.keycloak.provider.Provider;

/**
 * Cookie 读写提供者 SPI，封装 HTTP 请求/响应中的 Cookie 设置、读取与过期。
 */
public interface CookieProvider extends Provider {

    /** 按 {@link CookieType} 默认 max-age 写入 Cookie 值。 */
    void set(CookieType cookieType, String value);

    /** 写入 Cookie 并指定存活秒数（{@code maxAge}）。 */
    void set(CookieType cookieType, String value, int maxAge);

    /** 从当前请求读取指定类型的 Cookie 值，不存在时返回 {@code null}。 */
    String get(CookieType cookieType);

    /** 使指定类型的 Cookie 立即过期（清除客户端存储）。 */
    void expire(CookieType cookieType);

}
