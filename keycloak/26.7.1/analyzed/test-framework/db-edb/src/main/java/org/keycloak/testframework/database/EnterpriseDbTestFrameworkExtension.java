package org.keycloak.testframework.database;

import java.util.List;

import org.keycloak.testframework.TestFrameworkExtension;
import org.keycloak.testframework.injection.Supplier;

/**
 * EnterpriseDB 测试框架扩展，通过 SPI 注册 {@link EnterpriseDbDatabaseSupplier}。
 */
public class EnterpriseDbTestFrameworkExtension implements TestFrameworkExtension {

    /** {@inheritDoc} 返回 EDB 容器数据库供应器。 */
    @Override
    public List<Supplier<?, ?>> suppliers() {
        return List.of(new EnterpriseDbDatabaseSupplier());
    }
}
