package org.keycloak.cache;

import org.keycloak.provider.ProviderFactory;

/**
 * {@link LocalCacheProvider} 工厂 SPI，注册具体本地缓存实现（如 Caffeine）。
 */
public interface LocalCacheProviderFactory extends ProviderFactory<LocalCacheProvider> {
}
