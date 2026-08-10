package org.keycloak.cookie;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * 默认 {@link CookieProviderFactory}，SPI ID 为 {@code default}。
 * <p>为每个 {@link KeycloakSession} 创建 {@link DefaultCookieProvider}。</p>
 */
public class DefaultCookieProviderFactory implements CookieProviderFactory {

    @Override
    /** @param session 当前 Keycloak 会话 @return 新的 Cookie 提供者实例 */
    public CookieProvider create(KeycloakSession session) {
        return new DefaultCookieProvider(session);
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    @Override
    /** @return 工厂标识 {@code default} */
    public String getId() {
        return "default";
    }

}
