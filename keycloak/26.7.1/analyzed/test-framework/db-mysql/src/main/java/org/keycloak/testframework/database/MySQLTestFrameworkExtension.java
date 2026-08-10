package org.keycloak.testframework.database;

import java.util.List;

import org.keycloak.testframework.TestFrameworkExtension;
import org.keycloak.testframework.injection.Supplier;

/**
 * MySQL 测试框架扩展，通过 SPI 注册 {@link MySQLDatabaseSupplier}。
 */
public class MySQLTestFrameworkExtension implements TestFrameworkExtension {

    /** {@inheritDoc} 返回 MySQL 容器数据库供应器。 */
    @Override
    public List<Supplier<?, ?>> suppliers() {
        return List.of(new MySQLDatabaseSupplier());
    }
}
