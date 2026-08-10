package org.keycloak.protocol.oidc.scope;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 参数化 OIDC 作用域类型 SPI：注册 {@link ParameterizedScopeTypeProvider} 提供者。
 * <p>用于支持带参数的 scope（如 {@code scope:parameter}）的校验与解析。</p>
 */
public class ParameterizedScopeTypeSpi implements Spi {

    /** @return 内部 SPI，不对外暴露 */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** @return SPI 名称 {@code parameterized-scope-type} */
    @Override
    public String getName() {
        return "parameterized-scope-type";
    }

    /** @return 提供者接口 {@link ParameterizedScopeTypeProvider} */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return ParameterizedScopeTypeProvider.class;
    }

    /** @return 工厂接口（与 {@link ParameterizedScopeTypeProvider} 合并） */
    @Override
    public Class<? extends ProviderFactory<ParameterizedScopeTypeProvider>> getProviderFactoryClass() {
        return ParameterizedScopeTypeProvider.class;
    }
}
