package org.keycloak.cookie;

/**
 * Cookie 路径作用域：决定 Set-Cookie 的 {@code Path} 属性基于 realm 还是当前请求路径。
 */
public enum CookiePath {
    REALM,
    REQUEST
}
