package org.keycloak.cookie;

import jakarta.annotation.Nullable;

/**
 * Keycloak 内置 Cookie 类型定义，通过建造者配置名称、路径、作用域与默认 max-age。
 * <p>各 {@code public static final} 常量对应认证流程、会话、语言偏好等场景。</p>
 */
public final class CookieType {

    /** 已废弃但未清理的旧版 Cookie 名称列表。 */
    public static final CookieType[] OLD_UNUSED_COOKIES = new CookieType[] {
            CookieType.create("AUTH_SESSION_ID_LEGACY").build(),
            CookieType.create("KEYCLOAK_IDENTITY_LEGACY").build(),
            CookieType.create("KEYCLOAK_SESSION_LEGACY").build()
    };

    /** 认证流程 CSRF/状态校验 Cookie（内部作用域）。 */
    public static final CookieType AUTH_DETACHED = CookieType.create("KC_STATE_CHECKER")
            .scope(CookieScope.INTERNAL)
            .build();

    /** 认证重启标记 Cookie，会话级存活。 */
    public static final CookieType AUTH_RESTART = CookieType.create("KC_RESTART")
            .scope(CookieScope.FEDERATION)
            .defaultMaxAge(CookieMaxAge.SESSION)
            .build();

    /** 认证会话 ID 哈希，供 JavaScript 读取，默认 60 秒。 */
    public static final CookieType AUTH_SESSION_ID_HASH = CookieType.create("KC_AUTH_SESSION_HASH")
            .scope(CookieScope.FEDERATION_JS)
            .defaultMaxAge(60)
            .build();

    /** 根认证会话 ID Cookie。 */
    public static final CookieType AUTH_SESSION_ID = CookieType.create("AUTH_SESSION_ID")
            .scope(CookieScope.FEDERATION)
            .defaultMaxAge(CookieMaxAge.SESSION)
            .build();

    /** 用户身份令牌 Cookie。 */
    public static final CookieType IDENTITY = CookieType.create("KEYCLOAK_IDENTITY")
            .scope(CookieScope.FEDERATION)
            .build();

    /** 用户语言偏好 Cookie。 */
    public static final CookieType LOCALE = CookieType.create("KEYCLOAK_LOCALE")
            .scope(CookieScope.FEDERATION)
            .defaultMaxAge(CookieMaxAge.SESSION)
            .build();

    /** 登录提示/记住我 Cookie，默认存活一年。 */
    public static final CookieType LOGIN_HINT = CookieType.create("KEYCLOAK_REMEMBER_ME")
            .scope(CookieScope.FEDERATION)
            .defaultMaxAge(CookieMaxAge.YEAR)
            .build();

    /** 用户会话 Cookie（JavaScript 可读）。 */
    public static final CookieType SESSION = CookieType.create("KEYCLOAK_SESSION")
            .scope(CookieScope.FEDERATION_JS)
            .build();

    /** Welcome 页面 CSRF 校验 Cookie，基于请求路径，默认 300 秒。 */
    public static final CookieType WELCOME_CSRF = CookieType.create("WELCOME_STATE_CHECKER")
            .requestPath()
            .defaultMaxAge(300)
            .build();

    private final String name;
    private final CookiePath path;
    private final CookieScope scope;

    private final Integer defaultMaxAge;

    private CookieType(String name, CookiePath path, CookieScope scope, @Nullable Integer defaultMaxAge) {
        this.name = name;
        this.path = path;
        this.scope = scope;
        this.defaultMaxAge = defaultMaxAge;
    }

    private static CookieTypeBuilder create(String name) {
        return new CookieTypeBuilder(name);
    }

    /** @return Cookie 名称 */
    public String getName() {
        return name;
    }

    /** @return Cookie 路径策略 */
    public CookiePath getPath() {
        return path;
    }

    /** @return Cookie 安全作用域 */
    public CookieScope getScope() {
        return scope;
    }

    /** @return 默认 max-age（秒），未设置时为 {@code null} */
    public Integer getDefaultMaxAge() {
        return defaultMaxAge;
    }

    /** 流式建造者，用于组装不可变 {@link CookieType} 实例。 */
    private static class CookieTypeBuilder {

        private String name;
        private CookiePath path = CookiePath.REALM;
        private CookieScope scope = CookieScope.INTERNAL;
        private Integer defaultMaxAge;

        CookieTypeBuilder(String name) {
            this.name = name;
        }

        /** 将路径策略设为 {@link CookiePath#REQUEST}。 */
        CookieTypeBuilder requestPath() {
            this.path = CookiePath.REQUEST;
            return this;
        }

        /** 设置 Cookie 安全作用域。 */
        CookieTypeBuilder scope(CookieScope scope) {
            this.scope = scope;
            return this;
        }

        /** 设置默认 max-age（秒）。 */
        CookieTypeBuilder defaultMaxAge(int defaultMaxAge) {
            this.defaultMaxAge = defaultMaxAge;
            return this;
        }

        /** 构建不可变 Cookie 类型实例。 */
        CookieType build() {
            return new CookieType(name, path, scope, defaultMaxAge);
        }

    }

}
