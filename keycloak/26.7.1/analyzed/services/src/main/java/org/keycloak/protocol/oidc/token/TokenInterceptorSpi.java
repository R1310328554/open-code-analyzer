package org.keycloak.protocol.oidc.token;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 令牌拦截器 SPI 定义。
 * <p>注册 {@code token-interceptor} 内部 SPI，提供 {@link TokenPostProcessor} 扩展点。</p>
 */
public class TokenInterceptorSpi implements Spi {

    /** @return 是否为内部 SPI（true） */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** @return SPI 名称 {@code token-interceptor} */
    @Override
    public String getName() {
        return "token-interceptor";
    }

    /** @return 提供者接口 {@link TokenPostProcessor} */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return TokenPostProcessor.class;
    }

    /** @return 工厂接口 {@link TokenPostProcessorFactory} */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return TokenPostProcessorFactory.class;
    }
}
