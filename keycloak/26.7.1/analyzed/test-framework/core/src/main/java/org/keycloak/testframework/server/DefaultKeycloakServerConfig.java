package org.keycloak.testframework.server;

/**
 * {@link KeycloakServerConfig} 的默认空实现，不修改启动命令构建器中的任何选项。
 */
public class DefaultKeycloakServerConfig implements KeycloakServerConfig {

    /** {@inheritDoc} 原样返回传入的构建器。 */
    @Override
    public KeycloakServerConfigBuilder configure(KeycloakServerConfigBuilder config) {
        return config;
    }

}
