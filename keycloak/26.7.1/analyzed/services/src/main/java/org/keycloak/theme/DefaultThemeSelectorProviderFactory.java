package org.keycloak.theme;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * 默认主题选择器 Provider 工厂。
 * <p>注册 id 为 {@code default} 的 {@link DefaultThemeSelectorProvider}。</p>
 */
public class DefaultThemeSelectorProviderFactory implements ThemeSelectorProviderFactory {

    /** 创建会话级主题选择器实例。 */
    @Override
    public ThemeSelectorProvider create(KeycloakSession session) {
        return new DefaultThemeSelectorProvider(session);
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

    /** 工厂标识符。 */
    @Override
    public String getId() {
        return "default";
    }
}
