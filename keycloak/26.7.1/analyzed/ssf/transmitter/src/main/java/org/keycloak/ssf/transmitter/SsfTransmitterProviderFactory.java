package org.keycloak.ssf.transmitter;

import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.ProviderFactory;

/**
 * SSF 发送方 {@link SsfTransmitterProvider} 的 SPI 工厂接口。
 */
public interface SsfTransmitterProviderFactory extends ProviderFactory<SsfTransmitterProvider>, EnvironmentDependentProviderFactory {

    @Override
    default void close() {
        // NOOP
    }

}
