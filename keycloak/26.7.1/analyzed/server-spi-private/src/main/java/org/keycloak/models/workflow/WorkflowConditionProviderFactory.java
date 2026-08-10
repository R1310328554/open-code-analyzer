package org.keycloak.models.workflow;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.ProviderFactory;

/**
 * {@link WorkflowConditionProvider} 的工厂接口。
 * <p>仅在 {@link org.keycloak.common.Profile.Feature#WORKFLOWS} 特性启用时加载。</p>
 */
public interface WorkflowConditionProviderFactory<P extends WorkflowConditionProvider> extends ProviderFactory<P>, EnvironmentDependentProviderFactory {

    /**
     * 使用配置参数创建条件提供者实例。
     * @param session Keycloak 会话
     * @param configParameter 条件配置字符串
     */
    P create(KeycloakSession session, String configParameter);

    /** 无配置参数的 create 未实现，请使用带 configParameter 的重载。 */
    @Override
    default P create(KeycloakSession session) {
        throw new IllegalStateException("Use create(KeycloakSession session, String configParameter) instead.");
    }

    @Override
    default boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.WORKFLOWS);
    }

    @Override
    default void init(Config.Scope config) {
        // no-op default
    }

    @Override
    default void postInit(KeycloakSessionFactory factory) {
        // no-op default
    }

    @Override
    default void close() {
        // no-op default
    }
}
