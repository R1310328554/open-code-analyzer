package org.keycloak.services.resource;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.ProviderFactory;

/**
 * {@link AccountResourceProvider} 的 SPI 工厂接口。
 * <p>创建账户 REST 资源提供者实例；仅在启用 {@code ACCOUNT_V3} 特性时受支持。</p>
 */
public interface AccountResourceProviderFactory extends ProviderFactory<AccountResourceProvider>, EnvironmentDependentProviderFactory {

    /** @return 当 {@link Profile.Feature#ACCOUNT_V3} 启用时返回 {@code true} */
    @Override
    default boolean isSupported(Config.Scope config) {
        return Profile.isAnyVersionOfFeatureEnabled(Profile.Feature.ACCOUNT_V3);
    }
}
