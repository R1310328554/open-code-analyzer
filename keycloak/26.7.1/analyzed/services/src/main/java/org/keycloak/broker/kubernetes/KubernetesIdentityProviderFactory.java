package org.keycloak.broker.kubernetes;

import java.util.Map;

import org.keycloak.Config;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.common.Profile;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.EnvironmentDependentProviderFactory;

/**
 * Kubernetes 客户端断言身份提供者 SPI 工厂，Provider ID 为 {@code kubernetes}。
 */
public class KubernetesIdentityProviderFactory extends AbstractIdentityProviderFactory<KubernetesIdentityProvider> implements EnvironmentDependentProviderFactory {

    /** Provider ID 常量。 */
    public static final String PROVIDER_ID = "kubernetes";

    @Override
    /** @return 管理控制台显示名称 */
    public String getName() {
        return "Kubernetes";
    }

    @Override
    /** 创建 Kubernetes 身份提供者实例。 */
    public KubernetesIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new KubernetesIdentityProvider(session, new KubernetesIdentityProviderConfig(model));
    }

    @Override
    /** 不支持从字符串解析配置。 */
    public Map<String, String> parseConfig(KeycloakSession session, String configString) {
        throw new UnsupportedOperationException();
    }

    @Override
    /** @return 新的空配置模型 */
    public IdentityProviderModel createConfig() {
        return new KubernetesIdentityProviderConfig();
    }

    @Override
    /** @return {@link #PROVIDER_ID} */
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    /** 需启用 {@link Profile.Feature#KUBERNETES_SERVICE_ACCOUNTS} 特性。 */
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.KUBERNETES_SERVICE_ACCOUNTS);
    }

}
