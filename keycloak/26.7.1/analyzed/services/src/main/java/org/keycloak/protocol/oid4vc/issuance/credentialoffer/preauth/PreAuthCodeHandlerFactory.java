package org.keycloak.protocol.oid4vc.issuance.credentialoffer.preauth;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderFactory;

/**
 * {@link PreAuthCodeHandler} 的 {@link ProviderFactory}。
 * <p>注册具体预授权码实现（如 JWT 格式）到 Keycloak SPI 体系。</p>
 */
public interface PreAuthCodeHandlerFactory extends ProviderFactory<PreAuthCodeHandler> {

    /** SPI 初始化钩子；默认无操作。 */
    @Override
    default void init(Config.Scope config) {
    }

    /** 会话工厂就绪后的后置初始化；默认无操作。 */
    @Override
    default void postInit(KeycloakSessionFactory factory) {
    }

    /** 关闭工厂资源；默认无操作。 */
    @Override
    default void close() {
    }
}
