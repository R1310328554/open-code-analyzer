package org.keycloak.jgroups.certificates;

import java.util.stream.Stream;

import org.keycloak.Config;
import org.keycloak.compatibility.AbstractCompatibilityMetadataProvider;
import org.keycloak.infinispan.util.InfinispanUtils;
import org.keycloak.spi.infinispan.JGroupsCertificateProviderSpi;

/**
 * JGroups 证书 SPI 的兼容性元数据提供者。
 * <p>
 * 在嵌入式 Infinispan 场景下暴露 {@link DefaultJGroupsCertificateProviderFactory#ACTIVATED}
 * 配置键，并将 ACTIVATED 映射为 ENABLED 以兼容旧版元数据。
 */
public class JGroupsCertificatesMetadataProvider extends AbstractCompatibilityMetadataProvider {

    public JGroupsCertificatesMetadataProvider() {
        super(JGroupsCertificateProviderSpi.SPI_NAME, DefaultJGroupsCertificateProviderFactory.PROVIDER_ID);
    }

    @Override
    /** 仅在嵌入式 Infinispan 模式下启用元数据导出。 */
    protected boolean isEnabled(Config.Scope scope) {
        return InfinispanUtils.isEmbeddedInfinispan();
    }

    @Override
    public Stream<String> configKeys() {
        return Stream.of(DefaultJGroupsCertificateProviderFactory.ACTIVATED);
    }

    @Override
    /** 将 ACTIVATED 键重映射为 ENABLED 以兼容旧配置。 */
    protected String remapConfigKey(String key) {
        if (DefaultJGroupsCertificateProviderFactory.ACTIVATED.equals(key)) {
            return DefaultJGroupsCertificateProviderFactory.ENABLED;
        }
        return key;
    }
}
