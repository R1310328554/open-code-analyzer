package org.keycloak.cache;

import java.util.Objects;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.cache.CaffeineStatsCounter;

/**
 * The default implementation for {@link LocalCacheProvider} and {@link LocalCacheProviderFactory}.
 */
/**
 * 基于 Caffeine 的本地缓存 SPI：支持访问/创建过期、最大容量、
 * LoadingCache 与 Micrometer 指标绑定。
 */
public class DefaultLocalCacheProviderFactory implements LocalCacheProvider, LocalCacheProviderFactory {

    /** 工厂即 provider 单例，直接返回 this。 */
    @Override
    public LocalCacheProvider create(KeycloakSession session) {
        return this;
    }

    /** 无全局 SPI 配置。 */
    @Override
    public void init(Config.Scope config) {
    }

    /** 无后置初始化逻辑。 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    /** @return provider id "default" */
    @Override
    public String getId() {
        return "default";
    }

    /** 按配置构建 Caffeine 缓存并注册 Micrometer 统计。 */
    @Override
    public <K, V> LocalCache<K, V> create(LocalCacheConfiguration<K, V> configuration) {
        CaffeineStatsCounter metrics = new CaffeineStatsCounter(Metrics.globalRegistry, configuration.name());
        Caffeine<Object, Object> builder = Caffeine.newBuilder().recordStats(() -> metrics);

        if (configuration.maxSize() > 0) {
            builder.maximumSize(configuration.maxSize());
        }

        switch (configuration.expirationMode()) {
            case ACCESS -> builder.expireAfter(Expiry.accessing(configuration.expiration()));
            case CREATE -> builder.expireAfter(Expiry.creating(configuration.expiration()));
        }

        if (configuration.hasLoader()) {
            LoadingCache<K, V> cache = builder.build(k -> configuration.loader().apply(k));
            metrics.registerSizeMetric(cache);
            return new LoadingCaffeineWrapper<>(cache);
        } else {
            Cache<K, V> cache = builder.build();
            metrics.registerSizeMetric(cache);
            return new CaffeineWrapper<>(cache);
        }
    }

    /** 无全局资源需释放。 */
    @Override
    public void close() {
    }

    /** 非 Loading 模式的 Caffeine {@link LocalCache} 包装。 */
    private static class CaffeineWrapper<K, V> implements LocalCache<K, V> {

        /** 底层 Caffeine 缓存。 */
        final Cache<K, V> cache;

        CaffeineWrapper(Cache<K, V> cache) {
            this.cache = cache;
        }

        /** 仅返回已存在条目，不触发加载。 */
        /** 获取或经 loader 加载值。 */
        /** 仅返回已存在条目，不触发加载。 */
        /** 获取或经 loader 加载值。 */
        @Override
        public V get(K key) {
            Objects.requireNonNull(key);
            return cache.getIfPresent(key);
        }

        /** 写入键值，拒绝 null。 */
        @Override
        public void put(K key, V value) {
            Objects.requireNonNull(key);
            Objects.requireNonNull(value);
            cache.put(key, value);
        }

        /** 使指定键失效。 */
        @Override
        public void invalidate(K key) {
            Objects.requireNonNull(key);
            cache.invalidate(key);
        }

        /** 清理缓存内部结构。 */
        @Override
        public void close() {
            cache.cleanUp();
        }
    }

    /** 带 loader 的 LoadingCache 包装，get 会触发加载。 */
    private static class LoadingCaffeineWrapper<K, V> extends CaffeineWrapper<K, V> {

        final LoadingCache<K, V> cache;

        LoadingCaffeineWrapper(LoadingCache<K, V> cache) {
            super(cache);
            this.cache = cache;
        }

        @Override
        public V get(K key) {
            Objects.requireNonNull(key);
            return cache.get(key);
        }
    }
}
