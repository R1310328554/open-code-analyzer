package org.keycloak.logging;

import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.OrganizationModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * 空操作 MDC 提供者：不写入任何 MDC 键值对。
 * <p>在 {@code LOG_MDC} 特性关闭或无法获取会话时使用。</p>
 */
public class NoopMappedDiagnosticContextProvider implements MappedDiagnosticContextProvider {

    /** 认证会话变更时不写入 MDC。 */
    @Override
    public void update(KeycloakContext keycloakContext, AuthenticationSessionModel session) {
        // 空操作
    }

    /** 领域变更时不写入 MDC。 */
    @Override
    public void update(KeycloakContext keycloakContext, RealmModel realm) {
        // 空操作
    }

    /** 客户端变更时不写入 MDC。 */
    @Override
    public void update(KeycloakContext keycloakContext, ClientModel client) {
        // 空操作
    }

    /** 组织变更时不写入 MDC。 */
    @Override
    public void update(KeycloakContext keycloakContext, OrganizationModel organization) {
        // 空操作
    }

    /** 用户会话变更时不写入 MDC。 */
    @Override
    public void update(KeycloakContext keycloakContext, UserSessionModel userSession) {
        // 空操作
    }

    /** 关闭时不释放资源。 */
    @Override
    public void close() {
        // 空操作
    }
}
