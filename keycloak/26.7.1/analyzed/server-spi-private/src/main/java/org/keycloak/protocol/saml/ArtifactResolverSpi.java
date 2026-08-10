package org.keycloak.protocol.saml;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * SAML Artifact 解析器 SPI：注册 {@link ArtifactResolver} 与 {@link ArtifactResolverFactory}。
 */
public class ArtifactResolverSpi implements Spi {
    /** @return 内部 SPI */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** @return SPI 名称 {@code saml-artifact-resolver} */
    @Override
    public String getName() {
        return "saml-artifact-resolver";
    }

    /** @return 提供者接口 {@link ArtifactResolver} */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return ArtifactResolver.class;
    }

    /** @return 工厂接口 {@link ArtifactResolverFactory} */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return ArtifactResolverFactory.class;
    }
}
