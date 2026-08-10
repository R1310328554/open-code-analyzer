package org.keycloak.cache;

import java.time.Duration;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * 默认替代查找 SPI 工厂：启动时创建共享 Caffeine 查找缓存，
 * 配置 maximumSize 与 expireAfter（分钟）访问过期。
 */
public class DefaultAlternativeLookupProviderFactory implements AlternativeLookupProviderFactory {

    /** 查找缓存配置（postInit 后置 null）。 */
    private LocalCacheConfiguration<String, CachedValue> cacheConfig;
    /** 全局共享查找缓存实例。 */
    private LocalCache<String, CachedValue> lookupCache;

    /** @return provider id "default" */
    @Override
    public String getId() {
        return "default";
    }

    /** 为会话创建绑定共享缓存的 {@link DefaultAlternativeLookupProvider}。 */
    @Override
    public AlternativeLookupProvider create(KeycloakSession session) {
        return new DefaultAlternativeLookupProvider(lookupCache);
    }

    /** 读取 maximumSize（默认 1000）与 expireAfter 分钟（默认 60）。 */
    @Override
    public void init(Config.Scope config) {
        Integer maximumSize = config.getInt("maximumSize", 1000);
        Integer expireAfter = config.getInt("expireAfter", 60);

        cacheConfig = LocalCacheConfiguration.<String, CachedValue>builder()
              .name("lookup")
              .expirationAfterAccess(Duration.ofMinutes(expireAfter))
              .maxSize(maximumSize)
              .build();
    }

    /** 通过 {@link LocalCacheProvider} 实例化 lookup 缓存。 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
        try (KeycloakSession session = factory.create()) {
            lookupCache = session.getProvider(LocalCacheProvider.class).create(cacheConfig);
            cacheConfig = null;
        }
    }

    /** 关闭并释放查找缓存。 */
    @Override
    public void close() {
        if (lookupCache != null) {
            lookupCache.close();
            lookupCache = null;
        }
    }
}
