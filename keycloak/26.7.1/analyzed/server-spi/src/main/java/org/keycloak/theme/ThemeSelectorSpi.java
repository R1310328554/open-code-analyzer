package org.keycloak.theme;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/** 主题选择器 SPI：注册 {@link ThemeSelectorProvider} 及其工厂。 */
public class ThemeSelectorSpi implements Spi {

    @Override
    /** @return 是否为内部 SPI（本 SPI 对外可见） */
    public boolean isInternal() {
        return false;
    }

    @Override
    /** @return SPI 名称 {@code themeSelector} */
    public String getName() {
        return "themeSelector";
    }

    @Override
    /** @return Provider 接口类型 */
    public Class<? extends Provider> getProviderClass() {
        return ThemeSelectorProvider.class;
    }

    @Override
    /** @return ProviderFactory 接口类型 */
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return ThemeSelectorProviderFactory.class;
    }
}
