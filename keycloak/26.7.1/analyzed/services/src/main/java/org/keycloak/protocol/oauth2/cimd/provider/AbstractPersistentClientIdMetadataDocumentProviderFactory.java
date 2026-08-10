package org.keycloak.protocol.oauth2.cimd.provider;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * {@link AbstractPersistentClientIdMetadataDocumentProvider} 的抽象工厂。
 * <p>生命周期钩子默认为空实现，由具体子类提供 Provider 实例。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public abstract class AbstractPersistentClientIdMetadataDocumentProviderFactory implements ClientIdMetadataDocumentProviderFactory {

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

}
