package org.keycloak.cookie;

import jakarta.ws.rs.core.NewCookie;

/**
 * Cookie 安全作用域：定义 SameSite、HttpOnly 等属性组合，区分内部、联邦与遗留策略。
 */
public enum CookieScope {
    // 内部 Cookie：仅对直接访问 Keycloak 的请求可见
    INTERNAL(NewCookie.SameSite.STRICT, true),

    // 内部 Cookie（JavaScript 可读，非 HttpOnly）
    INTERNAL_JS(NewCookie.SameSite.STRICT, false),

    // 联邦 Cookie：应用重定向后可用，iframe 中亦可用（除非浏览器拦截第三方 Cookie）
    FEDERATION(NewCookie.SameSite.NONE, true),

    // 联邦 Cookie（JavaScript 可读）
    FEDERATION_JS(NewCookie.SameSite.NONE, false),

    // 遗留 Cookie：不设置 SameSite，现代浏览器默认为 Lax
    @Deprecated
    LEGACY(null, true),

    // 遗留 Cookie（JavaScript 可读）
    @Deprecated
    LEGACY_JS(null, false);

    private final NewCookie.SameSite sameSite;
    private final boolean httpOnly;

    /** @param sameSite SameSite 策略，遗留类型可为 {@code null} */
    /** @param httpOnly 是否禁止 JavaScript 访问 */
    CookieScope(NewCookie.SameSite sameSite, boolean httpOnly) {
        this.sameSite = sameSite;
        this.httpOnly = httpOnly;
    }

    /** @return 该作用域对应的 SameSite 属性 */
    public NewCookie.SameSite getSameSite() {
        return sameSite;
    }

    /** @return 是否启用 HttpOnly 标志 */
    public boolean isHttpOnly() {
        return httpOnly;
    }
}
