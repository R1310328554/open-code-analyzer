package org.keycloak.infinispan.compatibility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.compatibility.AbstractCompatibilityMetadataProvider;
import org.keycloak.crypto.JavaAlgorithm;
import org.keycloak.infinispan.util.InfinispanUtils;
import org.keycloak.jose.jws.crypto.HashUtils;
import org.keycloak.spi.infinispan.impl.embedded.CacheConfigurator;

import org.infinispan.commons.util.Version;
import org.infinispan.configuration.cache.HashConfiguration;

import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.CLUSTERED_CACHE_NUM_OWNERS;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.USER_AND_CLIENT_SESSION_CACHES;
import static org.keycloak.spi.infinispan.CacheEmbeddedConfigProviderSpi.SPI_NAME;
import static org.keycloak.spi.infinispan.impl.embedded.DefaultCacheEmbeddedConfigProviderFactory.CONFIG;
import static org.keycloak.spi.infinispan.impl.embedded.DefaultCacheEmbeddedConfigProviderFactory.PROVIDER_ID;
import static org.keycloak.spi.infinispan.impl.embedded.DefaultCacheEmbeddedConfigProviderFactory.STACK;

/**
 * 嵌入式 Infinispan 缓存兼容性元数据提供者。
 * <p>
 * 在集群模式下导出缓存副本数、Infinispan/JGroups 主次版本号以及 Infinispan 配置文件的 SHA-256 摘要，
 * 供升级与跨版本兼容性检查工具比对。
 */
public class CachingEmbeddedMetadataProvider extends AbstractCompatibilityMetadataProvider {

    /** 配置文件不存在或无法读取时写入元数据的占位值。 */
    public static final String CONFIG_FILE_NOT_FOUND = "not_found";

    /** 绑定嵌入式缓存配置 SPI 与默认 provider ID。 */
    public CachingEmbeddedMetadataProvider() {
        super(SPI_NAME, PROVIDER_ID);
    }

    /** {@inheritDoc} 仅在内嵌 Infinispan 模式下启用。 */
    @Override
    protected boolean isEnabled(Config.Scope scope) {
        return InfinispanUtils.isEmbeddedInfinispan();
    }

    /** 收集副本数、版本号及配置文件哈希等自定义元数据。 */
    @Override
    public Map<String, String> customMeta() {
        var meta = new HashMap<String, String>(8);
        var defaultNumOwners = HashConfiguration.NUM_OWNERS.getDefaultValue();
        Arrays.stream(CLUSTERED_CACHE_NUM_OWNERS)
                .map(CacheConfigurator::numOwnerConfigKey)
                .forEach(configKey -> addInt(meta, configKey, defaultNumOwners));
        if (Profile.isFeatureEnabled(Profile.Feature.PERSISTENT_USER_SESSIONS)) {
            // 持久化用户会话强制 num_owners=1，忽略 SPI 中的副本数配置
            Arrays.stream(USER_AND_CLIENT_SESSION_CACHES)
                    .map(CacheConfigurator::numOwnerConfigKey)
                    .forEach(meta::remove);
        }
        meta.put("version", majorMinorOf(Version.getVersion()));
        meta.put("jgroupsVersion", majorMinorOf(org.jgroups.Version.printVersion()));
        meta.put(CONFIG, sha256Of(Paths.get(config.get(CONFIG))));
        return meta;
    }

    /** 导出 JGroups 协议栈名称作为兼容性相关配置键。 */
    @Override
    public Stream<String> configKeys() {
        return Stream.of(STACK);
    }

    /** 将整型配置项写入元数据映射，缺省时使用默认值。 */
    private void addInt(Map<String, String> meta, String configKey, int defaultValue) {
        Optional.ofNullable(config.getInt(configKey, defaultValue))
                .map(String::valueOf)
                .ifPresent(value -> meta.put(configKey, value));
    }

    /** 计算指定文件的 SHA-256 摘要并以 Base64 编码返回；读取失败时返回 {@link #CONFIG_FILE_NOT_FOUND}。 */
    public static String sha256Of(Path filePath) {
        try {
            var hash = HashUtils.hash(JavaAlgorithm.SHA256, Files.readAllBytes(filePath));
            return Base64.getEncoder().encodeToString(hash);
        } catch (IOException e) {
            return CONFIG_FILE_NOT_FOUND;
        }
    }

    /** 从完整版本字符串中提取主次版本号（如 {@code 16.0}）。 */
    public static String majorMinorOf(String version) {
        if (version == null || version.isEmpty()) {
            return version;
        }
        // 仅匹配 "Major.Minor" 段（例如 16.0）
        Matcher matcher = Pattern.compile("^(\\d+\\.\\d+)").matcher(version);
        return matcher.find() ? matcher.group(1) : version;
    }
}
