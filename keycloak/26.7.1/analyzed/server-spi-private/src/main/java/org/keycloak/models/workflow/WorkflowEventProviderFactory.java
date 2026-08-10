package org.keycloak.models.workflow;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.ProviderFactory;

/**
 * {@link WorkflowEventProvider} 的工厂接口。
 * <p>仅在 {@link org.keycloak.common.Profile.Feature#WORKFLOWS} 特性启用时加载。</p>
 */
public interface WorkflowEventProviderFactory <P extends WorkflowEventProvider> extends ProviderFactory<P>, EnvironmentDependentProviderFactory {

    /**
     * 使用可选配置参数创建事件提供者。
     * @param session Keycloak 会话
     * @param configParameter 配置参数，可为 {@code null}
     */
    P create(KeycloakSession session, String configParameter);

    @Override
    default P create(KeycloakSession session) {
        return create(session, null);
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
