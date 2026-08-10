package org.keycloak.ssf.transmitter;

import org.keycloak.provider.Provider;
import org.keycloak.provider.Spi;

/**
 * SSF 发送方 SPI 定义，向 Keycloak SPI 框架注册 {@link SsfTransmitterProvider}
 * 及其 {@link SsfTransmitterProviderFactory} 工厂。
 */
public class SsfTransmitterSpi implements Spi {

    @Override
    public String getName() {
        return "ssf-transmitter";
    }

    @Override
    public boolean isInternal() {
        return false;
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return SsfTransmitterProvider.class;
    }

    @Override
    public Class<? extends SsfTransmitterProviderFactory> getProviderFactoryClass() {
        return SsfTransmitterProviderFactory.class;
    }
}
