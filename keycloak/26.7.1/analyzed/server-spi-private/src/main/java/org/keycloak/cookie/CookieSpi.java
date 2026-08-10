package org.keycloak.cookie;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * Cookie SPI，注册 {@link CookieProvider} 提供者类型。
 */
public class CookieSpi implements Spi {
    /** 内部 SPI，不对扩展模块公开。 */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** SPI 名称：{@code cookie}。 */
    @Override
    public String getName() {
        return "cookie";
    }

    /** Cookie 提供者接口类型。 */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return CookieProvider.class;
    }

    /** Cookie 工厂类型。 */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return CookieProviderFactory.class;
    }
}
