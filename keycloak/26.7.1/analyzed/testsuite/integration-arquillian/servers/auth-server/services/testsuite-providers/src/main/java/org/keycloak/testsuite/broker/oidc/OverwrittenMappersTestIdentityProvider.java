package org.keycloak.testsuite.broker.oidc;

import java.util.Arrays;
import java.util.List;

import org.keycloak.broker.oidc.KeycloakOIDCIdentityProvider;
import org.keycloak.broker.oidc.KeycloakOIDCIdentityProviderFactory;
import org.keycloak.broker.oidc.OIDCIdentityProviderConfig;
import org.keycloak.broker.provider.IdentityProviderMapper;
import org.keycloak.models.KeycloakSession;

/**
 * 覆盖映射器兼容性判定的 Keycloak OIDC 身份提供者，使父提供者支持的映射器同样可用。
 *
 * @author Daniel Fesenmeyer <daniel.fesenmeyer@bosch.com>
 */
public class OverwrittenMappersTestIdentityProvider extends KeycloakOIDCIdentityProvider {

    /**
     * @param session Keycloak 会话
     * @param config OIDC 身份提供者配置
     */
    public OverwrittenMappersTestIdentityProvider(KeycloakSession session, OIDCIdentityProviderConfig config) {
        super(session, config);
    }

    /**
     * {@inheritDoc}
     * 当映射器兼容任意提供者或 Keycloak-OIDC 提供者时返回 true。
     */
    @Override
    public boolean isMapperSupported(IdentityProviderMapper mapper) {
        List<String> compatibleIdps = Arrays.asList(mapper.getCompatibleProviders());

        // 提供与父提供者（Keycloak-OIDC）相同的映射器兼容列表
        return compatibleIdps.contains(IdentityProviderMapper.ANY_PROVIDER)
                || compatibleIdps.contains(KeycloakOIDCIdentityProviderFactory.PROVIDER_ID);
    }

}
