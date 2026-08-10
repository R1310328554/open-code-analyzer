package org.keycloak.services.resources.admin;

import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.token.TokenPostProcessor;
import org.keycloak.protocol.oidc.token.TokenPostProcessorFactory;

/**
 * {@link AdminRoleTokenPostProcessor} 的 SPI 工厂。
 */
public class AdminRoleTokenPostProcessorFactory implements TokenPostProcessorFactory {

    /** {@inheritDoc} 创建管理角色令牌后处理器实例。 */
    @Override
    public TokenPostProcessor create(KeycloakSession session) {
        return new AdminRoleTokenPostProcessor(session);
    }

    /** {@inheritDoc} 工厂标识 {@code admin-role-token-post-processor}。 */
    @Override
    public String getId() {
        return "admin-role-token-post-processor";
    }
}
