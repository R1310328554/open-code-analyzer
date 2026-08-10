package org.keycloak.testframework.mail;

import java.util.List;

import org.keycloak.testframework.TestFrameworkExtension;
import org.keycloak.testframework.injection.Supplier;

/**
 * GreenMail 测试框架扩展，向 SPI 注册 {@link GreenMailSupplier}。
 */
public class GreenMailTestFrameworkExtension implements TestFrameworkExtension {

    /** {@inheritDoc} 提供 GreenMail 邮件服务器供应器。 */
    @Override
    public List<Supplier<?, ?>> suppliers() {
        return List.of(new GreenMailSupplier());
    }

}
