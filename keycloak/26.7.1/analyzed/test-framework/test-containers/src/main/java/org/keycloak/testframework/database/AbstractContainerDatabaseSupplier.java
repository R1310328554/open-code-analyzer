package org.keycloak.testframework.database;

import org.keycloak.testframework.annotations.InjectTestDatabase;
import org.keycloak.testframework.config.Config;
import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.server.KeycloakServer;
import org.keycloak.testframework.server.KeycloakServerConfigBuilder;

/**
 * 容器化测试数据库供应器的抽象基类。
 * <p>
 * 在集群模式下，当 Keycloak 与数据库均运行于容器时，自动配置 Docker 网络内的 JDBC URL。
 */
public abstract class AbstractContainerDatabaseSupplier extends AbstractDatabaseSupplier {

    /**
     * {@inheritDoc}
     * <p>
     * 集群容器部署时，为 Keycloak 注入容器网络可访问的数据库连接配置。
     */
    @Override
    public KeycloakServerConfigBuilder intercept(KeycloakServerConfigBuilder serverConfig, InstanceContext<TestDatabase, InjectTestDatabase> instanceContext) {
        serverConfig = super.intercept(serverConfig, instanceContext);

        // If both KeycloakServer and TestDatabase run in container, we need to configure Keycloak with internal url that is accessible within docker network.
        // Right now it's supported by the cluster server mode only.
        if ("cluster".equals(Config.getSelectedSupplier(KeycloakServer.class)) &&
                instanceContext.getValue() instanceof AbstractContainerTestDatabase containerDatabase) {
            return serverConfig.options(containerDatabase.serverConfig(true));
        } else {
            return serverConfig;
        }
    }
}
