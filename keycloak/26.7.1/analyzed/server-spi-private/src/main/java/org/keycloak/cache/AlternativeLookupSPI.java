package org.keycloak.cache;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 替代查找 SPI 注册项，绑定 {@link AlternativeLookupProvider} 与 {@link AlternativeLookupProviderFactory}。
 */
public class AlternativeLookupSPI implements Spi {

    /** 内部 SPI。 */
    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public String getName() {
        return "alternativeLookup";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return AlternativeLookupProvider.class;
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return AlternativeLookupProviderFactory.class;
    }
}
