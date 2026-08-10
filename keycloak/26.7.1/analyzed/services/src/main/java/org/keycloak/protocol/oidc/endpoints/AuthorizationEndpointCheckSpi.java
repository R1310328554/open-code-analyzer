package org.keycloak.protocol.oidc.endpoints;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 授权端点附加校验 SPI 定义。
 * <p>注册 {@link AuthorizationEndpointCheckProvider} 及其工厂，供 {@link AuthorizationEndpointChecker} 调用。</p>
 */
public class AuthorizationEndpointCheckSpi implements Spi {

    /** @return 是否为 Keycloak 内部 SPI（true） */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** @return SPI 名称 {@code auth-endpoint-check} */
    @Override
    public String getName() {
        return "auth-endpoint-check";
    }

    /** @return 提供者接口类型 */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return AuthorizationEndpointCheckProvider.class;
    }

    /** @return 提供者工厂接口类型 */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return AuthorizationEndpointCheckProviderFactory.class;
    }
}
