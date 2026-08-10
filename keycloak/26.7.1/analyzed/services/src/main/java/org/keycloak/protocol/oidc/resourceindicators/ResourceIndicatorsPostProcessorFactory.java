package org.keycloak.protocol.oidc.resourceindicators;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.token.TokenPostProcessor;
import org.keycloak.protocol.oidc.token.TokenPostProcessorFactory;
import org.keycloak.provider.EnvironmentDependentProviderFactory;

/**
 * 资源指标令牌后处理器工厂：需启用 {@link org.keycloak.common.Profile.Feature#RESOURCE_INDICATORS} 特性。
 */
public class ResourceIndicatorsPostProcessorFactory implements TokenPostProcessorFactory, EnvironmentDependentProviderFactory {

    /** SPI 提供者标识符 */
    public static final String ID = "resource-indicators";

    /** {@inheritDoc} 创建 {@link ResourceIndicatorsPostProcessor} */
    @Override
    public TokenPostProcessor create(KeycloakSession session) {
        return new ResourceIndicatorsPostProcessor(session);
    }

    /** {@inheritDoc} 返回 {@link #ID} */
    @Override
    public String getId() {
        return ID;
    }

    /** {@inheritDoc} 需启用 RESOURCE_INDICATORS 特性 */
    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.RESOURCE_INDICATORS);
    }
}
