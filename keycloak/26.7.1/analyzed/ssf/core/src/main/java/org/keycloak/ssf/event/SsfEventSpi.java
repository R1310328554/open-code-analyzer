package org.keycloak.ssf.event;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 允许扩展向共享 {@link SsfEventRegistry} 贡献额外 SSF 事件类型的 SPI。
 * <p>可注册多个工厂，启动时聚合各工厂的 {@link SsfEventProviderFactory#getContributedEventFactories()}。</p>
 */
public class SsfEventSpi implements Spi {

    @Override
    public String getName() {
        return "ssf-events";
    }

    @Override
    public boolean isInternal() {
        return false;
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return SsfEventProvider.class;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return SsfEventProviderFactory.class;
    }
}
