package org.keycloak.authentication.authenticators.client;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.keycloak.Config;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.ClientAuthenticationFlowContext;
import org.keycloak.authentication.ConfigurableAuthenticatorFactory;
import org.keycloak.broker.provider.ClientAssertionIdentityProvider;
import org.keycloak.broker.provider.ClientAssertionIdentityProviderFactory;
import org.keycloak.broker.provider.IdentityProvider;
import org.keycloak.common.Profile;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.services.resources.IdentityBrokerService;


/**
 * 联合 JWT 客户端认证器：校验由外部身份提供者签发并签名的客户端断言 JWT。
 * <p>当 {@link Profile.Feature#CLIENT_AUTH_FEDERATED} 特性启用时可用；忽略 issuer 与 subject 相同的自签名断言。</p>
 */
public class FederatedJWTClientAuthenticator extends AbstractClientAuthenticator implements EnvironmentDependentProviderFactory {

    /** 提供者标识符。 */
    public static final String PROVIDER_ID = "federated-jwt";

    /** 客户端属性键：JWT 凭证签发者（身份提供者别名）。 */
    public static final String JWT_CREDENTIAL_ISSUER_KEY = "jwt.credential.issuer";
    /** 客户端属性键：联合主体（外部 clientId）。 */
    public static final String JWT_CREDENTIAL_SUBJECT_KEY = "jwt.credential.sub";

    private static final List<ProviderConfigProperty> CLIENT_CONFIG =
            ProviderConfigurationBuilder.create()
                    .property()
                    .name(JWT_CREDENTIAL_ISSUER_KEY)
                    .label("Identity provider")
                    .helpText("Issuer of the client assertion. Use the alias of an identity provider set up in this realm.")
                    .type(ProviderConfigProperty.STRING_TYPE)
                    .required(true)
                    .add()
                    .property().name(JWT_CREDENTIAL_SUBJECT_KEY)
                    .label("Federated subject")
                    .helpText("External clientId (subject) as provided by the identity provider.")
                    .type(ProviderConfigProperty.STRING_TYPE)
                    .required(true)
                    .add()
                    .build();

    /** 已注册的客户端断言查找策略列表。 */
    private final List<ClientAssertionIdentityProviderFactory.ClientAssertionStrategy> strategies = new LinkedList<>();

    /** @return 提供者 ID */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** 收集所有 {@link ClientAssertionIdentityProviderFactory} 的断言策略并注册默认策略。 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
        factory.getProviderFactoriesStream(IdentityProvider.class)
                .filter(ClientAssertionIdentityProviderFactory.class::isInstance)
                .map(ClientAssertionIdentityProviderFactory.class::cast)
                .map(ClientAssertionIdentityProviderFactory::getClientAssertionStrategy)
                .filter(Objects::nonNull)
                .forEach(strategies::add);

        strategies.add(new DefaultClientAssertionStrategy());
    }

    /** 校验联合 JWT 客户端断言；成功则标记 context.success()。 */
    @Override
    public void authenticateClient(ClientAuthenticationFlowContext context) {
        try {
            // 对直接返回的分支先标记为已尝试
            context.attempted();

            ClientAssertionState clientAssertionState = context.getState(ClientAssertionState.class, ClientAssertionState.supplier());
            if (clientAssertionState == null || clientAssertionState.getClientAssertionType() == null) {
                return;
            }

            JsonWebToken jwt = clientAssertionState.getToken();

            // 忽略 issuer 与 subject 相同的自签名客户端断言
            if (jwt != null && Objects.equals(jwt.getIssuer(), jwt.getSubject())) {
                return;
            }

            ClientAssertionIdentityProviderFactory.ClientAssertionStrategy strategy = findStrategy(clientAssertionState.getClientAssertionType());
            if (strategy == null) {
                return;
            }

            ClientAssertionIdentityProviderFactory.LookupResult lookup = strategy.lookup(context);
            if (lookup == null || lookup.identityProviderModel() == null || !lookup.identityProviderModel().isEnabled() || lookup.clientModel() == null) {
                return;
            }

            ClientAssertionIdentityProvider<?> identityProvider = getClientAssertionIdentityProvider(context.getSession(), lookup.identityProviderModel());
            ClientModel client = lookup.clientModel();
            clientAssertionState.setClient(client);

            if (!PROVIDER_ID.equals(client.getClientAuthenticatorType())) return;

            if (identityProvider.verifyClientAssertion(context)) {
                context.success();
            } else {
                context.failure(AuthenticationFlowError.INVALID_CLIENT_CREDENTIALS);
            }
        } catch (Exception e) {
            logger.warn("Authentication failed", e);
            context.failure(AuthenticationFlowError.INVALID_CLIENT_CREDENTIALS);
        }
    }

    /** 按断言类型查找首个支持的策略。 */
    private ClientAssertionIdentityProviderFactory.ClientAssertionStrategy findStrategy(String assertionType) {
        return strategies.stream().filter(c -> c.isSupportedAssertionType(assertionType)).findFirst().orElse(null);
    }

    /** 获取指定身份提供者模型对应的 {@link ClientAssertionIdentityProvider}。 */
    private ClientAssertionIdentityProvider<?> getClientAssertionIdentityProvider(KeycloakSession session, IdentityProviderModel identityProviderModel) {
        if (identityProviderModel == null) {
            return null;
        }
        return IdentityBrokerService.getIdentityProvider(session, identityProviderModel, ClientAssertionIdentityProvider.class);
    }

    /** @return 管理控制台显示名称 */
    @Override
    public String getDisplayType() {
        return "Signed JWT - Federated";
    }

    /** @return 认证器帮助说明文本 */
    @Override
    public String getHelpText() {
        return "Validates client based on signed JWT issued and signed by an external identity provider";
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return ConfigurableAuthenticatorFactory.REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Collections.emptyList();
    }

    /** @return 每客户端配置属性（身份提供者与联合主体） */
    @Override
    public List<ProviderConfigProperty> getConfigPropertiesPerClient() {
        return CLIENT_CONFIG;
    }

    @Override
    public Map<String, Object> getAdapterConfiguration(KeycloakSession session, ClientModel client) {
        return Collections.emptyMap();
    }

    @Override
    public Set<String> getProtocolAuthenticatorMethods(String loginProtocol) {
        return Collections.emptySet();
    }

    /** @return 是否启用 CLIENT_AUTH_FEDERATED 特性 */
    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.CLIENT_AUTH_FEDERATED);
    }

}
