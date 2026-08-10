package org.keycloak.organization.protocol.mappers.oidc;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.common.Profile.Feature;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.token.TokenPostProcessor;
import org.keycloak.protocol.oidc.token.TokenPostProcessorFactory;
import org.keycloak.provider.EnvironmentDependentProviderFactory;

/**
 * 组织令牌后处理器的 {@link TokenPostProcessorFactory}，工厂 ID 为 {@code organizations}。
 * <p>创建 {@link OrganizationTokenPostProcessor}，在组织特性启用时参与 OIDC 令牌签发与刷新流程。</p>
 */
public class OrganizationTokenPostProcessorFactory implements TokenPostProcessorFactory, EnvironmentDependentProviderFactory {

    @Override
    /** 创建 {@link OrganizationTokenPostProcessor} 实例。 */
    public TokenPostProcessor create(KeycloakSession session) {
        return new OrganizationTokenPostProcessor(session);
    }

    @Override
    /** @return 工厂 ID organizations */
    public String getId() {
        return "organizations";
    }

    @Override
    /** @return 组织特性启用时可用 */
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Feature.ORGANIZATION);
    }
}
