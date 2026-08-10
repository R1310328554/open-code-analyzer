package org.keycloak.services.ui.extend;

import org.keycloak.common.Profile;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 声明式 UI 标签页 SPI：注册 {@link UiTabProvider} 及工厂。
 * <p>内部 SPI，名称 {@code ui-tab}；需启用 {@link Profile.Feature#DECLARATIVE_UI}。</p>
 */
public class UiTabSpi implements Spi {
    /** @return 始终为 {@code true}，表示内部 SPI */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** @return SPI 名称 {@code ui-tab} */
    @Override
    public String getName() {
        return "ui-tab";
    }

    /** @return 提供者接口 {@link UiTabProvider} */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return UiTabProvider.class;
    }

    /** @return 工厂接口 {@link UiTabProviderFactory} */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return UiTabProviderFactory.class;
    }

    /** @return 当 {@link Profile.Feature#DECLARATIVE_UI} 启用时返回 {@code true} */
    @Override
    public boolean isEnabled() {
        return Profile.isFeatureEnabled(Profile.Feature.DECLARATIVE_UI);
    }
}
