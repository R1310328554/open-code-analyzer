package org.keycloak.theme;

import org.keycloak.models.ThemeManager;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 主题管理器 SPI：注册 {@link ThemeManager} 及 {@link ThemeManagerFactory}。
 * <p>内部 SPI，名称 {@code themeManager}。</p>
 */
public class ThemeManagerSpi implements Spi {

    /** @return 始终为 {@code true}，表示内部 SPI */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** @return SPI 名称 {@code themeManager} */
    @Override
    public String getName() {
        return "themeManager";
    }

    /** @return 提供者接口 {@link ThemeManager} */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return ThemeManager.class;
    }

    /** @return 工厂接口 {@link ThemeManagerFactory} */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return ThemeManagerFactory.class;
    }
}
