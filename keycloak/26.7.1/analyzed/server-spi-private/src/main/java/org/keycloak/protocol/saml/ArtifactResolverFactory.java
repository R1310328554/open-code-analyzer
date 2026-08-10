package org.keycloak.protocol.saml;

import org.keycloak.provider.ProviderFactory;

/**
 * {@link ArtifactResolver} 工厂：创建 SAML Artifact 解析器实例。
 * <p>A factory that creates {@link ArtifactResolver} instances.</p>
 */
public interface ArtifactResolverFactory extends ProviderFactory<ArtifactResolver> {
}
