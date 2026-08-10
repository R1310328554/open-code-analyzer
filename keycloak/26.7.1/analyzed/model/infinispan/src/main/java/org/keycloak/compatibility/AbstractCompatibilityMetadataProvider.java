package org.keycloak.compatibility;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.keycloak.Config;

/**
 * 兼容性元数据提供者的抽象基类：从 SPI 配置作用域收集键值对元数据。
 * <p>
 * 子类通过 {@link #isEnabled} 控制是否导出元数据，通过 {@link #configKeys} 与
 * {@link #customMeta} 定制导出内容，供升级/兼容性检查工具使用。
 */
public abstract class AbstractCompatibilityMetadataProvider implements CompatibilityMetadataProvider {

    /** SPI 名称，同时作为 {@link #getId()} 返回值。 */
    final String spi;
    /** 对应 provider 的配置作用域。 */
    protected final Config.Scope config;

    /**
     * @param spi        SPI 标识
     * @param providerId 提供者 ID
     */
    public AbstractCompatibilityMetadataProvider(String spi, String providerId) {
        this.spi = spi;
        this.config = Config.scope(spi, providerId);
    }

    /** 子类实现：判断当前配置下是否应导出兼容性元数据。 */
    abstract protected boolean isEnabled(Config.Scope scope);

    /** 收集已启用时的兼容性元数据（自定义项 + 配置键值）。 */
    @Override
    public Map<String, String> metadata() {
        if (!isEnabled(config))
            return Map.of();

        Map<String, String> metadata = new HashMap<>(customMeta());
        configKeys().forEach(key -> {
            String value = config.get(key);
            if (value != null)
                metadata.put(remapConfigKey(key), value);
        });
        return metadata;
    }

    /** 将配置键重映射为导出元数据的键名，默认保持原名。 */
    protected String remapConfigKey(String key) {
        return key;
    }

    @Override
    public String getId() {
        return spi;
    }

    /** 子类可覆盖以添加非配置项的自定义元数据。 */
    protected Map<String, String> customMeta() {
        return Map.of();
    }

    /** 子类可覆盖以指定需要从配置中导出的键列表。 */
    protected Stream<String> configKeys() {
        return Stream.of();
    }
}
