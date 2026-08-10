package org.keycloak.testframework.realm;

/**
 * {@link ClientConfig} 的默认空实现，不修改构建器中的任何选项。
 */
public class DefaultClientConfig implements ClientConfig {

    /** {@inheritDoc} 原样返回传入的构建器。 */
    @Override
    public ClientBuilder configure(ClientBuilder client) {
        return client;
    }

}
