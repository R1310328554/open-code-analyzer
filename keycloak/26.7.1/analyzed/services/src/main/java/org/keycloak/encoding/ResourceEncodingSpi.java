package org.keycloak.encoding;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 资源编码 SPI，注册 {@link ResourceEncodingProvider} 提供者类型。
 * <p>内部 SPI，用于主题/静态资源的 Gzip 等传输编码扩展。</p>
 */
public class ResourceEncodingSpi implements Spi {

    @Override
    /** 内部 SPI，不对扩展模块公开。 */
    public boolean isInternal() {
        return true;
    }

    @Override
    /** SPI 名称：{@code resource-encoding}。 */
    public String getName() {
        return "resource-encoding";
    }

    @Override
    /** 资源编码提供者接口类型。 */
    public Class<? extends Provider> getProviderClass() {
        return ResourceEncodingProvider.class;
    }

    @Override
    /** 资源编码工厂类型。 */
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return ResourceEncodingProviderFactory.class;
    }

}
