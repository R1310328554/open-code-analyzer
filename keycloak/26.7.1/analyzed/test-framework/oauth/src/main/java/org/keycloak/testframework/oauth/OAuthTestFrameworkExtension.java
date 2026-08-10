package org.keycloak.testframework.oauth;

import java.util.List;

import org.keycloak.testframework.TestFrameworkExtension;
import org.keycloak.testframework.injection.Supplier;

/**
 * OAuth 测试框架扩展，通过 SPI 注册 OAuth 相关 {@link Supplier}。
 * <p>
 * 涵盖 OAuth 客户端、测试应用、模拟 IdP、CIMD/CIBA 以及 sector identifier 等组件。
 */
public class OAuthTestFrameworkExtension implements TestFrameworkExtension {

    /** {@inheritDoc} 返回 OAuth 测试所需的全部供应器。 */
    @Override
    public List<Supplier<?, ?>> suppliers() {
        return List.of(new OAuthClientSupplier(), new TestAppSupplier(), new OAuthIdentityProviderSupplier(),
                new CimdProviderSupplier(), new SectorIdentifierRedirectUrisSupplier(), new CibaProviderSupplier());
    }

}
