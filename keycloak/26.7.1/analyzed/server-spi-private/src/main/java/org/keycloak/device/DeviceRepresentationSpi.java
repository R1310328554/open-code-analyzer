package org.keycloak.device;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 设备表示 SPI，注册 {@link DeviceRepresentationProvider} 提供者类型。
 */
public class DeviceRepresentationSpi implements Spi {

    /** SPI 名称常量：{@code deviceRepresentation}。 */
    public static final String NAME = "deviceRepresentation";
    /** 内部 SPI，不对扩展模块公开。 */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** @return {@link #NAME} */
    @Override
    public String getName() {
        return NAME;
    }

    /** 设备表示提供者接口类型。 */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return DeviceRepresentationProvider.class;
    }

    /** 设备表示工厂类型。 */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return DeviceRepresentationProviderFactory.class;
    }

}
