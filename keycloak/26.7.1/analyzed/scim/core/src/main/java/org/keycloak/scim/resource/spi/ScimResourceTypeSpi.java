package org.keycloak.scim.resource.spi;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * SCIM 资源类型 SPI 定义。
 * <p>将 {@link ScimResourceTypeProvider} 注册到 Keycloak 提供者框架。</p>
 */
public class ScimResourceTypeSpi implements Spi {

    /** 标记为内部 SPI，不对外暴露。 */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** 返回 SPI 名称。 */
    @Override
    public String getName() {
        return "scimResourceType";
    }

    /** 返回提供者接口类型。 */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return ScimResourceTypeProvider.class;
    }

    /** 返回提供者工厂接口类型。 */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return ScimResourceTypeProviderFactory.class;
    }
}
