package org.keycloak.services.logging;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.logging.MappedDiagnosticContextProvider;
import org.keycloak.logging.MappedDiagnosticContextProviderFactory;
import org.keycloak.logging.MappedDiagnosticContextUtil;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.OrganizationModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.sessions.AuthenticationSessionModel;

import org.jboss.logging.MDC;

/**
 * 默认 MDC（Mapped Diagnostic Context）Provider 工厂。
 * <p>可通过 {@code --spi-mapped-diagnostic-context-default-mdc-keys} 配置逗号分隔的 MDC 键；默认支持 realm、clientId、userId、ipAddress、org 等。扩展键需自定义 Provider。</p>
 * @author <a href="mailto:b.eicki@gmx.net">Björn Eickvonder</a>
 */
public class DefaultMappedDiagnosticContextProviderFactory implements MappedDiagnosticContextProviderFactory, MappedDiagnosticContextProvider, EnvironmentDependentProviderFactory {

    /** MDC 键：领域名称 */
    public static final String MDC_KEY_REALM_NAME = MDC_PREFIX + "realmName";
    /** MDC 键：客户端 ID */
    public static final String MDC_KEY_CLIENT_ID = MDC_PREFIX + "clientId";
    /** MDC 键：用户 ID */
    public static final String MDC_KEY_USER_ID = MDC_PREFIX + "userId";
    /** MDC 键：IP 地址 */
    public static final String MDC_KEY_IP_ADDRESS = MDC_PREFIX + "ipAddress";
    /** MDC 键：组织别名 */
    public static final String MDC_KEY_ORGANIZATION = MDC_PREFIX + "org";
    /** MDC 键：用户会话 ID */
    public static final String MDC_KEY_SESSION_ID = MDC_PREFIX + "sessionId";
    /** MDC 键：根认证会话 ID */
    public static final String MDC_KEY_AUTHENTICATION_SESSION_ID = MDC_PREFIX + "authenticationSessionId";
    /** MDC 键：认证会话浏览器标签页 ID */
    public static final String MDC_KEY_AUTHENTICATION_TAB_ID = MDC_PREFIX + "authenticationTabId";

    /** SPI 配置项：MDC 键列表 */
    public static final String MDC_KEYS = "mdcKeys";
    /** 已启用的 MDC 键集合 */
    private Set<String> mdcKeys;

    @Override
    public MappedDiagnosticContextProvider create(KeycloakSession session) {
        // 不使用 session，在此类中直接实现 Provider 并以单例方式处理即可
        return this;
    }

    @Override
    public void init(Config.Scope config) {
        this.mdcKeys = Arrays.stream(Objects.requireNonNullElse(config.getArray(MDC_KEYS), new String[] {})).map(s -> MDC_PREFIX + s).collect(Collectors.toSet());
        MappedDiagnosticContextUtil.setKeysToClear(mdcKeys);
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // no-op
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    public String getId() {
        return "default";
    }

    @Override
    public List<ProviderConfigProperty> getConfigMetadata() {
        ProviderConfigurationBuilder builder = ProviderConfigurationBuilder.create();

        builder.property()
                .name(MDC_KEYS)
                .type("string")
                .helpText("Comma-separated list of MDC keys to add to the Mapped Diagnostic Context.")
                .options(Stream.of(MDC_KEY_REALM_NAME, MDC_KEY_CLIENT_ID, MDC_KEY_USER_ID, MDC_KEY_IP_ADDRESS, MDC_KEY_ORGANIZATION, MDC_KEY_SESSION_ID, MDC_KEY_AUTHENTICATION_SESSION_ID, MDC_KEY_AUTHENTICATION_TAB_ID)
                        .map(s -> s.substring(MDC_PREFIX.length())).collect(Collectors.toList()))
                .add();

        return builder.build();
    }

    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.LOG_MDC);
    }

    @Override
    public void update(KeycloakContext keycloakContext, AuthenticationSessionModel session) {
        if (mdcKeys.contains(MDC_KEY_AUTHENTICATION_SESSION_ID)) {
            putMdc(MDC_KEY_AUTHENTICATION_SESSION_ID, session != null ? (session.getParentSession() != null ? session.getParentSession().getId() : null) : null);
        }
        if (mdcKeys.contains(MDC_KEY_AUTHENTICATION_TAB_ID)) {
            putMdc(MDC_KEY_AUTHENTICATION_TAB_ID, session != null ? session.getTabId() : null);
        }
    }

    @Override
    public void update(KeycloakContext keycloakContext, RealmModel realm) {
        if (mdcKeys.contains(MDC_KEY_REALM_NAME)) {
            putMdc(MDC_KEY_REALM_NAME, realm != null ? realm.getName() : null);
        }
    }

    @Override
    public void update(KeycloakContext keycloakContext, ClientModel client) {
        if (mdcKeys.contains(MDC_KEY_CLIENT_ID)) {
            putMdc(MDC_KEY_CLIENT_ID, client != null ? client.getClientId() : null);
        }
    }

    @Override
    public void update(KeycloakContext keycloakContext, OrganizationModel organization) {
        if (mdcKeys.contains(MDC_KEY_ORGANIZATION)) {
            putMdc(MDC_KEY_ORGANIZATION, organization != null ? organization.getAlias() : null);
        }
    }

    @Override
    public void update(KeycloakContext keycloakContext, UserSessionModel userSession) {
        if (mdcKeys.contains(MDC_KEY_USER_ID)) {
            putMdc(MDC_KEY_USER_ID, userSession != null && userSession.getUser() != null ? userSession.getUser().getId() : null);
        }
        if (mdcKeys.contains(MDC_KEY_SESSION_ID)) {
            putMdc(MDC_KEY_SESSION_ID, userSession != null ? userSession.getId() : null);
        }
        if (mdcKeys.contains(MDC_KEY_IP_ADDRESS)) {
            putMdc(MDC_KEY_IP_ADDRESS, userSession != null ? userSession.getIpAddress() : null);
        }
    }

    /** 写入或清除 MDC 键值。
     * @param key MDC 键
     * @param value 值；为 null 时移除该键
     */
    protected void putMdc(String key, String value) {
        if (value != null) {
            MDC.put(key, value);
        } else {
            MDC.remove(key);
        }
    }
}
