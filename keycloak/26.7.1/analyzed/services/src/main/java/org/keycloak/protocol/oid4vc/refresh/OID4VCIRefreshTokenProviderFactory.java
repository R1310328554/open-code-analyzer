package org.keycloak.protocol.oid4vc.refresh;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.protocol.oid4vc.OID4VCEnvironmentProviderFactory;
import org.keycloak.protocol.oidc.refresh.RefreshTokenProvider;
import org.keycloak.protocol.oidc.refresh.RefreshTokenProviderFactory;

/**
 * OID4VCI 刷新令牌提供者工厂。
 * <p>注册 provider id {@value #PROVIDER_ID}，优先级高于默认 refresh token 提供者。</p>
 */
public class OID4VCIRefreshTokenProviderFactory implements RefreshTokenProviderFactory, OID4VCEnvironmentProviderFactory {

    /** SPI 提供者 ID。 */
    public static final String PROVIDER_ID = "oid4vci";

    /** @param session Keycloak 会话
     * @return OID4VCI 刷新令牌提供者实例 */
    @Override
    public RefreshTokenProvider create(KeycloakSession session) {
        return new OID4VCIRefreshTokenProvider(session);
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

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public int order() {
        // 优先级高于默认 refresh token 提供者
        return 10;
    }
}
