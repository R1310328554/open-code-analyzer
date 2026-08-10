package org.keycloak.protocol.oauth2.cimd.clientpolicy.executor;

import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.representations.oidc.OIDCClientRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyException;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jboss.logging.Logger;

/**
 * {@link AbstractClientIdMetadataDocumentExecutor} 的具体实现，提供 Keycloak 特有的额外策略。
 *
 * <p>额外客户端元数据验证策略：</p>
 * <ul>
 *     <li>可选仅接受机密客户端（confidential client）</li>
 *     <li>同域 SSRF 防护（继承自抽象类的受信域名与同域限制）</li>
 * </ul>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class ClientIdMetadataDocumentExecutor extends AbstractClientIdMetadataDocumentExecutor<ClientIdMetadataDocumentExecutor.Configuration> {

    private static final Logger logger = Logger.getLogger(ClientIdMetadataDocumentExecutor.class);

    /** @return 本执行器专用日志器 */
    protected Logger getLogger() {
        return logger;
    }

    /**
     * @param session Keycloak 会话
     * @param providerConfig 工厂级 CIMD 全局配置
     */
    public ClientIdMetadataDocumentExecutor(KeycloakSession session, ClientIdMetadataDocumentExecutorFactoryProviderConfig providerConfig) {
        super(session, providerConfig);
    }

    @Override
    public String getProviderId() {
        return ClientIdMetadataDocumentExecutorFactory.PROVIDER_ID;
    }

    @Override
    public Class<ClientIdMetadataDocumentExecutor.Configuration> getExecutorConfigurationClass() {
        return ClientIdMetadataDocumentExecutor.Configuration.class;
    }

    @Override
    public void setupConfiguration(ClientIdMetadataDocumentExecutor.Configuration config) {
        this.configuration = Optional.ofNullable(config).orElse(createDefaultConfiguration());
    }

    private ClientIdMetadataDocumentExecutor.Configuration createDefaultConfiguration() {
        return new ClientIdMetadataDocumentExecutor.Configuration();
    }

    public static class Configuration extends AbstractClientIdMetadataDocumentExecutor.Configuration {
        // Keycloak 特有附加配置
        // 客户端元数据额外验证配置
        @JsonProperty(ClientIdMetadataDocumentExecutorFactory.ONLY_ALLOW_CONFIDENTIAL_CLIENT)
        /** 为 true 时仅接受机密客户端。 */
        protected boolean onlyAllowConfidentialClient = false;

        public Configuration() {
            super();
        }

        public boolean isOnlyAllowConfidentialClient() {
            return onlyAllowConfidentialClient;
        }

        public void setOnlyAllowConfidentialClient(boolean onlyAllowConfidentialClient) {
            this.onlyAllowConfidentialClient = onlyAllowConfidentialClient;
        }
    }

    // 机密客户端策略相关错误消息
    public static final String ERR_METADATA_NO_CONFIDENTIAL_CLIENT = "Invalid Client Metadata: confidential client is only allowed.";
    public static final String ERR_METADATA_NO_CONFIDENTIAL_CLIENT_JWKS = "Invalid Client Metadata: ether jwks or jwks_uri property is required.";

    @Override
    protected void validateClientMetadata(final URI clientIdURI, final URI redirectUriURI, final OIDCClientRepresentation clientOIDC) throws ClientPolicyException {
        super.validateClientMetadata(clientIdURI, redirectUriURI, clientOIDC);

        // 可选策略：仅接受机密客户端
        if (configuration.isOnlyAllowConfidentialClient()) {
            if (clientOIDC.getTokenEndpointAuthMethod() == null || !ALLOWED_ALGORITHMS.contains(clientOIDC.getTokenEndpointAuthMethod())) {
                getLogger().warn("not confidential client");
                throw invalidClientIdMetadata(ERR_METADATA_NO_CONFIDENTIAL_CLIENT);
            }
            if (clientOIDC.getJwksUri() == null && clientOIDC.getJwks() == null) {
                getLogger().warn("confidential client but jwks or jwks_uri properties is not included");
                throw invalidClientIdMetadata(ERR_METADATA_NO_CONFIDENTIAL_CLIENT_JWKS);
            }
            if (clientOIDC.getJwksUri() != null && clientOIDC.getJwks() != null) {
                getLogger().warn("confidential client but both jwks and jwks_uri properties are included");
                throw invalidClientIdMetadata(ERR_METADATA_NO_CONFIDENTIAL_CLIENT_JWKS);
            }
        }

    }

    protected static final Set<String> ALLOWED_ALGORITHMS = new LinkedHashSet<>(Arrays.asList(
            OIDCLoginProtocol.PRIVATE_KEY_JWT,
            OIDCLoginProtocol.TLS_CLIENT_AUTH
    ));
}
