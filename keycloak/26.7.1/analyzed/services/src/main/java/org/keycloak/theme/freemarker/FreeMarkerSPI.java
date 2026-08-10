package org.keycloak.theme.freemarker;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * FreeMarker 模板 SPI 注册。
 * <p>将 {@link FreeMarkerProvider} / {@link FreeMarkerProviderFactory} 注册为内部 SPI。</p>
 */
public class FreeMarkerSPI implements Spi {
    /** 标记为 Keycloak 内部 SPI。 */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** 返回 SPI 名称 {@code freemarker}。 */
    @Override
    public String getName() {
        return "freemarker";
    }

    /** 返回 Provider 接口类型。 */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return FreeMarkerProvider.class;
    }

    /** 返回 ProviderFactory 接口类型。 */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return FreeMarkerProviderFactory.class;
    }
}
