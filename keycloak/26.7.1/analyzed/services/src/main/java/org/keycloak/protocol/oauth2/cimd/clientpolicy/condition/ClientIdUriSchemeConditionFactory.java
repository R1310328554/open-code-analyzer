package org.keycloak.protocol.oauth2.cimd.clientpolicy.condition;

import java.util.ArrayList;
import java.util.List;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.clientpolicy.condition.AbstractClientPolicyConditionProviderFactory;
import org.keycloak.services.clientpolicy.condition.ClientPolicyConditionProvider;

/**
 * {@link ClientIdUriSchemeCondition} 的 SPI 工厂：提供 URI scheme 与受信域名两项配置。
 * <p>仅在 {@link Profile.Feature#CIMD} 特性启用时可用。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class ClientIdUriSchemeConditionFactory extends AbstractClientPolicyConditionProviderFactory
        implements EnvironmentDependentProviderFactory {

    /** 客户端策略条件提供方 ID。 */
    public static final String PROVIDER_ID = "client-id-uri";

    /** 配置键：允许的 URI scheme 列表。 */
    public static final String CLIENT_ID_URI_SCHEME = "client-id-uri-scheme";
    /** 配置键：受信域名列表。 */
    public static final String TRUSTED_DOMAINS = "client-id-uri-allow-permitted-domains";

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    static {
        addCommonConfigProperties(configProperties);

        ProviderConfigProperty property;
        property = new ProviderConfigProperty(
                CLIENT_ID_URI_SCHEME,
                "URI scheme",
                "Scheme part of the URI",
                ProviderConfigProperty.MULTIVALUED_STRING_TYPE, null);
        configProperties.add(property);

        property = new ProviderConfigProperty(
                TRUSTED_DOMAINS,
                "Trusted domains",
                "If some domains are filled, The condition evaluates to true " +
                        "if the host part of client_id parameter in an authorization request matches one of the filled domains. " +
                        "Otherwise, the condition evaluates to false " +
                        "The domains are checked by using wildcard pattern matching (e.g. '*.example.org'). " +
                        "If the domains not filled, the condition evaluate to false regardless of the client_id parameter value. " +
                        "For example, use pattern like '*.example.org' if you want to accept the parameter / property whose domain is 'example.org' or its subdomains.",
                ProviderConfigProperty.MULTIVALUED_STRING_TYPE,
                null);
        configProperties.add(property);
    }

    @Override
    /** @param session Keycloak 会话 @return 条件提供方实例 */
    public ClientPolicyConditionProvider create(KeycloakSession session) {
        return new ClientIdUriSchemeCondition(session);
    }

    @Override
    /** @return 提供方 ID */
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    /** @return 条件帮助说明（scheme 与 host 均需匹配） */
    public String getHelpText() {
        return "The condition checks that the scheme part of client_id parameter matches one of the filled ones " +
               "and the host part of the client_id matches one of the filled domains.";
    }

    @Override
    /** @return 静态注册的配置属性列表 */
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    /** 仅 CIMD 特性启用时注册此条件。 */
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.CIMD);
    }
}
