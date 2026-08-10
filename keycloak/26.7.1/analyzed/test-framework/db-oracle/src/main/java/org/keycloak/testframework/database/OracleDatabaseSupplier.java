package org.keycloak.testframework.database;

import org.keycloak.testframework.annotations.InjectTestDatabase;
import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.server.KeycloakServerConfigBuilder;

/**
 * 基于 Testcontainers 启动 Oracle 容器并为 Keycloak 注入 ojdbc 依赖的测试数据库供应器。
 * <p>
 * 通过 {@link AbstractContainerDatabaseSupplier} 管理容器生命周期，
 * 并在服务器启动前追加 Oracle JDBC 驱动依赖。
 */
public class OracleDatabaseSupplier extends AbstractContainerDatabaseSupplier {

    /** {@inheritDoc} 返回 {@link OracleTestDatabase#NAME} 作为注入别名。 */
    @Override
    public String getAlias() {
        return OracleTestDatabase.NAME;
    }

    /** {@inheritDoc} 创建 {@link OracleTestDatabase} 实例。 */
    @Override
    TestDatabase getTestDatabase() {
        return new OracleTestDatabase();
    }

    /**
     * {@inheritDoc}
     * <p>
     * 在父类注入数据库配置后，追加 {@code com.oracle.database.jdbc:ojdbc17} 依赖。
     *
     * @param serverConfig 服务器配置构建器
     * @param instanceContext 当前测试数据库实例上下文
     * @return 已追加 JDBC 驱动依赖的配置构建器
     */
    @Override
    public KeycloakServerConfigBuilder intercept(KeycloakServerConfigBuilder serverConfig, InstanceContext<TestDatabase, InjectTestDatabase> instanceContext) {
        return super.intercept(serverConfig, instanceContext)
                .dependency("com.oracle.database.jdbc", "ojdbc17");
    }
}
