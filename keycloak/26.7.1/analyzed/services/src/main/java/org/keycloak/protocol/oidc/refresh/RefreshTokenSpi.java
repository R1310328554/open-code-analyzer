package org.keycloak.protocol.oidc.refresh;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * refresh-token SPI 定义：注册 {@link RefreshTokenProvider} 与对应工厂。
 * <p>内部 SPI，名称为 {@value #SPI_NAME}。</p>
 */
public class RefreshTokenSpi implements Spi {

    /** SPI 名称 */
    public static final String SPI_NAME = "refresh-token";

    /** {@inheritDoc} 内部 SPI，不对扩展公开 */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** {@inheritDoc} 返回 {@link #SPI_NAME} */
    @Override
    public String getName() {
        return SPI_NAME;
    }

    /** {@inheritDoc} 提供者接口为 {@link RefreshTokenProvider} */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return RefreshTokenProvider.class;
    }

    /** {@inheritDoc} 工厂接口为 {@link RefreshTokenProviderFactory} */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return RefreshTokenProviderFactory.class;
    }
}
