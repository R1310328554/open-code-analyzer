package org.keycloak.protocol.oidc.ext;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * OpenID Connect 扩展 SPI：注册 {@link OIDCExtProvider} 及对应工厂。
 */
public class OIDCExtSPI implements Spi {

    /** @return 内部 SPI，不对外暴露配置 */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** @return SPI 名称 {@code openid-connect-ext} */
    @Override
    public String getName() {
        return "openid-connect-ext";
    }

    /** @return Provider 接口类 */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return OIDCExtProvider.class;
    }

    /** @return ProviderFactory 接口类 */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return OIDCExtProviderFactory.class;
    }

}
