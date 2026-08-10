package org.keycloak.services.resource;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 账户资源 SPI：用于替换默认账户端点与资源。
 * <p>实现方可通过此 {@link Spi} 创建 JAX-RS 资源，覆盖默认 {@code /account} 路径下的行为。</p>
 */
public class AccountResourceSpi implements Spi {

    /** @return 始终为 {@code true}，表示内部 SPI */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** @return SPI 名称 {@code account-resource} */
    @Override
    public String getName() {
        return "account-resource";
    }

    /** @return 提供者接口 {@link AccountResourceProvider} */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return AccountResourceProvider.class;
    }

    /** @return 工厂接口 {@link AccountResourceProviderFactory} */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return AccountResourceProviderFactory.class;
    }
}
