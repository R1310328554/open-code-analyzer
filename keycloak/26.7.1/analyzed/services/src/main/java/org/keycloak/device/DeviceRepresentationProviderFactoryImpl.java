package org.keycloak.device;

import java.util.List;

import org.keycloak.Config;
import org.keycloak.cache.LocalCache;
import org.keycloak.cache.LocalCacheConfiguration;
import org.keycloak.cache.LocalCacheProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import ua_parser.Client;
import ua_parser.Parser;

/**
 * 设备表示 SPI 默认工厂实现。
 * <p>使用 ua-parser 解析 User-Agent，并通过 {@link LocalCache} 缓存解析结果以提升性能。</p>
 */
public class DeviceRepresentationProviderFactoryImpl implements DeviceRepresentationProviderFactory {

    /** 全局 User-Agent 解析器实例。 */
    private static final Parser UA_PARSER = new Parser();
    /** 配置键：本地缓存最大条目数。 */
    private static final String CACHE_SIZE = "cacheSize";
    // User-Agent 最大 512 字节，每条缓存约占 1KB；默认 2048 条约 2MB。
    /** 默认缓存容量（条目数）。 */
    private static final int DEFAULT_CACHE_SIZE = 2048;
    /** SPI 工厂标识：{@code deviceRepresentation}。 */
    public static final String PROVIDER_ID = "deviceRepresentation";

    private LocalCacheConfiguration<String, Client> cacheConfig;
    private LocalCache<String, Client> cache;

    @Override
    /** @return {@link #PROVIDER_ID} */
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    /** 根据配置构建 User-Agent 本地缓存配置。 */
    public void init(Config.Scope config) {
        cacheConfig = LocalCacheConfiguration.<String, Client>builder()
              .name("userAgent")
              .maxSize(config.getInt(CACHE_SIZE, DEFAULT_CACHE_SIZE))
              .loader(UA_PARSER::parse)
              .build();
    }

    @Override
    /** 通过 {@link LocalCacheProvider} 创建进程级 User-Agent 缓存。 */
    public void postInit(KeycloakSessionFactory factory) {
        try (KeycloakSession session = factory.create()) {
            cache = session.getProvider(LocalCacheProvider.class).create(cacheConfig);
            cacheConfig = null;
        }
    }

    @Override
    /** @param session 当前会话 @return 绑定共享缓存的设备表示提供者 */
    public DeviceRepresentationProvider create(KeycloakSession session) {
        return new DeviceRepresentationProviderImpl(session, cache);
    }

    @Override
    /** @return 缓存大小等可配置项元数据 */
    public List<ProviderConfigProperty> getConfigMetadata() {
        return ProviderConfigurationBuilder.create()
                .property()
                .name(CACHE_SIZE)
                .type(ProviderConfigProperty.INTEGER_TYPE)
                .helpText("Sets the maximum number of parsed user-agent values in the local cache.")
                .defaultValue(DEFAULT_CACHE_SIZE)
                .add()
                .build();
    }

    @Override
    /** 关闭并释放 User-Agent 本地缓存。 */
    public void close() {
        if (cache != null) {
            cache.close();
            cache = null;
        }
    }
}
