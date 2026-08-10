package org.keycloak.testframework.infinispan;

import java.util.List;

import org.keycloak.testframework.TestFrameworkExtension;
import org.keycloak.testframework.injection.Supplier;

/**
 * Infinispan 外部服务器测试框架扩展。
 * <p>
 * 通过 SPI 注册 {@link InfinispanExternalServerSupplier}，供集成测试启动外部 Infinispan 容器。
 */
public class InfinispanExternalServerTestFrameworkExtension implements TestFrameworkExtension {

    /** {@inheritDoc} 返回 Infinispan 外部服务器供应器。 */
    @Override
    public List<Supplier<?, ?>> suppliers() {
        return List.of(new InfinispanExternalServerSupplier());
    }
}
