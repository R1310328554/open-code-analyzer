package org.keycloak.protocol.oauth2.cimd.provider;

import java.net.URI;
import java.net.URISyntaxException;

import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oauth2.cimd.clientpolicy.executor.ClientIdMetadataDocumentExecutor;
import org.keycloak.representations.idm.ClientRepresentation;

import org.jboss.logging.Logger;

import static org.keycloak.models.ClientScopeModel.CONSENT_SCREEN_TEXT;
import static org.keycloak.models.ClientScopeModel.DISPLAY_ON_CONSENT_SCREEN;

/**
 * {@link AbstractPersistentClientIdMetadataDocumentProvider} 的默认持久化实现。
 * <p>在 {@link ClientRepresentation} 上应用 CIMD/MCP 要求的增强策略：</p>
 * <ul>
 *     <li>强制同意：在同意页展示客户端信息，降低钓鱼风险。</li>
 *     <li>禁止全范围授权：遵循最小权限，仅允许请求的 scope。</li>
 * </ul>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class PersistentClientIdMetadataDocumentProvider extends AbstractPersistentClientIdMetadataDocumentProvider<ClientIdMetadataDocumentExecutor.Configuration> {

    /** 本 Provider 使用的 JBoss 日志记录器。 */
    protected Logger logger = Logger.getLogger(PersistentClientIdMetadataDocumentProvider.class);

    public Logger getLogger() {
        return logger;
    }

    /** @param session Keycloak 会话 */
    public PersistentClientIdMetadataDocumentProvider(KeycloakSession session) {
        super(session);
    }

    @Override
    public ClientIdMetadataDocumentExecutor.Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(ClientIdMetadataDocumentExecutor.Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void augmentClientMetadata(ClientRepresentation clientRep) {
        clientRep.setConsentRequired(true);
        clientRep.setFullScopeAllowed(false);

        // 在同意页展示客户端信息
        clientRep.getAttributes().put(DISPLAY_ON_CONSENT_SCREEN, "true");
        URI uri = null;
        try {
            uri = new URI(clientRep.getClientId());
        } catch (URISyntaxException e) {
            return;
        }

        // 规范建议在授权界面展示 client_id 的主机名；TODO：后续可展示更多元数据字段
        clientRep.getAttributes().put(CONSENT_SCREEN_TEXT, "The client's hostname is " + uri.getHost());
    }
}
