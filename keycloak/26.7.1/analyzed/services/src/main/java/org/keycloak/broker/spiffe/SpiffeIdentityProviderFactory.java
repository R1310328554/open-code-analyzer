package org.keycloak.broker.spiffe;

import java.util.Map;

import org.keycloak.Config;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.provider.ClientAssertionIdentityProviderFactory;
import org.keycloak.common.Profile;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.EnvironmentDependentProviderFactory;

/**
 * SPIFFE 身份代理工厂：在启用 {@link Profile.Feature#SPIFFE} 时注册 SPIFFE IdP，
 * 并暴露 {@link ClientAssertionStrategy} 供客户端断言使用。
 */
public class SpiffeIdentityProviderFactory extends AbstractIdentityProviderFactory<SpiffeIdentityProvider> implements EnvironmentDependentProviderFactory, ClientAssertionIdentityProviderFactory {

    /** SPIFFE 身份代理 provider id。 */
    public static final String PROVIDER_ID = "spiffe";

    /** @return 控制台显示名称 SPIFFE */
    @Override
    public String getName() {
        return "SPIFFE";
    }

    /** 基于会话与 IdP 模型创建 {@link SpiffeIdentityProvider} 实例。 */
    @Override
    public SpiffeIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new SpiffeIdentityProvider(session, new SpiffeIdentityProviderConfig(model));
    }

    /** SPIFFE 配置不支持字符串解析，调用将抛出 {@link UnsupportedOperationException}。 */
    @Override
    public Map<String, String> parseConfig(KeycloakSession session, String configString) {
        throw new UnsupportedOperationException();
    }

    /** @return 默认 {@link SpiffeIdentityProviderConfig} 配置模型 */
    @Override
    public IdentityProviderModel createConfig() {
        return new SpiffeIdentityProviderConfig();
    }

    /** @return provider id {@value #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** 仅当 SPIFFE 特性开关启用时可用。 */
    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.SPIFFE);
    }

    /** @return SPIFFE 客户端断言策略实现 */
    @Override
    public ClientAssertionStrategy getClientAssertionStrategy() {
        return new SpiffeClientAssertionStrategy();
    }

}
