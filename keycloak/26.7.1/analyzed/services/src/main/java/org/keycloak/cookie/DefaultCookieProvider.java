package org.keycloak.cookie;

import java.util.Map;

import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.NewCookie;

import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.utils.SecureContextResolver;

import org.jboss.logging.Logger;

/**
 * 默认 {@link CookieProvider}：读写会话 Cookie，自动处理 Secure、SameSite、HttpOnly 与 Path。
 * <p>非安全上下文下降级 SameSite=None，并记录代理/HTTPS 配置警告。</p>
 */
public class DefaultCookieProvider implements CookieProvider {

    private static final Logger logger = Logger.getLogger(DefaultCookieProvider.class);

    private final KeycloakSession session;

    /** 解析各 Cookie 类型 Path 的辅助器。 */
    private final CookiePathResolver pathResolver;

    /** 当前请求是否视为安全上下文（影响 Secure/SameSite）。 */
    private final boolean secure;
    private boolean warned;

    private final Map<String, Cookie> cookies;

    /** 从会话上下文初始化，并清理已废弃的旧 Cookie。 */
    public DefaultCookieProvider(KeycloakSession session) {
        KeycloakContext context = session.getContext();

        this.session = session;
        this.cookies = context.getRequestHeaders().getCookies();
        this.pathResolver = new CookiePathResolver(context);
        this.secure = SecureContextResolver.isSecureContext(session);

        if (logger.isTraceEnabled()) {
            logger.tracef("Received cookies: %s, path: %s", String.join(", ", this.cookies.keySet()), context.getUri().getRequestUri().getRawPath());
        }

        expireOldUnusedCookies();
    }

    @Override
    /** 使用 Cookie 类型默认 max-age 写入值。 */
    public void set(CookieType cookieType, String value) {
        if (cookieType.getDefaultMaxAge() == null) {
            throw new IllegalArgumentException(cookieType + " has no default max-age");
        }

        set(cookieType, value, cookieType.getDefaultMaxAge());
    }

    @Override
    /** 写入 Cookie，非安全上下文将 SameSite=None 降级为 LAX。 */
    public void set(CookieType cookieType, String value, int maxAge) {
        String name = cookieType.getName();
        NewCookie.SameSite sameSite = cookieType.getScope().getSameSite();
        if (NewCookie.SameSite.NONE.equals(sameSite) && !secure) {
            sameSite = NewCookie.SameSite.LAX;
        }

        String path = pathResolver.resolvePath(cookieType);
        boolean httpOnly = cookieType.getScope().isHttpOnly();

        NewCookie newCookie = new NewCookie.Builder(name)
                .version(1)
                .value(value)
                .path(path)
                .maxAge(maxAge)
                .secure(secure)
                .httpOnly(httpOnly)
                .sameSite(sameSite)
                .build();

        session.getContext().getHttpResponse().setCookieIfAbsent(newCookie);

        logger.tracef("Setting cookie: name: %s, path: %s, same-site: %s, secure: %s, http-only: %s, max-age: %d", name, path, sameSite, secure, httpOnly, maxAge);

        if (!secure && !warned) {
            warned = true;

            StringBuilder warning = new StringBuilder("Non-secure context detected; cookies are not secured, and will not be available in cross-origin POST requests.");

            String forwarded = session.getContext().getRequestHeaders().getHeaderString("Forwarded");
            String xForwarded = session.getContext().getRequestHeaders().getHeaderString("X-Forwarded-Proto");

            // 非安全上下文常见原因：
            //   passthrough/reencrypt 模式下代理头配置错误；
            //   edge 模式代理未设置 X-Forwarded-Proto；
            //   直连 HTTP 且未启用 HTTPS

            if (forwarded != null || xForwarded != null) {
                if (session.getContext().getHttpRequest().isProxyTrusted()) {
                    warning.append(" Please review your proxy settings as the request appears to have originated from a proxy.");
                } else {
                    warning.append(" This is likely due to the proxy not being trusted.");
                }
            } else {
                warning.append(" Please review whether this direct HTTP usage is expected.");
            }

            logger.warnf(warning.toString());
        }
    }

    @Override
    /** @return 请求中同名 Cookie 的值，不存在则 null */
    public String get(CookieType cookieType) {
        Cookie cookie = cookies.get(cookieType.getName());
        return cookie != null ? cookie.getValue() : null;
    }

    @Override
    /** 若请求携带该 Cookie，则下发 max-age=0 使其失效。 */
    public void expire(CookieType cookieType) {
        String cookieName = cookieType.getName();
        Cookie cookie = cookies.get(cookieName);
        if (cookie != null) {
            String path = pathResolver.resolvePath(cookieType);
            NewCookie newCookie = new NewCookie.Builder(cookieName)
                    .version(1)
                    .path(path)
                    .maxAge(CookieMaxAge.EXPIRED)
                    .build();

            session.getContext().getHttpResponse().setCookieIfAbsent(newCookie);

            logger.tracef("Expiring cookie: name: %s, path: %s", cookie.getName(), path);
        }
    }

    /** 启动时清理 {@link CookieType#OLD_UNUSED_COOKIES} 中的遗留 Cookie。 */
    private void expireOldUnusedCookies() {
        for (CookieType cookieType : CookieType.OLD_UNUSED_COOKIES) {
            expire(cookieType);
        }
    }

    @Override
    public void close() {
    }

}
