package org.keycloak.testframework.database;

import java.util.List;

import org.keycloak.testframework.TestFrameworkExtension;
import org.keycloak.testframework.injection.Supplier;

/**
 * Oracle 测试框架扩展，向 SPI 注册 {@link OracleDatabaseSupplier}。
 */
public class OracleTestFrameworkExtension implements TestFrameworkExtension {

    /** {@inheritDoc} 提供 Oracle 测试数据库供应器。 */
    @Override
    public List<Supplier<?, ?>> suppliers() {
        return List.of(new OracleDatabaseSupplier());
    }
}
