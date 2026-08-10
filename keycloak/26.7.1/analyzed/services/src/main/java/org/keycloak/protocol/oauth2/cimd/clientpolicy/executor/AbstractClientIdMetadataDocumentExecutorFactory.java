package org.keycloak.protocol.oauth2.cimd.clientpolicy.executor;

import java.util.List;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.services.clientpolicy.executor.ClientPolicyExecutorProviderFactory;

/**
 * {@link AbstractClientIdMetadataDocumentExecutor} 的抽象 SPI 工厂。
 *
 * <p>提供以下配置项：</p>
 * <ul>
 *     <li>Client ID / 元数据 URL 校验：是否允许 http scheme（仅开发环境）</li>
 *     <li>Client ID 策略：受信域名列表（支持 {@code *.example.org} 通配符）</li>
 *     <li>元数据策略：同域限制、必填属性列表</li>
 *     <li>工厂全局：CIMD 提供方名称、缓存时间上下限、元数据字节上限</li>
 * </ul>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public abstract class AbstractClientIdMetadataDocumentExecutorFactory
        implements ClientPolicyExecutorProviderFactory, EnvironmentDependentProviderFactory {

    // Client ID 格式校验配置键
    public static final String ALLOW_HTTP_SCHEME = "cimd-allow-http-scheme";

    // Client ID 策略验证配置键
    public static final String TRUSTED_DOMAINS = "cimd-allow-permitted-domains";

    // 客户端元数据策略验证配置键
    public static final String REQUIRED_PROPERTIES = "cimd-required-properties";
    public static final String RESTRICT_SAME_DOMAIN = "cimd-restrict-same-domain";

    // 工厂级全局 SPI 配置键
    public static final String CONFIG_CIMD_PROVIDER_NAME = "cimd-provider-name";
    public static final String CONFIG_MIN_CACHE_TIME = "min-cache-time";
    public static final String CONFIG_MAX_CACHE_TIME = "max-cache-time";
    public static final String CONFIG_UPPER_LIMIT_METADATA_BYTES = "upper-limit-metadata-bytes";

    protected ClientIdMetadataDocumentExecutorFactoryProviderConfig providerConfig;

    @Override
    /** 从 SPI 配置初始化工厂级全局参数。 */
    public void init(Config.Scope config) {
        providerConfig = new ClientIdMetadataDocumentExecutorFactoryProviderConfig(config);
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    @Override
    /** @return 执行器帮助说明 */
    public String getHelpText() {
        return "On receiving an authorization request, this executor process the request by following OAuth Client ID Metadata Document (Internet Draft).";
    }

    static protected void addCommonConfigProperties(List<ProviderConfigProperty> configProperties) {
        // Client ID 与元数据 URL 相关校验配置
        ProviderConfigProperty property = new ProviderConfigProperty(
                ALLOW_HTTP_SCHEME,
                "Allow http scheme",
                "If ON, then the executor allows http scheme as a valid Client ID URL and property of Client Metadata whose value is URL: client_uri, logo_uri, tos_uri, policy_uri, jwks_uri. " +
                        "It can be ON only for development environment. It must be OFF in production environment. ",
                ProviderConfigProperty.BOOLEAN_TYPE,
                false);
        configProperties.add(property);

        // Client ID 受信域名配置
        property = new ProviderConfigProperty(
                TRUSTED_DOMAINS,
                "Trusted domains",
                "If some domains are filled, the executor only accepts the following URL-formatted parameters whose host part matches one of the filled domains: " +
                        "Authorization request parameters: client_id, redirect_uri, " +
                        "Client metadata properties: client_id, redirect_uris, jwks_uri, logo_uri, policy_uri, tos_uri, client_uri. " +
                        "The domains are checked by using wildcard pattern matching (e.g. '*.example.org'). " +
                        "If the domains not filled, the executor denies all such the parameters and properties. " +
                        "For example, use pattern like '*.example.org' if you want to accept the parameter / property whose domain is 'example.org' or its subdomains.",
                ProviderConfigProperty.MULTIVALUED_STRING_TYPE,
                null);
        configProperties.add(property);

        // 客户端元数据策略配置
        property = new ProviderConfigProperty(
                RESTRICT_SAME_DOMAIN,
                "Restrict same domain",
                "If ON, then the executor checks Client ID URL and Redirect URI of an authorization request " +
                "and properties of client metadata whose value is URI are under the same one of allow permitted domains.",
                ProviderConfigProperty.BOOLEAN_TYPE,
                false);
        configProperties.add(property);

        property = new ProviderConfigProperty(
                REQUIRED_PROPERTIES,
                "Required properties",
                "If client metadata does not include all the properties, the executor does not accept the client metadata.",
                ProviderConfigProperty.MULTIVALUED_STRING_TYPE,
                null);
        configProperties.add(property);
    }

    @Override
    /** @return 工厂级 SPI 元数据配置（缓存时间、元数据大小上限等） */
    public List<ProviderConfigProperty> getConfigMetadata() {
        return ProviderConfigurationBuilder.create()
                .property()
                .name(CONFIG_CIMD_PROVIDER_NAME)
                .type("string")
                .helpText("Provider to use for the CIMD")
                .defaultValue(ClientIdMetadataDocumentExecutorFactoryProviderConfig.DEFAULT_CONFIG_CIMD_PROVIDER_NAME)
                .add()

                .property()
                .name(CONFIG_MIN_CACHE_TIME)
                .type("int")
                .helpText("Min cache time of client metadata in seconds for the CIMD.")
                .defaultValue(ClientIdMetadataDocumentExecutorFactoryProviderConfig.DEFAULT_CONFIG_MIN_CACHE_TIME)
                .add()

                .property()
                .name(CONFIG_MAX_CACHE_TIME)
                .type("int")
                .helpText("Max cache time of client metadata in seconds for the CIMD.")
                .defaultValue(ClientIdMetadataDocumentExecutorFactoryProviderConfig.DEFAULT_CONFIG_MAX_CACHE_TIME)
                .add()

                .property()
                .name(CONFIG_UPPER_LIMIT_METADATA_BYTES)
                .type("int")
                .helpText("Client metadata upper limit in byte for the CIMD.")
                .defaultValue(ClientIdMetadataDocumentExecutorFactoryProviderConfig.DEFAULT_CONFIG_UPPER_LIMIT_METADATA_BYTES)
                .add()

                .build();
    }

    @Override
    /** 仅 CIMD 特性启用时可用。 */
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.CIMD);
    }
}
