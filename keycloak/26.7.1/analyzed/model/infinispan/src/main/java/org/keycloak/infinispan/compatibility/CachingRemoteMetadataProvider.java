package org.keycloak.infinispan.compatibility;

import java.util.stream.Stream;

import org.keycloak.Config;
import org.keycloak.compatibility.AbstractCompatibilityMetadataProvider;
import org.keycloak.infinispan.util.InfinispanUtils;
import org.keycloak.spi.infinispan.CacheRemoteConfigProviderSpi;
import org.keycloak.spi.infinispan.impl.remote.DefaultCacheRemoteConfigProviderFactory;

/**
 * 远程 Infinispan 缓存兼容性元数据提供者。
 * <p>
 * 在多站点或 clusterless 模式下导出远程服务器主机名与端口等连接配置，
 * 供升级兼容性检查比对远程缓存拓扑是否一致。
 */
public class CachingRemoteMetadataProvider extends AbstractCompatibilityMetadataProvider {

    /** 绑定远程缓存配置 SPI 与默认 provider ID。 */
    public CachingRemoteMetadataProvider() {
        super(CacheRemoteConfigProviderSpi.SPI_NAME, DefaultCacheRemoteConfigProviderFactory.PROVIDER_ID);
    }

    /** {@inheritDoc} 仅在远程 Infinispan 模式下启用。 */
    @Override
    protected boolean isEnabled(Config.Scope scope) {
        return InfinispanUtils.isRemoteInfinispan();
    }

    /** 导出远程 Infinispan 主机名与端口配置键。 */
    @Override
    protected Stream<String> configKeys() {
        return Stream.of(DefaultCacheRemoteConfigProviderFactory.HOSTNAME, DefaultCacheRemoteConfigProviderFactory.PORT);
    }
}
