package org.keycloak.protocol.oidc.ext;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderFactory;

/**
 * {@link OIDCExtProvider} 的工厂接口。
 */
public interface OIDCExtProviderFactory extends ProviderFactory<OIDCExtProvider> {

    @Override
    default void init(Config.Scope config) {

    }

    @Override
    default void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    default void close() {

    }

    @Override
    default int order() {
        return 0;
    }

}
