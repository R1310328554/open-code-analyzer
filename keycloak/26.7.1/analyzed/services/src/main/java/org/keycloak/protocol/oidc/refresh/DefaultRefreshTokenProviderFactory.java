package org.keycloak.protocol.oidc.refresh;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * 默认刷新令牌提供者工厂：注册 provider id {@value #PROVIDER_ID}。
 */
public class DefaultRefreshTokenProviderFactory implements RefreshTokenProviderFactory {

    /** SPI 提供者标识符 */
    public static final String PROVIDER_ID = "default";

    /** {@inheritDoc} 创建 {@link DefaultRefreshTokenProvider} 实例 */
    @Override
    public RefreshTokenProvider create(KeycloakSession session) {
        return new DefaultRefreshTokenProvider(session);
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    /** {@inheritDoc} 返回 {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
