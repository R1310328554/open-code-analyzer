package org.keycloak.cache;

import java.util.Set;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;

/**
 * {@link AlternativeLookupProvider} 工厂 SPI，声明对 {@link LocalCacheProvider} 的依赖。
 */
public interface AlternativeLookupProviderFactory extends ProviderFactory<AlternativeLookupProvider> {
    /** 依赖本地缓存提供者以支撑查找结果缓存。 */
    @Override
    default Set<Class<? extends Provider>> dependsOn() {
        return Set.of(LocalCacheProvider.class);
    }
}
