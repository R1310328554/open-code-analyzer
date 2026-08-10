package org.keycloak.testframework.database;

/**
 * MariaDB 容器数据库 {@link AbstractContainerDatabaseSupplier}。
 * <p>
 * 别名 {@link MariaDBTestDatabase#NAME}（{@code mariadb}）。
 */
public class MariaDBDatabaseSupplier extends AbstractContainerDatabaseSupplier {

    /** {@inheritDoc} 返回 {@link MariaDBTestDatabase#NAME}。 */
    @Override
    public String getAlias() {
        return MariaDBTestDatabase.NAME;
    }

    /** {@inheritDoc} 返回 {@link MariaDBTestDatabase} 实例。 */
    @Override
    TestDatabase getTestDatabase() {
        return new MariaDBTestDatabase();
    }

}
