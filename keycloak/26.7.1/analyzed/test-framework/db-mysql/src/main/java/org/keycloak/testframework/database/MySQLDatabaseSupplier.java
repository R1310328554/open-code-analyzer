package org.keycloak.testframework.database;

/**
 * MySQL 容器数据库 {@link AbstractContainerDatabaseSupplier}。
 * <p>
 * 别名 {@link MySQLTestDatabase#NAME}（{@code mysql}）。
 */
public class MySQLDatabaseSupplier extends AbstractContainerDatabaseSupplier {

    /** {@inheritDoc} 返回 {@link MySQLTestDatabase#NAME}。 */
    @Override
    public String getAlias() {
        return MySQLTestDatabase.NAME;
    }

    /** {@inheritDoc} 返回 {@link MySQLTestDatabase} 实例。 */
    @Override
    TestDatabase getTestDatabase() {
        return new MySQLTestDatabase();
    }

}
