package org.keycloak.cookie;

/**
 * Cookie {@code Max-Age} 常用取值常量（秒）。
 * <p>{@link #SESSION} 表示会话 Cookie；{@link #EXPIRED} 表示立即过期。</p>
 */
public interface CookieMaxAge {

    /** 立即过期（Max-Age=0）。 */
    int EXPIRED = 0;

    /** 会话 Cookie（浏览器关闭即失效）。 */
    int SESSION = -1;

    /** 一年（365 天）秒数，用作长期 Cookie 上限参考。 */
    int YEAR = 365 * 24 * 60 * 60;

}
