package org.keycloak.testframework.database;

/**
 * Microsoft SQL Server 容器数据库 {@link AbstractContainerDatabaseSupplier}。
 * <p>
 * 别名 {@link MSSQLServerTestDatabase#NAME}（{@code mssql}）。
 */
public class MSSQLServerDatabaseSupplier extends AbstractContainerDatabaseSupplier {

    /** {@inheritDoc} 返回 {@link MSSQLServerTestDatabase#NAME}。 */
    @Override
    public String getAlias() {
        return MSSQLServerTestDatabase.NAME;
    }

    /** {@inheritDoc} 返回 {@link MSSQLServerTestDatabase} 实例。 */
    @Override
    TestDatabase getTestDatabase() {
        return new MSSQLServerTestDatabase();
    }

}
