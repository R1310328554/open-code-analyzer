package org.keycloak.protocol.oid4vc.issuance;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.protocol.oidc.endpoints.AuthorizationEndpointCheckProvider;
import org.keycloak.protocol.oidc.endpoints.AuthorizationEndpointCheckProviderFactory;

/**
 * {@link OID4VCAuthorizationCheckProvider} 的 SPI 工厂。
 * <p>仅在 {@link Profile.Feature#OID4VC_VCI} 特性启用时注册。</p>
 */
public class OID4VCAuthorizationCheckProviderFactory implements AuthorizationEndpointCheckProviderFactory {

    @Override
    /** @param session Keycloak 会话 @return OID4VCI 授权检查提供方 */
    public AuthorizationEndpointCheckProvider create(KeycloakSession session) {
        return new OID4VCAuthorizationCheckProvider(session);
    }

    @Override
    /** @return 提供方 ID {@code oid4vci-auth-checker} */
    public String getId() {
        return "oid4vci-auth-checker";
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
    /** OID4VCI 特性开启时可用。 */
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.OID4VC_VCI);
    }
}
