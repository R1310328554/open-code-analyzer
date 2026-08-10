package org.keycloak.protocol.oauth2.cimd.clientpolicy.executor;

import java.util.ArrayList;
import java.util.List;

import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.clientpolicy.executor.ClientPolicyExecutorProvider;

/**
 * {@link ClientIdMetadataDocumentExecutor} 的 SPI 工厂。
 *
 * <p>在抽象工厂公共配置基础上，额外提供：</p>
 * <ul>
 *     <li>仅允许机密客户端（Only Allow Confidential Client）</li>
 * </ul>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class ClientIdMetadataDocumentExecutorFactory extends AbstractClientIdMetadataDocumentExecutorFactory {

    /** CIMD 执行器提供方 ID。 */
    public static final String PROVIDER_ID = "client-id-metadata-document";

    /** 配置键：是否仅接受机密客户端。 */
    public static final String ONLY_ALLOW_CONFIDENTIAL_CLIENT = "only-allow-confidential-client";

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    static {
        addCommonConfigProperties(configProperties);

        // Keycloak 特有附加配置项
        // 客户端元数据额外验证
        ProviderConfigProperty property = new ProviderConfigProperty(
                ONLY_ALLOW_CONFIDENTIAL_CLIENT,
                "Only Allow Confidential Client",
                "If ON, then the executor only accept a Client Metadata showing a confidential client.",
                ProviderConfigProperty.BOOLEAN_TYPE,
                false);
        configProperties.add(property);
    }

    @Override
    /** @param session Keycloak 会话 @return CIMD 执行器实例 */
    public ClientPolicyExecutorProvider<ClientIdMetadataDocumentExecutor.Configuration> create(KeycloakSession session) {
        return new ClientIdMetadataDocumentExecutor(session, providerConfig);
    }

    @Override
    /** @return 提供方 ID */
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    /** @return 执行器可配置属性列表（含公共与机密客户端选项） */
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }
}
