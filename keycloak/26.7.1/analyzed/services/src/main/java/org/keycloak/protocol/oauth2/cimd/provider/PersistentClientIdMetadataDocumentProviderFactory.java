package org.keycloak.protocol.oauth2.cimd.provider;

import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oauth2.cimd.clientpolicy.executor.ClientIdMetadataDocumentExecutor;

/**
 * {@link PersistentClientIdMetadataDocumentProvider} 的工厂。
 * <p>Provider ID 为 {@code persistent-cimd}。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class PersistentClientIdMetadataDocumentProviderFactory extends AbstractPersistentClientIdMetadataDocumentProviderFactory {

    /** 持久化 CIMD Provider 的注册 ID。 */
    public static final String PROVIDER_ID = "persistent-cimd";

    /** {@inheritDoc} 创建会话级持久化 CIMD Provider。 */
    @Override
    public ClientIdMetadataDocumentProvider<ClientIdMetadataDocumentExecutor.Configuration> create(KeycloakSession session) {
        return new PersistentClientIdMetadataDocumentProvider(session);
    }

    /** @return {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
