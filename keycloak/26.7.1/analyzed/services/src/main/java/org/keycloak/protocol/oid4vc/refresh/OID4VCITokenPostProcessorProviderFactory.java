package org.keycloak.protocol.oid4vc.refresh;

import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oid4vc.OID4VCEnvironmentProviderFactory;
import org.keycloak.protocol.oidc.token.TokenPostProcessor;
import org.keycloak.protocol.oidc.token.TokenPostProcessorFactory;

/**
 * OID4VCI 令牌后处理器工厂。
 * <p>创建 {@link OID4VCITokenPostProcessor} 实例。</p>
 */
public class OID4VCITokenPostProcessorProviderFactory implements TokenPostProcessorFactory, OID4VCEnvironmentProviderFactory {

    /** @param session Keycloak 会话
     * @return 令牌后处理器 */
    @Override
    public TokenPostProcessor create(KeycloakSession session) {
        return new OID4VCITokenPostProcessor(session);
    }

    /** @return 提供者 ID {@code oid4vci} */
    @Override
    public String getId() {
        return "oid4vci";
    }
}
