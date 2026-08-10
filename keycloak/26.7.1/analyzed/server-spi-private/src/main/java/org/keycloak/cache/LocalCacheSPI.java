package org.keycloak.cache;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 本地缓存 SPI 注册项，绑定 {@link LocalCacheProvider} 与 {@link LocalCacheProviderFactory}。
 */
public class LocalCacheSPI implements Spi {
    /** 内部 SPI。 */
    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public String getName() {
        return "localCache";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return LocalCacheProvider.class;
    }

    @Override
    public Class<? extends ProviderFactory<?>> getProviderFactoryClass() {
        return LocalCacheProviderFactory.class;
    }
}
