package org.keycloak.scim.resource.spi;

import org.keycloak.Config.Scope;
import org.keycloak.common.Profile;
import org.keycloak.common.Profile.Feature;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.ProviderFactory;

/**
 * {@link ScimResourceTypeProvider} 的工厂接口。
 * <p>仅在 {@link Feature#SCIM_API} 特性启用时加载。</p>
 *
 * @param <P> 提供者类型
 */
public interface ScimResourceTypeProviderFactory<P extends ScimResourceTypeProvider<?>> extends ProviderFactory<P>, EnvironmentDependentProviderFactory {

    /**
     * 判断当前环境是否支持此工厂。
     *
     * @param config Keycloak 配置作用域
     * @return 启用 SCIM API 特性时返回 {@code true}
     */
    @Override
    default boolean isSupported(Scope config) {
        return Profile.isFeatureEnabled(Feature.SCIM_API);
    }
}
