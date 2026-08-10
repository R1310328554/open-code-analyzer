package org.keycloak.protocol.oid4vc.issuance.credentialoffer.preauth;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 预授权码生成与校验的 SPI 定义。
 * <p>将 {@link PreAuthCodeHandler} 接入 Keycloak 提供方扩展机制。</p>
 */
public class PreAuthCodeHandlerSpi implements Spi {

    /** SPI 注册名称。 */
    private static final String NAME = "preAuthCodeHandler";

    /** 内部 SPI，不对外暴露为可插拔扩展。 */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** @return SPI 名称 {@value #NAME} */
    @Override
    public String getName() {
        return NAME;
    }

    /** @return 提供方接口 {@link PreAuthCodeHandler} */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return PreAuthCodeHandler.class;
    }

    /** @return 工厂接口 {@link PreAuthCodeHandlerFactory} */
    @Override
    public Class<? extends ProviderFactory<PreAuthCodeHandler>> getProviderFactoryClass() {
        return PreAuthCodeHandlerFactory.class;
    }
}
