package org.keycloak.protocol.oauth2.cimd.provider;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * CIMD Provider SPI：注册 {@link ClientIdMetadataDocumentProvider} 及其工厂。
 * <p>SPI 名称为 {@code cimd}，标记为内部 SPI。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class ClientIdMetadataDocumentProviderSpi implements Spi {

    /** {@inheritDoc} 内部 SPI，不对外暴露扩展点文档。 */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** @return SPI 名称 {@code cimd} */
    @Override
    public String getName() {
        return "cimd";
    }

    /** @return Provider 接口类型 */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return ClientIdMetadataDocumentProvider.class;
    }

    /** @return Provider 工厂接口类型 */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return ClientIdMetadataDocumentProviderFactory.class;
    }
}
