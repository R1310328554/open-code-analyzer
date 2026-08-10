package org.keycloak.testframework.database;

import org.keycloak.testframework.annotations.InjectTestDatabase;
import org.keycloak.testframework.config.Config;
import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.server.KeycloakServerConfigBuilder;

/**
 * 连接外部/远程数据库的 {@link AbstractDatabaseSupplier} 实现。
 * <p>
 * 别名 {@code remote}，通过框架配置指定 JDBC 连接与可选驱动依赖。
 */
public class RemoteDatabaseSupplier extends AbstractDatabaseSupplier {

    /** 远程数据库供应器别名常量。 */
    public static final String NAME = "remote";

    /** {@inheritDoc} 返回 {@link #NAME}。 */
    @Override
    public String getAlias() {
        return NAME;
    }

    /** {@inheritDoc} 返回 {@link RemoteTestDatabase} 实例。 */
    @Override
    TestDatabase getTestDatabase() {
        return new RemoteTestDatabase();
    }

    /** 从配置读取 JDBC 驱动 Maven 坐标（{@code groupId:artifactId}）。 */
    private String getDriverDependencyArtifact() {
        return Config.getValueTypeConfig(TestDatabase.class, "driver.artifact", null, String.class);
    }

    /** {@inheritDoc} 合并数据库配置并按需添加驱动依赖。 */
    @Override
    public KeycloakServerConfigBuilder intercept(KeycloakServerConfigBuilder serverConfig, InstanceContext<TestDatabase, InjectTestDatabase> instanceContext) {
        serverConfig = super.intercept(serverConfig, instanceContext);

        String dependencyArtifact = getDriverDependencyArtifact();
        if (dependencyArtifact != null) {
            String[] artifact = dependencyArtifact.split(":");
            if (artifact.length != 2) {
                throw new IllegalArgumentException("Invalid dependency artifact " + dependencyArtifact);
            }
            serverConfig.dependency(artifact[0], artifact[1]);
        }
        return serverConfig;
    }

}
