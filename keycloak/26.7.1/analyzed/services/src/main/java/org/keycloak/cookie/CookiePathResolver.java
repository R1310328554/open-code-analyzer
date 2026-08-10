package org.keycloak.cookie;

import org.keycloak.models.KeycloakContext;
import org.keycloak.services.resources.RealmsResource;

/**
 * 根据 {@link CookieType} 的路径策略解析 Cookie 的 Path 属性。
 * <p>支持按 Realm 根路径或当前请求路径作用域。</p>
 */
class CookiePathResolver {

    /** 当前请求的 Keycloak 上下文。 */
    private final KeycloakContext context;
    private String realmPath;

    private String requestPath;

    /** @param context 用于构造 Realm/请求 URI 路径 */
    CookiePathResolver(KeycloakContext context) {
        this.context = context;
    }

    /** 按 Cookie 类型返回 Path（结果缓存于实例字段）。 */
    String resolvePath(CookieType cookieType) {
        switch (cookieType.getPath()) {
            case REALM:
                if (realmPath == null) {
                    realmPath = RealmsResource.realmBaseUrl(context.getUri()).path("/").build(context.getRealm().getName()).getRawPath();
                }
                return realmPath;
            case REQUEST:
                if (requestPath == null) {
                    requestPath = context.getUri().getRequestUri().getRawPath();
                }
                return requestPath;
            default:
                throw new IllegalArgumentException("Unsupported enum value " + cookieType.getPath().name());
        }
    }

}
