package org.keycloak.testframework.database;

import java.util.List;

import org.keycloak.testframework.TestFrameworkExtension;
import org.keycloak.testframework.injection.Supplier;

/**
 * TiDB 测试框架扩展，向 SPI 注册 {@link TiDBDatabaseSupplier}。
 */
public class TiDBTestFrameworkExtension implements TestFrameworkExtension {

    /** {@inheritDoc} 提供 TiDB 测试数据库供应器。 */
    @Override
    public List<Supplier<?, ?>> suppliers() {
        return List.of(new TiDBDatabaseSupplier());
    }
}
