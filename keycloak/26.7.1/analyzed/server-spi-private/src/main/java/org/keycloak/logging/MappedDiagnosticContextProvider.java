package org.keycloak.logging;

import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.OrganizationModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.provider.Provider;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * 映射诊断上下文（MDC）提供者 SPI：根据当前 Keycloak 上下文写入日志 MDC 键值对。
 * <p>所有 MDC 键均带 {@code kc.} 前缀，避免与应用程序日志上下文冲突。</p>
 *
 * @author <a href="mailto:b.eicki@gmx.net">Björn Eickvonder</a>
 */
public interface MappedDiagnosticContextProvider extends Provider {

    /** MDC 键的统一前缀。 */
    String MDC_PREFIX = "kc.";

    /**
     * 根据认证会话更新 MDC（设置 Keycloak 会话或认证会话属性变更时调用）。
     *
     * @param keycloakContext the current Keycloak context, never null
     * @param session the authentication session
     */
    void update(KeycloakContext keycloakContext, AuthenticationSessionModel session);

    /**
     * 根据领域更新 MDC（设置 Keycloak 会话或领域属性变更时调用）。
     *
     * @param keycloakContext the current Keycloak context, never null
     * @param realm the realm
     */
    void update(KeycloakContext keycloakContext, RealmModel realm);

    /**
     * 根据客户端更新 MDC（设置 Keycloak 会话或客户端属性变更时调用）。
     *
     * @param keycloakContext the current Keycloak context, never null
     * @param client the client
     */
    void update(KeycloakContext keycloakContext, ClientModel client);

    /**
     * 根据组织更新 MDC（设置 Keycloak 会话或组织属性变更时调用）。
     *
     * @param keycloakContext the current Keycloak context, never null
     * @param organization the organization
     */
    void update(KeycloakContext keycloakContext, OrganizationModel organization);

    /**
     * 根据用户会话更新 MDC（设置 Keycloak 会话或用户会话属性变更时调用）。
     *
     * @param keycloakContext the current Keycloak context, never null
     * @param userSession the user session
     */
    void update(KeycloakContext keycloakContext, UserSessionModel userSession);

}
