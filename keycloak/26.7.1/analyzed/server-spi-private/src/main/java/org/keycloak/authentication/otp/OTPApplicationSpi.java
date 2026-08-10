package org.keycloak.authentication.otp;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * OTP 应用 SPI 描述符，注册 {@link OTPApplicationProvider} 与 {@link OTPApplicationProviderFactory}。
 */
public class OTPApplicationSpi implements Spi {

    /** 内部 SPI。 */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** SPI 名称：otp-application。 */
    @Override
    public String getName() {
        return "otp-application";
    }

    /** Provider 接口类型。 */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return OTPApplicationProvider.class;
    }

    /** ProviderFactory 接口类型。 */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return OTPApplicationProviderFactory.class;
    }

}
