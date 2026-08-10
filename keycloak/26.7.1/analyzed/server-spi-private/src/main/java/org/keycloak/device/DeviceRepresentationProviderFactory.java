package org.keycloak.device;

import java.util.Set;

import org.keycloak.Config;
import org.keycloak.cache.LocalCacheProvider;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;

/**
 * {@link DeviceRepresentationProvider} 的 {@link ProviderFactory} 工厂接口。
 * <p>依赖 {@link LocalCacheProvider} 以缓存 User-Agent 解析结果。</p>
 */
public interface DeviceRepresentationProviderFactory extends ProviderFactory<DeviceRepresentationProvider> {

    /** 默认空实现。 */
    @Override
    default void init(Config.Scope config) {
    }

    /** 默认空实现。 */
    @Override
    default void postInit(KeycloakSessionFactory factory) {
    }

    /** 默认空实现。 */
    @Override
    default void close() {
    }

    /** 声明依赖本地缓存提供者。 */
    @Override
    default Set<Class<? extends Provider>> dependsOn() {
        return Set.of(LocalCacheProvider.class);
    }
}
