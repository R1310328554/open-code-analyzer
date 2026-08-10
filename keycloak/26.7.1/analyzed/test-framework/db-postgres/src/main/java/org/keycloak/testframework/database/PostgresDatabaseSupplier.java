package org.keycloak.testframework.database;

/**
 * 基于 Testcontainers 启动 PostgreSQL 容器的测试数据库供应器。
 * <p>
 * 通过 {@link AbstractContainerDatabaseSupplier} 管理容器生命周期并注入数据库配置。
 */
public class PostgresDatabaseSupplier extends AbstractContainerDatabaseSupplier {

    /** {@inheritDoc} 返回 {@link PostgresTestDatabase#NAME} 作为注入别名。 */
    @Override
    public String getAlias() {
        return PostgresTestDatabase.NAME;
    }

    /** {@inheritDoc} 创建 {@link PostgresTestDatabase} 实例。 */
    @Override
    TestDatabase getTestDatabase() {
        return new PostgresTestDatabase();
    }

}
