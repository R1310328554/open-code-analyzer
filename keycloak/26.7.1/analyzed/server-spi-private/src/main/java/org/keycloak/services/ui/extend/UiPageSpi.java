package org.keycloak.services.ui.extend;

import org.keycloak.common.Profile;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 声明式 UI 页面 SPI：注册 {@link UiPageProvider} 及工厂。
 * <p>内部 SPI，名称 {@code ui-page}；需启用 {@link Profile.Feature#DECLARATIVE_UI}。</p>
 */
public class UiPageSpi implements Spi {
    /** @return 始终为 {@code true}，表示内部 SPI */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** @return SPI 名称 {@code ui-page} */
    @Override
    public String getName() {
        return "ui-page";
    }

    /** @return 提供者接口 {@link UiPageProvider} */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return UiPageProvider.class;
    }

    /** @return 工厂接口 {@link UiPageProviderFactory} */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return UiPageProviderFactory.class;
    }

    /** @return 当 {@link Profile.Feature#DECLARATIVE_UI} 启用时返回 {@code true} */
    @Override
    public boolean isEnabled() {
        return Profile.isFeatureEnabled(Profile.Feature.DECLARATIVE_UI);
    }
}
