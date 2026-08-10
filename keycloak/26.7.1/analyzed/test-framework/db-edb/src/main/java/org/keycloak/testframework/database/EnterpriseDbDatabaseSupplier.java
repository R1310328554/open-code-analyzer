package org.keycloak.testframework.database;

/**
 * EnterpriseDB（PostgreSQL 兼容）容器数据库 {@link AbstractContainerDatabaseSupplier}。
 * <p>
 * 别名 {@link EnterpriseDbTestDatabase#NAME}（{@code edb}），通过 Testcontainers 启动 EDB 镜像。
 */
public class EnterpriseDbDatabaseSupplier extends AbstractContainerDatabaseSupplier {

    /** {@inheritDoc} 返回 {@link EnterpriseDbTestDatabase#NAME}。 */
    @Override
    public String getAlias() {
        return EnterpriseDbTestDatabase.NAME;
    }

    /** {@inheritDoc} 返回 {@link EnterpriseDbTestDatabase} 实例。 */
    @Override
    TestDatabase getTestDatabase() {
        return new EnterpriseDbTestDatabase();
    }

}
