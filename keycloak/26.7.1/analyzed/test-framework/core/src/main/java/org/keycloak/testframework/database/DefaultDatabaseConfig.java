package org.keycloak.testframework.database;

/**
 * {@link DatabaseConfig} 的默认空实现，不修改构建器中的任何选项。
 */
public class DefaultDatabaseConfig implements DatabaseConfig {
    /** {@inheritDoc} 原样返回传入的构建器。 */
    @Override
    public DatabaseConfigBuilder configure(DatabaseConfigBuilder database) {
        return database;
    }
}
