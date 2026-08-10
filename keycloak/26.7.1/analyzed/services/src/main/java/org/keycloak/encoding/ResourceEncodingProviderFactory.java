package org.keycloak.encoding;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderFactory;

/**
 * {@link ResourceEncodingProvider} 的 {@link ProviderFactory} 工厂接口。
 * <p>除标准工厂生命周期外，还需声明是否对给定 Content-Type 执行编码。</p>
 */
public interface ResourceEncodingProviderFactory extends ProviderFactory<ResourceEncodingProvider> {

    /** @param contentType 响应 Content-Type @return 该工厂是否支持对此类型编码 */
    boolean encodeContentType(String contentType);

    @Override
    default void init(Config.Scope config) {
    }

    @Override
    default void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    default void close() {
    }

}
